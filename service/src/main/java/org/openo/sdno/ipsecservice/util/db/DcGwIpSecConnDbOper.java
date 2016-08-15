/*
 * Copyright (c) 2016, Huawei Technologies Co., Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
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
import org.openo.sdno.overlayvpn.inventory.sdk.util.InventoryDaoUtil;
import org.openo.sdno.overlayvpn.model.netmodel.ipsec.DcGwIpSecConnection;
import org.openo.sdno.overlayvpn.result.ResultRsp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import net.sf.json.JSONObject;

/**
 * It is used to operate DcGwIpSecConnection table. <br/>
 * 
 * @author
 * @version SDNO 0.5 Jun 22, 2016
 */
public class DcGwIpSecConnDbOper {

    private static final Logger LOGGER = LoggerFactory.getLogger(DcGwIpSecConnDbOper.class);

    private static final String UUID = "uuid";

    private static final String CONNECTION_SERVICE_ID = "connectionServiceId";

    /**
     * Constructor<br/>
     * 
     * @since SDNO 0.5
     */
    private DcGwIpSecConnDbOper() {

    }

    /**
     * It is used to insert DcGwIpSecConnection data. <br/>
     * 
     * @param dcGwIpSecConnectionList The list of DcGwIpSecConnection data
     * @throws ServiceException When insert failed.
     * @since SDNO 0.5
     */
    public static void insert(List<DcGwIpSecConnection> dcGwIpSecConnectionList) throws ServiceException {

        new InventoryDaoUtil<DcGwIpSecConnection>().getInventoryDao().batchInsert(dcGwIpSecConnectionList);
    }

    /**
     * It is used to update status. <br/>
     * 
     * @param dcGwIpSecConnectionList The data that want to be updated
     * @param updateFieldList The field that want to be updated
     * @throws ServiceException When update failed.
     * @since SDNO 0.5
     */
    public static void update(List<DcGwIpSecConnection> dcGwIpSecConnectionList, String updateFieldList)
            throws ServiceException {
        new InventoryDaoUtil<DcGwIpSecConnection>().getInventoryDao().update(DcGwIpSecConnection.class,
                dcGwIpSecConnectionList, updateFieldList);
    }

    /**
     * It is used to query DcGwIpSecConnection data. <br/>
     * 
     * @param connectionId The connection id
     * @return The object list of DcGwIpSecConnection
     * @throws ServiceException When query failed
     * @since SDNO 0.5
     */
    public static ResultRsp<List<DcGwIpSecConnection>> query(String connectionId) throws ServiceException {
        return queryByFilter(connectionId, null);
    }

    /**
     * It is used to delete DcGwIpSecConnection data. <br/>
     * 
     * @param connectionId The connection id
     * @throws ServiceException When delete failed.
     * @since SDNO 0.5
     */
    public static void delete(String connectionId) throws ServiceException {
        ResultRsp<List<DcGwIpSecConnection>> queryDbRsp = queryByFilter(connectionId, UUID);
        if(CollectionUtils.isEmpty(queryDbRsp.getData())) {
            LOGGER.warn("delete error, connectionId (" + connectionId + ") is not found");
            return;
        }

        DcGwIpSecConnection dcGwIpSecConnection = queryDbRsp.getData().get(0);
        new InventoryDaoUtil<DcGwIpSecConnection>().getInventoryDao().delete(DcGwIpSecConnection.class,
                dcGwIpSecConnection.getUuid());
    }

    private static ResultRsp<List<DcGwIpSecConnection>> queryByFilter(String connectionId, String queryResultFields)
            throws ServiceException {
        Map<String, Object> filterMap = new HashMap<String, Object>();
        if(StringUtils.hasLength(connectionId)) {
            filterMap.put(CONNECTION_SERVICE_ID, Arrays.asList(connectionId));
        }

        String filter = JSONObject.fromObject(filterMap).toString();

        return new InventoryDaoUtil<DcGwIpSecConnection>().getInventoryDao().queryByFilter(DcGwIpSecConnection.class,
                filter, queryResultFields);
    }

}
