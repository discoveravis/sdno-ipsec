/*
 * Copyright 2016 Huawei Technologies Co., Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openo.sdno.ipsecservice.util.db;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.openo.baseservice.remoteservice.exception.ServiceException;
import org.openo.sdno.framework.container.util.JsonUtil;
import org.openo.sdno.ipsecservice.model.IpSecReqModelInfo;
import org.openo.sdno.ipsecservice.util.exception.ThrowException;
import org.openo.sdno.ipsecservice.util.operation.CommonUtil;
import org.openo.sdno.overlayvpn.inventory.sdk.util.InventoryDaoUtil;
import org.openo.sdno.overlayvpn.model.common.enums.ActionStatus;
import org.openo.sdno.overlayvpn.model.servicemodel.OverlayVpn;
import org.openo.sdno.overlayvpn.result.ResultRsp;
import org.openo.sdno.overlayvpn.util.objreflectoper.UuidAllocUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import net.sf.json.JSONObject;

/**
 * It is used to operate IpSecReqModelInfo table. <br>
 * 
 * @author
 * @version SDNO 0.5 June 16, 2016
 */
public class IpSecReqDbOper {

    private static final Logger LOGGER = LoggerFactory.getLogger(IpSecReqDbOper.class);

    private static final String ACTION_STATE = "actionState";

    private static final String UUID = "uuid";

    private static final String CONNECTION_ID = "connectionId";

    /**
     * Constructor<br>
     * 
     * @since SDNO 0.5
     */
    private IpSecReqDbOper() {

    }

    /**
     * It is used to check the special record is existed or not. <br>
     * 
     * @param connectionId The connectionId field in IpSecReqModelInfo
     * @return true if the record is existed
     * @throws ServiceException When check failed.
     * @since SDNO 0.5
     */
    public static boolean checkRecordIsExisted(String connectionId) throws ServiceException {
        ResultRsp<List<IpSecReqModelInfo>> queryDbRsp = queryByFilter(connectionId, UUID);
        if(CollectionUtils.isEmpty(queryDbRsp.getData())) {
            return false;
        }

        return true;
    }

    /**
     * It is used to insert the original data. <br>
     * 
     * @param overlayVpn The original data
     * @throws ServiceException When insert failed.
     * @since SDNO 0.5
     */
    public static void insert(OverlayVpn overlayVpn) throws ServiceException {
        IpSecReqModelInfo ipSecReqModelInfo = new IpSecReqModelInfo();

        UuidAllocUtil.allocUuid(ipSecReqModelInfo);
        ipSecReqModelInfo.setActionState(ActionStatus.CREATING.getName());
        ipSecReqModelInfo.setConnectionId(CommonUtil.getIpSecConnection(overlayVpn).getUuid());

        String requestJsonString = JsonUtil.toJson(overlayVpn);
        ipSecReqModelInfo.setData(requestJsonString);

        new InventoryDaoUtil<IpSecReqModelInfo>().getInventoryDao().insert(ipSecReqModelInfo);
    }

    /**
     * It is used to update status. <br>
     * 
     * @param connectionId The connection id
     * @param actionState The status
     * @throws ServiceException When update failed.
     * @since SDNO 0.5
     */
    public static void update(String connectionId, String actionState) throws ServiceException {
        ResultRsp<List<IpSecReqModelInfo>> queryDbRsp = queryByFilter(connectionId, null);
        if(CollectionUtils.isEmpty(queryDbRsp.getData())) {
            String errMsg = "update error, connectionId (" + connectionId + ") is not found";
            LOGGER.error(errMsg);
            ThrowException.throwResNotExist(errMsg);
        }

        IpSecReqModelInfo ipSecReqModelInfo = queryDbRsp.getData().get(0);
        ipSecReqModelInfo.setActionState(actionState);

        new InventoryDaoUtil<IpSecReqModelInfo>().getInventoryDao().update(ipSecReqModelInfo, ACTION_STATE);
    }

    /**
     * It is used to query the original data. <br>
     * 
     * @param connectionId The connection id
     * @return The object of OverlayVpn
     * @throws ServiceException When query failed
     * @since SDNO 0.5
     */
    public static OverlayVpn query(String connectionId) throws ServiceException {
        ResultRsp<List<IpSecReqModelInfo>> queryDbRsp = queryByFilter(connectionId, null);
        if(CollectionUtils.isEmpty(queryDbRsp.getData())) {
            LOGGER.warn("query error, connectionId (" + connectionId + ") is not found");
            return null;
        }

        IpSecReqModelInfo ipSecReqModelInfo = queryDbRsp.getData().get(0);

        return JsonUtil.fromJson(ipSecReqModelInfo.getData(), OverlayVpn.class);
    }

    /**
     * It is used to delete the original data. <br>
     * 
     * @param connectionId The connection id
     * @throws ServiceException When delete failed.
     * @since SDNO 0.5
     */
    public static void delete(String connectionId) throws ServiceException {
        ResultRsp<List<IpSecReqModelInfo>> queryDbRsp = queryByFilter(connectionId, UUID);
        if(CollectionUtils.isEmpty(queryDbRsp.getData())) {
            LOGGER.warn("delete error, connectionId (" + connectionId + ") is not found");
            return;
        }

        IpSecReqModelInfo ipSecReqModelInfo = queryDbRsp.getData().get(0);
        new InventoryDaoUtil<IpSecReqModelInfo>().getInventoryDao().delete(IpSecReqModelInfo.class,
                ipSecReqModelInfo.getUuid());
    }

    private static ResultRsp<List<IpSecReqModelInfo>> queryByFilter(String connectionId, String queryResultFields)
            throws ServiceException {
        Map<String, Object> filterMap = new HashMap<String, Object>();
        if(StringUtils.hasLength(connectionId)) {
            filterMap.put(CONNECTION_ID, Arrays.asList(connectionId));
        }

        String filter = JSONObject.fromObject(filterMap).toString();

        return new InventoryDaoUtil<IpSecReqModelInfo>().getInventoryDao().queryByFilter(IpSecReqModelInfo.class,
                filter, queryResultFields);
    }
}
