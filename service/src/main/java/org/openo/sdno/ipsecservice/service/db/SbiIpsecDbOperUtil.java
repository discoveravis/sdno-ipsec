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

package org.openo.sdno.ipsecservice.service.db;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.openo.baseservice.remoteservice.exception.ServiceException;
import org.openo.sdno.framework.container.util.JsonUtil;
import org.openo.sdno.framework.container.util.UuidUtils;
import org.openo.sdno.ipsecservice.model.enums.NeRoleType;
import org.openo.sdno.overlayvpn.dao.common.InventoryDao;
import org.openo.sdno.overlayvpn.inventory.sdk.util.InventoryDaoUtil;
import org.openo.sdno.overlayvpn.model.v2.ipsec.SbiIkePolicy;
import org.openo.sdno.overlayvpn.model.v2.ipsec.SbiIpSecPolicy;
import org.openo.sdno.overlayvpn.model.v2.ipsec.SbiNeIpSec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

/**
 * <br/>
 * <p>
 * </p>
 * 
 * @author
 * @version SDNO 0.5 Jan 11, 2017
 */
public class SbiIpsecDbOperUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(SbiIpsecDbOperUtil.class);

    private SbiIpsecDbOperUtil() {
    }

    public static void insertNeIpsecList(List<SbiNeIpSec> newSbiNeIpsecs) throws ServiceException {
        InventoryDao<SbiNeIpSec> greTunnelDao = new InventoryDaoUtil<SbiNeIpSec>().getInventoryDao();

        for(SbiNeIpSec sbiIpsec : newSbiNeIpsecs) {
            if(StringUtils.isEmpty(sbiIpsec.getUuid())) {
                sbiIpsec.setUuid(UuidUtils.createUuid());
            }
        }

        greTunnelDao.batchInsert(newSbiNeIpsecs);

        List<SbiIkePolicy> ikePolicyList = new ArrayList<SbiIkePolicy>();
        List<SbiIpSecPolicy> ipsecPolicyList = new ArrayList<SbiIpSecPolicy>();

        for(SbiNeIpSec sbiNeIpSec : newSbiNeIpsecs) {
            if(NeRoleType.VPC.getName().equals(sbiNeIpSec.getLocalNeRole())) {
                ikePolicyList.add(sbiNeIpSec.getIkePolicy());
                ipsecPolicyList.add(sbiNeIpSec.getIpSecPolicy());

                sbiNeIpSec.getIkePolicy().setSbiServiceId(sbiNeIpSec.getUuid());
                sbiNeIpSec.getIpSecPolicy().setSbiServiceId(sbiNeIpSec.getUuid());
            }

        }

        new InventoryDaoUtil<SbiIkePolicy>().getInventoryDao().batchInsert(ikePolicyList);
        new InventoryDaoUtil<SbiIpSecPolicy>().getInventoryDao().batchInsert(ipsecPolicyList);
    }

    /**
     * <br/>
     * 
     * @param sbiNeIpSecs
     * @throws ServiceException
     * @since SDNO 0.5
     */
    public static void deleteNeIpsecList(List<SbiNeIpSec> sbiNeIpSecs) throws ServiceException {
        if(CollectionUtils.isEmpty(sbiNeIpSecs)) {
            return;
        }

        List<String> uuids = new ArrayList<String>();
        for(SbiNeIpSec sbiNeIpsec : sbiNeIpSecs) {
            uuids.add(sbiNeIpsec.getUuid());
        }

        new InventoryDaoUtil<SbiNeIpSec>().getInventoryDao().batchDelete(SbiNeIpSec.class, uuids);
        LOGGER.info("Delete SbiNeIpSec complete");

        Map<String, List<String>> filterMap = new HashMap<String, List<String>>();
        filterMap.put("sbiServiceId", uuids);
        String filter = JsonUtil.toJson(filterMap);
        List<SbiIkePolicy> ikes =
                new InventoryDaoUtil<SbiIkePolicy>().getInventoryDao().batchQuery(SbiIkePolicy.class, filter).getData();
        if(!CollectionUtils.isEmpty(ikes)) {
            List<String> ids = new ArrayList<String>();
            for(SbiIkePolicy ike : ikes) {
                ids.add(ike.getUuid());
            }
            new InventoryDaoUtil<SbiIkePolicy>().getInventoryDao().batchDelete(SbiIkePolicy.class, ids);
            LOGGER.info("Delete SbiIkePolicy complete");
        }

        List<SbiIpSecPolicy> ipsecPolicys = new InventoryDaoUtil<SbiIpSecPolicy>().getInventoryDao()
                .batchQuery(SbiIpSecPolicy.class, filter).getData();
        if(!CollectionUtils.isEmpty(ipsecPolicys)) {
            List<String> ids = new ArrayList<String>();
            for(SbiIpSecPolicy ipsecPolicy : ipsecPolicys) {
                ids.add(ipsecPolicy.getUuid());
            }
            new InventoryDaoUtil<SbiIpSecPolicy>().getInventoryDao().batchDelete(SbiIpSecPolicy.class, ids);
            LOGGER.info("Delete SbiIpSecPolicy complete");
        }

    }
}
