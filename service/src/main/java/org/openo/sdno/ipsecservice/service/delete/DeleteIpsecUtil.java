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

package org.openo.sdno.ipsecservice.service.delete;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openo.baseservice.remoteservice.exception.ServiceException;
import org.openo.sdno.exception.ParameterServiceException;
import org.openo.sdno.framework.container.util.JsonUtil;
import org.openo.sdno.ipsecservice.model.enums.NeRoleType;
import org.openo.sdno.ipsecservice.resource.AllocateResUtil;
import org.openo.sdno.ipsecservice.service.db.SbiIpsecDbOperUtil;
import org.openo.sdno.overlayvpn.inventory.sdk.util.InventoryDaoUtil;
import org.openo.sdno.overlayvpn.model.v2.ipsec.NbiIpSec;
import org.openo.sdno.overlayvpn.model.v2.ipsec.SbiNeIpSec;
import org.openo.sdno.overlayvpn.util.check.UuidUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

/**
 * Class of delete ipsec utils.<br/>
 * 
 * @author
 * @version SDNO 0.5 Jan 14, 2017
 */
public class DeleteIpsecUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeleteIpsecUtil.class);

    private DeleteIpsecUtil() {
    }

    /**
     * Query Nbi ipsec data in db.<br/>
     * 
     * @param ipsecId uuid of ipsec
     * @return Nbi data
     * @throws ServiceException when query failed
     * @since SDNO 0.5
     */
    public static NbiIpSec getNbiData(String ipsecId) throws ServiceException {
        if(!UuidUtil.validate(ipsecId)) {
            LOGGER.error("delete ipsec fail.invalid id: ", ipsecId);
            throw new ParameterServiceException("delete ipsec failed!");
        }

        List<String> uuids = new ArrayList<>();
        uuids.add(ipsecId);
        List<NbiIpSec> nbiIpsecs =
                new InventoryDaoUtil<NbiIpSec>().getInventoryDao().batchQuery(NbiIpSec.class, uuids).getData();

        if(CollectionUtils.isEmpty(nbiIpsecs)) {
            return null;
        }

        return nbiIpsecs.get(0);
    }

    /**
     * Query Sbi ipsec data in db.<br/>
     * 
     * @param nbiIpSec Nbi ipsec object
     * @return List of sbi ipsec objects
     * @throws ServiceException when query failed
     * @since SDNO 0.5
     */
    public static List<SbiNeIpSec> getSbiData(NbiIpSec nbiIpSec) throws ServiceException {
        List<String> uuids = new ArrayList<>();
        uuids.add(nbiIpSec.getUuid());
        Map<String, List<String>> filterMap = new HashMap<>();
        filterMap.put("connectionServiceId", uuids);
        return new InventoryDaoUtil<SbiNeIpSec>().getInventoryDao()
                .batchQuery(SbiNeIpSec.class, JsonUtil.toJson(filterMap)).getData();
    }

    /**
     * Delete ipsec data.<br/>
     * 
     * @param nbiIpsec Nbi ipsec object
     * @param sbiNeIpSecs List of sbi ipsec objects
     * @throws ServiceException when delete failed
     * @since SDNO 0.5
     */
    public static void delData(NbiIpSec nbiIpsec, List<SbiNeIpSec> sbiNeIpSecs) throws ServiceException {
        SbiIpsecDbOperUtil.deleteNeIpsecList(sbiNeIpSecs);

        for(SbiNeIpSec sbiNeIpSec : sbiNeIpSecs) {
            if(!NeRoleType.VPC.getName().equals(sbiNeIpSec.getLocalNeRole())) {
                AllocateResUtil.freeSeqNum(sbiNeIpSec.getNeId() + sbiNeIpSec.getSoureIfName(),
                        sbiNeIpSec.getExternalId());
            }
        }

        new InventoryDaoUtil<NbiIpSec>().getInventoryDao().delete(NbiIpSec.class, nbiIpsec.getUuid());
        LOGGER.info("Delete NbiIpSec complete");
    }

}
