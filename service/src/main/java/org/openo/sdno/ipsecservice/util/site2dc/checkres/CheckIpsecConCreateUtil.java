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

package org.openo.sdno.ipsecservice.util.site2dc.checkres;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openo.baseservice.remoteservice.exception.ServiceException;
import org.openo.sdno.framework.container.util.JsonUtil;
import org.openo.sdno.ipsecservice.model.enums.DeployStatus;
import org.openo.sdno.ipsecservice.model.enums.OperationStatus;
import org.openo.sdno.overlayvpn.inventory.sdk.util.InventoryDaoUtil;
import org.openo.sdno.overlayvpn.model.v2.ipsec.SbiNeIpSec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class of check ipsec util.<br/>
 * 
 * @author
 * @version SDNO 0.5 Jan 11, 2017
 */
public class CheckIpsecConCreateUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(CheckIpsecConCreateUtil.class);

    private CheckIpsecConCreateUtil() {
    }

    /**
     * Check ipsec has been created or not.<br/>
     * 
     * @param acSbiNeIpsecList list of ac sbi ipsec objects
     * @param fsSbiNeIpsecList list of fs sbi ipsec objects
     * @param fsCreateList list of created fs ipsecs
     * @param acCreateList list of created ac ipsecs
     * @throws ServiceException when check failed
     * @since SDNO 0.5
     */
    public static void checkIpsecConnIsCreated(List<SbiNeIpSec> acSbiNeIpsecList, List<SbiNeIpSec> fsSbiNeIpsecList,
            List<SbiNeIpSec> fsCreateList, List<SbiNeIpSec> acCreateList) throws ServiceException {
        Set<String> neIdSet = new HashSet<String>();
        for(SbiNeIpSec sbiNeIpSec : acSbiNeIpsecList) {
            neIdSet.add(sbiNeIpSec.getNeId());
        }

        for(SbiNeIpSec sbiNeIpSec : fsSbiNeIpsecList) {
            neIdSet.add(sbiNeIpSec.getNeId());
        }

        Map<String, List<String>> filter = new HashMap<>();

        filter.put("neId", new ArrayList<String>(neIdSet));
        filter.put("deployStatus", Arrays.asList("deploy"));
        List<SbiNeIpSec> dbSbiNeIpSecList = new InventoryDaoUtil<SbiNeIpSec>().getInventoryDao()
                .batchQuery(SbiNeIpSec.class, JsonUtil.toJson(filter)).getData();

        Iterator<SbiNeIpSec> iter = acSbiNeIpsecList.iterator();
        while(iter.hasNext()) {
            SbiNeIpSec sbiNeIpSec = iter.next();
            for(SbiNeIpSec dbSbiNeIpSec : dbSbiNeIpSecList) {
                if(sbiNeIpSec.getUuid().equals(dbSbiNeIpSec.getUuid())) {
                    continue;
                }

                if(CheckIpSecConSrcSnkSameUtil.checkSrcSnkSame(acCreateList, iter, sbiNeIpSec, dbSbiNeIpSec)) {
                    LOGGER.warn("this ipsec has been created");
                    break;
                }

                if(checkTemplateIsCreate(acCreateList, iter, sbiNeIpSec, dbSbiNeIpSec)) {
                    LOGGER.warn("this template ipsec has been created");
                    break;
                }
            }

        }

        iter = fsSbiNeIpsecList.iterator();
        while(iter.hasNext()) {
            SbiNeIpSec sbiNeIpSec = iter.next();
            for(SbiNeIpSec dbSbiNeIpSec : dbSbiNeIpSecList) {
                if(sbiNeIpSec.getUuid().equals(dbSbiNeIpSec.getUuid())
                        || DeployStatus.UNDEPLOY.getName().equals(dbSbiNeIpSec.getDeployStatus())) {
                    continue;
                }

                if(CheckIpSecConSrcSnkSameUtil.checkSrcSnkSame(acCreateList, iter, sbiNeIpSec, dbSbiNeIpSec)) {
                    LOGGER.warn("this ipsec has been created");
                    break;
                }
            }

        }
    }

    private static boolean checkTemplateIsCreate(List<SbiNeIpSec> activeIpsecs, Iterator<SbiNeIpSec> iter,
            SbiNeIpSec sbiNeIpSec, SbiNeIpSec dbSbiNeIpSec) throws ServiceException {
        if(OperationStatus.CREATING.getName().equals(dbSbiNeIpSec.getOperationStatus())) {
            return false;
        }

        if("true".equals(sbiNeIpSec.getIsTemplateType())
                && sbiNeIpSec.getSoureIfName().equals(dbSbiNeIpSec.getSoureIfName())) {
            sbiNeIpSec.setExternalId(dbSbiNeIpSec.getExternalId());
            sbiNeIpSec.setExternalIpSecId(dbSbiNeIpSec.getExternalIpSecId());
            iter.remove();
            activeIpsecs.add(sbiNeIpSec);

            new InventoryDaoUtil<SbiNeIpSec>().getInventoryDao().update(sbiNeIpSec,
                    "externalId,sourceAddress,sourceLanCidrs");
            return true;
        }
        return false;
    }
}
