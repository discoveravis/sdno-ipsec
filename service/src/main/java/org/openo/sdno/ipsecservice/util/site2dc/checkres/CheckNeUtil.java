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
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openo.baseservice.remoteservice.exception.ServiceException;
import org.openo.sdno.exception.InnerErrorServiceException;
import org.openo.sdno.exception.ParameterServiceException;
import org.openo.sdno.overlayvpn.brs.invdao.NetworkElementInvDao;
import org.openo.sdno.overlayvpn.brs.model.NetworkElementMO;
import org.openo.sdno.overlayvpn.model.v2.ipsec.NbiIpSec;
import org.openo.sdno.overlayvpn.model.v2.ipsec.SbiNeIpSec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

/**
 * Class of ne check util.<br/>
 * 
 * @author
 * @version SDNO 0.5 Jan 9, 2017
 */
public class CheckNeUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(CheckNeUtil.class);

    private CheckNeUtil() {
    }

    /**
     * Check ne resource.<br/>
     * 
     * @param neIdSet set of ne ids
     * @param deviceIdToNeMap map of device id and ne
     * @param ipsecCons list of ipsecs
     * @throws ServiceException when check failed
     * @since SDNO 0.5
     */
    public static void checkNesResource(Set<String> neIdSet, Map<String, NetworkElementMO> deviceIdToNeMap,
            List<NbiIpSec> ipsecCons) throws ServiceException {
        List<NetworkElementMO> queryedNeMos = new ArrayList<>();

        NetworkElementInvDao neDao = new NetworkElementInvDao();
        for(String neId : neIdSet) {
            queryedNeMos.add(neDao.query(neId));
        }

        for(String tempNeId : neIdSet) {
            NetworkElementMO tempNe = null;
            for(NetworkElementMO tempNeMo : queryedNeMos) {
                if(tempNeId.equals(tempNeMo.getId())) {
                    tempNe = tempNeMo;
                    break;
                }
            }

            try {
                checkNeBaseData(tempNeId, tempNe);
                if(!deviceIdToNeMap.containsKey(tempNe.getNativeID())) {
                    deviceIdToNeMap.put(tempNe.getNativeID(), tempNe);
                }
            } catch(ServiceException e) {
                LOGGER.error("checkNeBaseData failed! ne:", tempNeId);
                throw new ParameterServiceException("checkNeBaseData failed!");
            }
        }
    }

    private static void checkNeBaseData(String tempNeId, NetworkElementMO tempNe) throws ServiceException {
        if(null == tempNe) {
            LOGGER.error("ne not exist: " + tempNeId);
            throw new InnerErrorServiceException("ne not exist!");
        }

        if(!StringUtils.hasLength(tempNe.getNativeID())) {
            LOGGER.error("NativeID of NE is null,  ne: " + tempNeId);
            throw new ParameterServiceException("NativeID of NE is null");
        }

    }

    /**
     * Check ne and fill sbi objects.<br/>
     * 
     * @param neIds set of ne ids
     * @param sbiNeIpsecs list of sbi ipsecs
     * @throws ServiceException when operate failed
     * @since SDNO 0.5
     */
    @SuppressWarnings("null")
    public static void checkNesResourceAndFillSbi(Set<String> neIds, List<SbiNeIpSec> sbiNeIpsecs)
            throws ServiceException {
        List<NetworkElementMO> queryedNeMos = new ArrayList<>();
        List<NetworkElementMO> allNeMos = new ArrayList<>();
        try {
            allNeMos.addAll(new NetworkElementInvDao().getAllMO());
        } catch(ServiceException e) {
            LOGGER.error("batch query NeMO exception. ids: ", neIds);
            throw new InnerErrorServiceException("query ne failed!");
        }

        for(NetworkElementMO tmpNe : allNeMos) {
            if(neIds.contains(tmpNe.getId())) {
                queryedNeMos.add(tmpNe);
            }
        }

        for(String tempNeId : neIds) {
            NetworkElementMO tmpNe = null;
            for(NetworkElementMO tempNeMo : queryedNeMos) {
                if(tempNeId.equals(tempNeMo.getId())) {
                    tmpNe = tempNeMo;
                    break;
                }
                checkNeBaseData(tempNeId, tempNeMo);
                for(SbiNeIpSec sbiNeIpSec : sbiNeIpsecs) {
                    if(sbiNeIpSec.getNeId().equals(tmpNe.getId())) {
                        sbiNeIpSec.setDeviceId(tmpNe.getNativeID());
                    }
                }
            }
        }
    }

}
