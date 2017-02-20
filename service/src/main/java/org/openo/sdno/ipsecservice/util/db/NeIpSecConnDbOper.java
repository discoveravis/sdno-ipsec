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
import org.openo.sdno.overlayvpn.inventory.sdk.util.InventoryDaoUtil;
import org.openo.sdno.overlayvpn.model.netmodel.ipsec.NeIpSecConnection;
import org.openo.sdno.overlayvpn.result.ResultRsp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import net.sf.json.JSONObject;

/**
 * It is used to operate NeIpSecConnection table. <br>
 * 
 * @author
 * @version SDNO 0.5 June 22, 2016
 */
public class NeIpSecConnDbOper {

    private static final Logger LOGGER = LoggerFactory.getLogger(NeIpSecConnDbOper.class);

    private static final String UUID = "uuid";

    private static final String CONNECTION_SERVICE_ID = "connectionServiceId";

    /**
     * Constructor<br>
     * 
     * @since SDNO 0.5
     */
    private NeIpSecConnDbOper() {

    }

    /**
     * It is used to insert NeIpSecConnection data. <br>
     * 
     * @param neIpSecNeConnectionList The list of NeIpSecConnection data
     * @throws ServiceException When insert failed.
     * @since SDNO 0.5
     */
    public static void insert(List<NeIpSecConnection> neIpSecNeConnectionList) throws ServiceException {

        new InventoryDaoUtil<NeIpSecConnection>().getInventoryDao().batchInsert(neIpSecNeConnectionList);
    }

    /**
     * It is used to update status. <br>
     * 
     * @param neIpSecNeConnectionList The data that want to be updated
     * @param updateFieldList The field that want to be updated
     * @throws ServiceException When update failed.
     * @since SDNO 0.5
     */
    public static void update(List<NeIpSecConnection> neIpSecNeConnectionList, String updateFieldList)
            throws ServiceException {
        new InventoryDaoUtil<NeIpSecConnection>().getInventoryDao().update(NeIpSecConnection.class,
                neIpSecNeConnectionList, updateFieldList);
    }

    /**
     * It is used to query NeIpSecConnection data. <br>
     * 
     * @param connectionId The connection id
     * @return The object list of NeIpSecConnection
     * @throws ServiceException When query failed
     * @since SDNO 0.5
     */
    public static ResultRsp<List<NeIpSecConnection>> query(String connectionId) throws ServiceException {
        return queryByFilter(connectionId, null);
    }

    /**
     * It is used to delete NeIpSecConnection data. <br>
     * 
     * @param connectionId The connection id
     * @throws ServiceException When delete failed.
     * @since SDNO 0.5
     */
    public static void delete(String connectionId) throws ServiceException {
        ResultRsp<List<NeIpSecConnection>> queryDbRsp = queryByFilter(connectionId, UUID);
        if(CollectionUtils.isEmpty(queryDbRsp.getData())) {
            LOGGER.warn("delete error, connectionId (" + connectionId + ") is not found");
            return;
        }

        NeIpSecConnection neIpSecConnection = queryDbRsp.getData().get(0);
        new InventoryDaoUtil<NeIpSecConnection>().getInventoryDao().delete(NeIpSecConnection.class,
                neIpSecConnection.getUuid());
    }

    private static ResultRsp<List<NeIpSecConnection>> queryByFilter(String connectionId, String queryResultFields)
            throws ServiceException {
        Map<String, Object> filterMap = new HashMap<>();
        if(StringUtils.hasLength(connectionId)) {
            filterMap.put(CONNECTION_SERVICE_ID, Arrays.asList(connectionId));
        }

        String filter = JSONObject.fromObject(filterMap).toString();

        return new InventoryDaoUtil<NeIpSecConnection>().getInventoryDao().queryByFilter(NeIpSecConnection.class,
                filter, queryResultFields);
    }

}
