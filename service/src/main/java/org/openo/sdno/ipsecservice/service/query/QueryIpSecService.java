/*
 * Copyright (c) 2017, Huawei Technologies Co., Ltd.
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

package org.openo.sdno.ipsecservice.service.query;

import java.util.List;

import org.openo.baseservice.remoteservice.exception.ServiceException;
import org.openo.sdno.exception.InnerErrorServiceException;
import org.openo.sdno.overlayvpn.errorcode.ErrorCode;
import org.openo.sdno.overlayvpn.inventory.sdk.util.InventoryDaoUtil;
import org.openo.sdno.overlayvpn.model.v2.ipsec.NbiIpSec;
import org.openo.sdno.overlayvpn.model.v2.result.ResultRsp;
import org.openo.sdno.overlayvpn.util.FilterDataUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class of query ipsec util.<br/>
 * <p>
 * </p>
 * 
 * @author
 * @version SDNO 0.5 Jan 13, 2017
 */
public class QueryIpSecService {

    private static final Logger LOGGER = LoggerFactory.getLogger(QueryIpSecService.class);

    private QueryIpSecService() {
    }

    /**
     * Query ipsec connection.<br/>
     * 
     * @param ipSecConnectionId uuid of ipsec
     * @return query result
     * @throws ServiceException when query failed
     * @since SDNO 0.5
     */
    public static ResultRsp<NbiIpSec> queryIpsecConnection(String ipSecConnectionId) throws ServiceException {
        NbiIpSec ipsecCon = new InventoryDaoUtil<NbiIpSec>().getInventoryDao()
                .query(NbiIpSec.class, ipSecConnectionId, null).getData();
        if(null == ipsecCon) {
            LOGGER.error("IpsecConnection not found. id: ", ipSecConnectionId);
            throw new InnerErrorServiceException("query IpsecConnection.IpsecConnection not found!");
        }

        return new ResultRsp<NbiIpSec>(ErrorCode.OVERLAYVPN_SUCCESS, ipsecCon);
    }

    /**
     * Batch query ipsec connections.<br/>
     * 
     * @param ids List of uuids
     * @return query result
     * @throws ServiceException when query failed
     * @since SDNO 0.5
     */
    public static ResultRsp<List<NbiIpSec>> queryIpsecConnection(List<String> ids) throws ServiceException {
        List<NbiIpSec> ipsecCons = new InventoryDaoUtil<NbiIpSec>().getInventoryDao()
                .batchQuery(NbiIpSec.class, FilterDataUtil.getFilterData("uuid", ids)).getData();

        return new ResultRsp<List<NbiIpSec>>(ErrorCode.OVERLAYVPN_SUCCESS, ipsecCons);
    }
}
