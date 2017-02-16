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

package org.openo.sdno.ipsecservice.util.site2dc;

import java.util.List;

import org.openo.baseservice.remoteservice.exception.ServiceException;
import org.openo.sdno.exception.InnerErrorServiceException;
import org.openo.sdno.exception.ParameterServiceException;
import org.openo.sdno.overlayvpn.inventory.sdk.util.InventoryDaoUtil;
import org.openo.sdno.overlayvpn.model.v2.ipsec.NbiIpSec;
import org.openo.sdno.overlayvpn.model.v2.ipsec.SbiNeIpSec;
import org.openo.sdno.overlayvpn.util.FilterDataUtil;
import org.openo.sdno.overlayvpn.util.check.UuidUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class of deploy and undeploy util.<br/>
 * 
 * @author
 * @version SDNO 0.5 Jan 13, 2017
 */
public class ActionUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(ActionUtil.class);

    private ActionUtil() {
    }

    /**
     * Check data and query sbi data in db.<br/>
     * 
     * @param ipsecIds list of ipsec uuids
     * @param nbiIpsecs list of nbi ipsecs
     * @return list of sbi ipsecs
     * @throws ServiceException when operate failed
     * @since SDNO 0.5
     */
    public static List<SbiNeIpSec> checkDataAndQueryIpsecInDb(List<String> ipsecIds, List<NbiIpSec> nbiIpsecs)
            throws ServiceException {

        if(!UuidUtil.validate(ipsecIds)) {
            LOGGER.error("Input ids are invalid.");
            throw new ParameterServiceException("ipsecId is invalid!");
        }

        nbiIpsecs.addAll(new InventoryDaoUtil<NbiIpSec>().getInventoryDao()
                .batchQuery(NbiIpSec.class, FilterDataUtil.getFilterData("uuid", ipsecIds)).getData());

        if(nbiIpsecs.size() != ipsecIds.size()) {
            LOGGER.error("can't find resource!");
            throw new InnerErrorServiceException("can't find reource!");
        }

        return new InventoryDaoUtil<SbiNeIpSec>().getInventoryDao()
                .batchQuery(SbiNeIpSec.class, FilterDataUtil.getFilterData("connectionServiceId", ipsecIds)).getData();
    }
}
