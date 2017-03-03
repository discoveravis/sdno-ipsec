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

package org.openo.sdno.ipsecservice.service.create;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openo.baseservice.remoteservice.exception.ServiceException;
import org.openo.sdno.exception.ParameterServiceException;
import org.openo.sdno.framework.container.util.JsonUtil;
import org.openo.sdno.ipsecservice.model.enums.NeRoleType;
import org.openo.sdno.overlayvpn.model.v2.ipsec.NbiIpSec;
import org.openo.sdno.overlayvpn.model.v2.ipsec.SbiIkePolicy;
import org.openo.sdno.overlayvpn.model.v2.ipsec.SbiIp;
import org.openo.sdno.overlayvpn.model.v2.ipsec.SbiIpSecPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

/**
 * Fill data util class.<br/>
 * 
 * @author
 * @version SDNO 0.5 Jan 7, 2017
 */
public class FillDataUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(FillDataUtil.class);

    private FillDataUtil() {
    }

    /**
     * Fill policy in Nbi model.<br/>
     * 
     * @param ipsecs List of Nbi ipsec objects
     * @throws ServiceException when fill data failed
     * @since SDNO 0.5
     */
    public static void fillPolicy(List<NbiIpSec> ipsecs) throws ServiceException {
        for(NbiIpSec nbiIpSec : ipsecs) {
            try {
                fillPolicyData(nbiIpSec);
            } catch(IllegalArgumentException e) {
                LOGGER.error("input body json error, " + e);

                throw new ParameterServiceException("Input Body Is Error");
            }
        }

    }

    private static void fillPolicyData(NbiIpSec nbiIpSec) {
        String ikePolicy = nbiIpSec.getIkePolicy();
        if(StringUtils.hasLength(ikePolicy)) {
            nbiIpSec.setIkePolicyData(JsonUtil.fromJson(ikePolicy, SbiIkePolicy.class));
        }

        String ipsecPolicy = nbiIpSec.getIpsecPolicy();
        if(StringUtils.hasLength(ipsecPolicy)) {
            nbiIpSec.setIpSecPolicyData(JsonUtil.fromJson(ipsecPolicy, SbiIpSecPolicy.class));
        }
    }

    /**
     * Fill source ne data.<br/>
     * 
     * @param neIdPortNameToPortNameMap Map of ne id and port name
     * @param neIdSet Set of ne id
     * @param temGreTunnel Nbi ipsec
     * @return ne id
     * @since SDNO 0.5
     */
    public static String fillSrcNeData(Map<String, String> neIdPortNameToPortNameMap, Set<String> neIdSet,
            NbiIpSec temGreTunnel) {
        String srcNeRole = temGreTunnel.getSrcNeRole();
        String srcNeId = temGreTunnel.getSrcNeId();
        String srcPortName = temGreTunnel.getSrcPortName();

        if(!NeRoleType.VPC.getName().equals(srcNeRole)) {
            neIdPortNameToPortNameMap.put(srcNeId + srcPortName, srcPortName);
            neIdSet.add(srcNeId);
        }

        return srcNeId;
    }

    /**
     * Fill dest ne data.<br/>
     * 
     * @param neIdPortNameToPortNameMap Map of ne id and port name
     * @param neIdSet Set of ne id
     * @param temGreTunnel Nbi ipsec
     * @return ne id
     * @since SDNO 0.5
     */
    public static String fillDestNeData(Map<String, String> neIdPortNameToPortNameMap, Set<String> neIdSet,
            NbiIpSec temGreTunnel) {
        String destNeRole = temGreTunnel.getDestNeRole();
        String destNeId = temGreTunnel.getDestNeId();
        String destPortName = temGreTunnel.getDestPortName();

        if(!NeRoleType.VPC.getName().equals(destNeRole)) {
            neIdPortNameToPortNameMap.put(destNeId + destPortName, destPortName);
            neIdSet.add(destNeId);
        }

        return destNeId;
    }

    /**
     * Fill device id and ip in Nbi model.<br/>
     * 
     * @param deviceIdPortNameToIpMap Map of device id and port name
     * @param greTunnel Nbi ipsec model
     * @param srcDeviceId source device id
     * @param destDeviceId dest device id
     * @since SDNO 0.5
     */
    public static void fillDeviceIdAndIp(Map<String, SbiIp> deviceIdPortNameToIpMap, NbiIpSec greTunnel,
            String srcDeviceId, String destDeviceId) {
        greTunnel.setSrcDeviceId(srcDeviceId);
        greTunnel.setDestDeviceId(destDeviceId);
        if(StringUtils.hasLength(srcDeviceId)) {
            greTunnel.setSrcIp(deviceIdPortNameToIpMap.get(srcDeviceId + greTunnel.getSrcPortName()));
        }

        if(StringUtils.hasLength(destDeviceId)) {
            greTunnel.setDestIp(deviceIdPortNameToIpMap.get(destDeviceId + greTunnel.getDestPortName()));
        }
    }
}
