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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.openo.baseservice.remoteservice.exception.ServiceException;
import org.openo.sdno.framework.container.util.JsonUtil;
import org.openo.sdno.ipsecservice.model.enums.NeRoleType;
import org.openo.sdno.ipsecservice.resource.VpcUtil;
import org.openo.sdno.overlayvpn.model.netmodel.vpc.Vpc;
import org.openo.sdno.overlayvpn.model.v2.ipsec.NbiIpSec;
import org.openo.sdno.overlayvpn.model.v2.ipsec.SbiIp;
import org.openo.sdno.overlayvpn.model.v2.ipsec.SbiNeIpSec;

/**
 * Class of model transfer util.<br/>
 * 
 * @author
 * @version SDNO 0.5 Jan 10, 2017
 */
public class NbiModelToSbiModel {

    private NbiModelToSbiModel() {
    }

    /**
     * Transfer nbi model to sbi.<br/>
     * 
     * @param ipsecConnections list of nbi ipsecs
     * @param deviceIdToCtrollMap map of device id and controller id
     * @return list of sbi ipsecs
     * @throws ServiceException when transfer failed
     * @since SDNO 0.5
     */
    public static List<SbiNeIpSec> convertToNeIpsec(List<NbiIpSec> ipsecConnections,
            Map<String, String> deviceIdToCtrollMap) throws ServiceException {
        List<SbiNeIpSec> sbiNeIpSecs = new ArrayList<>();

        if(CollectionUtils.isEmpty(ipsecConnections)) {
            return sbiNeIpSecs;
        }

        for(NbiIpSec ipsec : ipsecConnections) {
            SbiNeIpSec srcSbiNeTunnel = new SbiNeIpSec();
            setBasicSbiNeTunnel(ipsec, srcSbiNeTunnel);
            setSrcSbiNeTunnel(ipsec, srcSbiNeTunnel, deviceIdToCtrollMap);

            SbiNeIpSec destSbiNeTunnel = new SbiNeIpSec();
            setBasicSbiNeTunnel(ipsec, destSbiNeTunnel);
            setDestSbiNeTunnel(ipsec, destSbiNeTunnel, deviceIdToCtrollMap);

            sbiNeIpSecs.add(srcSbiNeTunnel);
            sbiNeIpSecs.add(destSbiNeTunnel);
        }

        return sbiNeIpSecs;

    }

    private static void setBasicSbiNeTunnel(NbiIpSec nbiIpsec, SbiNeIpSec sbiIpsec) {
        sbiIpsec.setName(nbiIpsec.getName());
        sbiIpsec.setTenantId(nbiIpsec.getTenantId());
        sbiIpsec.setDescription(nbiIpsec.getDescription());
        sbiIpsec.setDeployStatus(nbiIpsec.getDeployStatus());
        sbiIpsec.setActiveStatus(nbiIpsec.getActiveStatus());

        sbiIpsec.setIkePolicy(nbiIpsec.getIkePolicyData());
        sbiIpsec.setIpSecPolicy(nbiIpsec.getIpSecPolicyData());

        sbiIpsec.setNqa(nbiIpsec.getNqa());
        sbiIpsec.setWorkType(nbiIpsec.getWorkType());
        sbiIpsec.setConnectionServiceId(nbiIpsec.getUuid());
        sbiIpsec.setProtectionPolicy(nbiIpsec.getProtectionPolicy());
        sbiIpsec.setQosPreClassify(nbiIpsec.getQosPreClassify());
        sbiIpsec.setTenantName(null);
        sbiIpsec.setRegionId(nbiIpsec.getRegionId());
    }

    private static void setSrcSbiNeTunnel(NbiIpSec nbiIpsec, SbiNeIpSec srcSbiNeIpsec,
            Map<String, String> deviceIdToCtrollMap) throws ServiceException {
        srcSbiNeIpsec.setDeviceId(nbiIpsec.getSrcDeviceId());
        srcSbiNeIpsec.setPeerDeviceId(nbiIpsec.getDestDeviceId());
        srcSbiNeIpsec.setNeId(nbiIpsec.getSrcNeId());
        srcSbiNeIpsec.setPeerNeId(nbiIpsec.getDestNeId());

        if(nbiIpsec.getSrcIp() != null) {
            srcSbiNeIpsec.setSourceAddress(JsonUtil.toJson(nbiIpsec.getSrcIp()));
        }

        if(nbiIpsec.getDestIp() != null) {
            srcSbiNeIpsec.setPeerAddress(JsonUtil.toJson(nbiIpsec.getDestIp()));
        }

        srcSbiNeIpsec.setWorkType(nbiIpsec.getWorkType());
        srcSbiNeIpsec.setSourceLanCidrs(nbiIpsec.getSourceLanCidrs());
        srcSbiNeIpsec.setPeerLanCidrs(nbiIpsec.getDestLanCidrs());
        srcSbiNeIpsec.setIsTemplateType(nbiIpsec.getSrcIsTemplateType());

        if(!StringUtils.isEmpty(nbiIpsec.getSrcDeviceId())) {
            srcSbiNeIpsec.setControllerId(deviceIdToCtrollMap.get(nbiIpsec.getSrcDeviceId()));
        }

        if(NeRoleType.VPC.getName().equals(nbiIpsec.getSrcNeRole())) {
            Vpc vpc = VpcUtil.queryById(srcSbiNeIpsec.getNeId());
            srcSbiNeIpsec.setControllerId(vpc.getOsControllerId());
            srcSbiNeIpsec.setSourceAddress(JsonUtil.toJson(new SbiIp(vpc.getExternalIp())));
        }

        if(NeRoleType.VPC.getName().equals(nbiIpsec.getDestNeRole())) {
            Vpc vpc = VpcUtil.queryById(srcSbiNeIpsec.getPeerNeId());
            srcSbiNeIpsec.setPeerAddress(JsonUtil.toJson(new SbiIp(vpc.getExternalIp())));
        }

        srcSbiNeIpsec.setSoureIfName(nbiIpsec.getSrcPortName());
        srcSbiNeIpsec.setDestIfName(nbiIpsec.getDestPortName());
        srcSbiNeIpsec.setLocalNeRole(nbiIpsec.getSrcNeRole());
    }

    private static void setDestSbiNeTunnel(NbiIpSec nbiIpsec, SbiNeIpSec destSbiNeIpsec,
            Map<String, String> deviceIdToCtrollMap) throws ServiceException {
        destSbiNeIpsec.setDeviceId(nbiIpsec.getDestDeviceId());
        destSbiNeIpsec.setPeerDeviceId(nbiIpsec.getSrcDeviceId());
        destSbiNeIpsec.setNeId(nbiIpsec.getDestNeId());
        destSbiNeIpsec.setPeerNeId(nbiIpsec.getSrcNeId());

        if(nbiIpsec.getDestIp() != null) {
            destSbiNeIpsec.setSourceAddress(JsonUtil.toJson(nbiIpsec.getDestIp()));
        }

        if(nbiIpsec.getSrcIp() != null) {
            destSbiNeIpsec.setPeerAddress(JsonUtil.toJson(nbiIpsec.getSrcIp()));
        }

        destSbiNeIpsec.setSourceLanCidrs(nbiIpsec.getDestLanCidrs());
        destSbiNeIpsec.setPeerLanCidrs(nbiIpsec.getSourceLanCidrs());
        destSbiNeIpsec.setIsTemplateType(nbiIpsec.getDestIsTemplateType());

        if(!StringUtils.isEmpty(nbiIpsec.getDestDeviceId())) {
            destSbiNeIpsec.setControllerId(deviceIdToCtrollMap.get(nbiIpsec.getDestDeviceId()));
        }

        if(NeRoleType.VPC.getName().equals(nbiIpsec.getDestNeRole())) {
            Vpc vpc = VpcUtil.queryById(destSbiNeIpsec.getNeId());
            destSbiNeIpsec.setControllerId(vpc.getOsControllerId());
            destSbiNeIpsec.setSourceAddress(JsonUtil.toJson(new SbiIp(vpc.getExternalIp())));
        }

        if(NeRoleType.VPC.getName().equals(nbiIpsec.getSrcNeRole())) {
            Vpc vpc = VpcUtil.queryById(destSbiNeIpsec.getPeerNeId());
            destSbiNeIpsec.setPeerAddress(JsonUtil.toJson(new SbiIp(vpc.getExternalIp())));
        }

        destSbiNeIpsec.setSoureIfName(nbiIpsec.getDestPortName());
        destSbiNeIpsec.setDestIfName(nbiIpsec.getSrcPortName());
        destSbiNeIpsec.setLocalNeRole(nbiIpsec.getDestNeRole());
    }
}
