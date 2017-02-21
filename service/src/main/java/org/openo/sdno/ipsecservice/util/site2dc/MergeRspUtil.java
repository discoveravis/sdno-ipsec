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

import org.apache.commons.collections.CollectionUtils;
import org.openo.baseservice.remoteservice.exception.ServiceException;
import org.openo.sdno.ipsecservice.model.enums.DeployStatus;
import org.openo.sdno.ipsecservice.model.enums.NeRoleType;
import org.openo.sdno.ipsecservice.model.enums.OperationStatus;
import org.openo.sdno.ipsecservice.model.enums.OperationType;
import org.openo.sdno.overlayvpn.errorcode.ErrorCode;
import org.openo.sdno.overlayvpn.inventory.sdk.util.InventoryDaoUtil;
import org.openo.sdno.overlayvpn.model.v2.ipsec.NbiIpSec;
import org.openo.sdno.overlayvpn.model.v2.ipsec.SbiIkePolicy;
import org.openo.sdno.overlayvpn.model.v2.ipsec.SbiIpSecPolicy;
import org.openo.sdno.overlayvpn.model.v2.ipsec.SbiNeIpSec;
import org.openo.sdno.overlayvpn.model.v2.result.ResultRsp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class of merge result util.<br/>
 * 
 * @author
 * @version SDNO 0.5 Jan 12, 2017
 */
public class MergeRspUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(MergeRspUtil.class);

    private MergeRspUtil() {
    }

    /**
     * Merge result of creation.<br/>
     * 
     * @param nbiTunnels list of nbi ipsecs
     * @param createSbiNeTunnels list of sbi ipsecs
     * @param acCreateRsp result of ac
     * @param fsCreateRsp result of fs
     * @param operType operation type
     * @return operation result
     * @throws ServiceException when merge failed
     * @since SDNO 0.5
     */
    public static ResultRsp<NbiIpSec> mergeAllCreateRsp(List<NbiIpSec> nbiTunnels, List<SbiNeIpSec> createSbiNeTunnels,
            ResultRsp<SbiNeIpSec> acCreateRsp, ResultRsp<SbiNeIpSec> fsCreateRsp, OperationType operType)
            throws ServiceException {
        ResultRsp<NbiIpSec> createRsp = new ResultRsp<>();

        List<SbiNeIpSec> sbiNeTunnelCreateSuccess = acCreateRsp.getSuccessed();
        sbiNeTunnelCreateSuccess.addAll(fsCreateRsp.getSuccessed());

        if(OperationType.UNDEPLOY_CONST.equals(operType.getCommonName())) {
            updateUndeploySuccessTunnels(createSbiNeTunnels, sbiNeTunnelCreateSuccess);
        } else {
            updateDeploySuccessTunnels(createSbiNeTunnels, sbiNeTunnelCreateSuccess);
        }

        UpdateStatusUtil.updateNbiIpsecs(nbiTunnels, createSbiNeTunnels, operType);

        createRsp.setErrorCode(ErrorCode.OVERLAYVPN_SUCCESS);
        createRsp.setSuccessed(nbiTunnels);

        return createRsp;
    }

    private static void updateUndeploySuccessTunnels(List<SbiNeIpSec> createSbiNeTunnels,
            List<SbiNeIpSec> sbiNeTunnelCreateSuccess) throws ServiceException {
        List<SbiNeIpSec> needUpdateSbiNeIpSecs = new ArrayList<>();
        for(SbiNeIpSec sbiNeIpSec : createSbiNeTunnels) {
            for(SbiNeIpSec sbiNeIpSecSuccess : sbiNeTunnelCreateSuccess) {
                if(sbiNeIpSec.getUuid().equals(sbiNeIpSecSuccess.getUuid())) {
                    sbiNeIpSec.setOperationStatus(OperationStatus.NORMAL.getName());
                    sbiNeIpSec.setDeployStatus(DeployStatus.UNDEPLOY.getName());

                    needUpdateSbiNeIpSecs.add(sbiNeIpSec);
                    break;
                }
            }

            if(CollectionUtils.isNotEmpty(needUpdateSbiNeIpSecs)) {
                new InventoryDaoUtil<SbiNeIpSec>().getInventoryDao().update(SbiNeIpSec.class, needUpdateSbiNeIpSecs,
                        "deployStatus,operationStatus");

                LOGGER.info("update SbiNeIpSec's status complete. ");
            }
        }
    }

    private static void updateDeploySuccessTunnels(List<SbiNeIpSec> sbiNeIpSecs, List<SbiNeIpSec> sbiNeIpSecsSuccess)
            throws ServiceException {
        List<SbiNeIpSec> needUpdateSbiNeIpSecs = new ArrayList<>();
        List<SbiIkePolicy> ikePolicyList = new ArrayList<>();
        List<SbiIpSecPolicy> ipSecPolicyList = new ArrayList<>();

        for(SbiNeIpSec sbiNeIpSec : sbiNeIpSecs) {
            updateSbiData(sbiNeIpSec, sbiNeIpSecsSuccess, ikePolicyList, ipSecPolicyList, needUpdateSbiNeIpSecs);
        }

        if(CollectionUtils.isNotEmpty(needUpdateSbiNeIpSecs)) {
            new InventoryDaoUtil<SbiNeIpSec>().getInventoryDao().update(SbiNeIpSec.class, needUpdateSbiNeIpSecs,
                    "deployStatus,operationStatus,externalId,externalIpSecId");
            new InventoryDaoUtil<SbiIkePolicy>().getInventoryDao().update(SbiIkePolicy.class, ikePolicyList, null);
            new InventoryDaoUtil<SbiIpSecPolicy>().getInventoryDao().update(SbiIpSecPolicy.class, ipSecPolicyList,
                    null);
            LOGGER.info("update SbiNeIpSec's status and IkePolicy,IpSecPolicy complete. ");
        }

    }

    private static void updateSbiData(SbiNeIpSec sbiNeIpSec, List<SbiNeIpSec> sbiNeIpSecsSuccess,
            List<SbiIkePolicy> ikePolicyList, List<SbiIpSecPolicy> ipSecPolicyList,
            List<SbiNeIpSec> needUpdateSbiNeIpSecs) {
        for(SbiNeIpSec sbiNeIpSecSuccess : sbiNeIpSecsSuccess) {
            if(sbiNeIpSec.getUuid().equals(sbiNeIpSecSuccess.getUuid())) {
                sbiNeIpSec.setOperationStatus(OperationStatus.NORMAL.getName());
                sbiNeIpSec.setDeployStatus(DeployStatus.DEPLOY.getName());
                sbiNeIpSec.setExternalId(sbiNeIpSecSuccess.getExternalId());
                sbiNeIpSec.setExternalIpSecId(sbiNeIpSecSuccess.getExternalIpSecId());

                if(NeRoleType.VPC.getName().equals(sbiNeIpSecSuccess.getLocalNeRole())) {
                    ikePolicyList.add(sbiNeIpSecSuccess.getIkePolicy());
                    ipSecPolicyList.add(sbiNeIpSecSuccess.getIpSecPolicy());
                }
                needUpdateSbiNeIpSecs.add(sbiNeIpSec);

                break;
            }
        }
    }
}
