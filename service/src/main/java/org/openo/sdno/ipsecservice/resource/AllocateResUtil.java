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

package org.openo.sdno.ipsecservice.resource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.openo.baseservice.remoteservice.exception.ServiceException;
import org.openo.sdno.exception.InnerErrorServiceException;
import org.openo.sdno.framework.container.util.JsonUtil;
import org.openo.sdno.framework.container.util.UuidUtils;
import org.openo.sdno.ipsecservice.model.enums.NeRoleType;
import org.openo.sdno.overlayvpn.inventory.sdk.util.InventoryDaoUtil;
import org.openo.sdno.overlayvpn.model.v2.ipsec.SbiNeIpSec;
import org.openo.sdno.overlayvpn.res.ResourcesUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class of allocate external Id resource util.<br/>
 * 
 * @author
 * @version SDNO 0.5 Jan 10, 2017
 */
public class AllocateResUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(AllocateResUtil.class);

    private AllocateResUtil() {
    }

    /**
     * Allocate external id for ipsec.<br/>
     * 
     * @param sbiNeIpsecList list of Sbi ipsec objects
     * @throws ServiceException when allocate id failed
     * @since SDNO 0.5
     */
    public static void allocateExternalId(List<SbiNeIpSec> sbiNeIpsecList) throws ServiceException {
        Map<String, String> neIdToSecIdMap = new HashMap<>();
        Map<String, String> templateIdToSeqNum = new HashMap<>();

        for(SbiNeIpSec sbiNeIpSec : sbiNeIpsecList) {
            if(NeRoleType.VPC.getName().equals(sbiNeIpSec.getLocalNeRole())) {
                continue;
            }

            String neId = sbiNeIpSec.getNeId();
            String neIdPortName = neId + sbiNeIpSec.getSoureIfName();
            if(neIdToSecIdMap.containsKey(neIdPortName)) {
                sbiNeIpSec.setExternalIpSecId(neIdToSecIdMap.get(neIdPortName));

                if(templateIdToSeqNum.containsKey(neIdToSecIdMap.get(neIdPortName))) {
                    sbiNeIpSec.setExternalId(templateIdToSeqNum.get(neIdToSecIdMap.get(neIdPortName)));
                } else {
                    sbiNeIpSec.setExternalId(String.valueOf(allocateSeqNum(neIdPortName).get(0)));
                }
                continue;
            }

            Map<String, List<String>> filterMap = new HashMap<>();
            filterMap.put("neId", Arrays.asList(neId));
            filterMap.put("soureIfName", Arrays.asList(sbiNeIpSec.getSoureIfName()));

            List<SbiNeIpSec> dbSbiNeIpSecs = new InventoryDaoUtil<SbiNeIpSec>().getInventoryDao()
                    .batchQuery(SbiNeIpSec.class, JsonUtil.toJson(filterMap)).getData();

            if(CollectionUtils.isEmpty(dbSbiNeIpSecs)) {
                sbiNeIpSec.setExternalIpSecId(UuidUtils.createUuid());
                sbiNeIpSec.setExternalId(String.valueOf(allocateSeqNum(neIdPortName).get(0)));
            } else {
                sbiNeIpSec.setExternalIpSecId(dbSbiNeIpSecs.get(0).getExternalIpSecId());

                if("true".equals(sbiNeIpSec.getIsTemplateType())) {
                    sbiNeIpSec.setExternalId(dbSbiNeIpSecs.get(0).getExternalId());

                } else {
                    sbiNeIpSec.setExternalId(String.valueOf(allocateSeqNum(neIdPortName).get(0)));
                }
            }

            neIdToSecIdMap.put(neIdPortName, sbiNeIpSec.getExternalIpSecId());
            if("true".equals(sbiNeIpSec.getIsTemplateType())) {
                templateIdToSeqNum.put(sbiNeIpSec.getExternalIpSecId(), sbiNeIpSec.getExternalId());
            }

        }
    }

    /**
     * Free serial number.<br/>
     * 
     * @param neIdPortName ne id and port name
     * @param seqNum serial number
     * @throws ServiceException when free resource failed
     * @since SDNO 0.5
     */
    public static void freeSeqNum(String neIdPortName, String seqNum) throws ServiceException {
        LOGGER.info("free SeqNum strat");
        ResourcesUtil.freeGlobalValueList(neIdPortName + "-ipsec", "ipsec", Arrays.asList(Long.valueOf(seqNum)));
        LOGGER.info("free SeqNum end");
    }

    private static List<Integer> allocateSeqNum(String neIdPortName) throws ServiceException {
        List<Long> seqNumList = ResourcesUtil.requestGloabelValue(neIdPortName + "-ipsec", "ipsec", 1, 1L, 1000L);

        if(CollectionUtils.isEmpty(seqNumList)) {
            LOGGER.error("allocateSeqNum failed. neIdPortName: ", neIdPortName);
            throw new InnerErrorServiceException("allocate seq num failed!");
        }

        List<Integer> rst = new ArrayList<>();
        for(Long seq : seqNumList) {
            rst.add(Integer.valueOf(seq.toString()));
        }
        return rst;
    }
}
