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

package org.openo.sdno.ipsecservice.service.action;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.openo.baseservice.remoteservice.exception.ServiceException;
import org.openo.sdno.exception.InnerErrorServiceException;
import org.openo.sdno.framework.container.util.JsonUtil;
import org.openo.sdno.ipsecservice.model.enums.DeployStatus;
import org.openo.sdno.ipsecservice.model.enums.NeRoleType;
import org.openo.sdno.ipsecservice.model.enums.OperationStatus;
import org.openo.sdno.ipsecservice.model.enums.OperationType;
import org.openo.sdno.ipsecservice.service.create.CreateUtil;
import org.openo.sdno.ipsecservice.util.site2dc.ActionUtil;
import org.openo.sdno.ipsecservice.util.site2dc.MergeRspUtil;
import org.openo.sdno.ipsecservice.util.site2dc.checkres.CheckIpsecConCreateUtil;
import org.openo.sdno.ipsecservice.util.site2dc.checkres.CheckNeUtil;
import org.openo.sdno.overlayvpn.inventory.sdk.util.InventoryDaoUtil;
import org.openo.sdno.overlayvpn.model.v2.ipsec.NbiIpSec;
import org.openo.sdno.overlayvpn.model.v2.ipsec.SbiIkePolicy;
import org.openo.sdno.overlayvpn.model.v2.ipsec.SbiIpSecPolicy;
import org.openo.sdno.overlayvpn.model.v2.ipsec.SbiNeIpSec;
import org.openo.sdno.overlayvpn.model.v2.result.ResultRsp;
import org.openo.sdno.overlayvpn.result.FailData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

/**
 * Deploy ipsec util.<br/>
 * 
 * @author
 * @version SDNO 0.5 Jan 13, 2017
 */
public class DeployIpsecUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeployIpsecUtil.class);

    private DeployIpsecUtil() {

    }

    /**
     * Deploy ipsec connections.<br/>
     * 
     * @param req HttpServletRequest Object
     * @param ipsecIds uuids of ipsec to be deploy
     * @return List of uuids
     * @throws ServiceException when deploy failed
     * @since SDNO 0.5
     */
    public static final List<String> doDeploy(HttpServletRequest req, List<String> ipsecIds) throws ServiceException {
        List<NbiIpSec> nbiIpsecs = new ArrayList<>();
        List<SbiNeIpSec> sbiNeIpsecs = checkDataAndGetSbiNeIpsec(ipsecIds, nbiIpsecs);

        List<SbiNeIpSec> acInactiveNeIpsecs = new ArrayList<>();
        List<SbiNeIpSec> fsInactiveNeIpsecs = new ArrayList<>();
        List<SbiNeIpSec> activeNeIpsecs = new ArrayList<>();
        checkSbiStatus(acInactiveNeIpsecs, fsInactiveNeIpsecs, activeNeIpsecs, sbiNeIpsecs);

        if(CollectionUtils.isEmpty(acInactiveNeIpsecs) && CollectionUtils.isEmpty(fsInactiveNeIpsecs)) {
            return ipsecIds;
        }

        CheckIpsecConCreateUtil.checkIpsecConnIsCreated(acInactiveNeIpsecs, fsInactiveNeIpsecs, activeNeIpsecs,
                activeNeIpsecs);

        checkNeAndFillSbi(acInactiveNeIpsecs);

        ResultRsp<SbiNeIpSec> fsDeployRsp = new ResultRsp<>();
        fsDeployRsp.setSuccessed(new ArrayList<SbiNeIpSec>());
        fsDeployRsp.setFail(new ArrayList<FailData<SbiNeIpSec>>());

        if(!CollectionUtils.isEmpty(fsInactiveNeIpsecs)) {
            CreateUtil.createByFs(fsInactiveNeIpsecs, fsDeployRsp);
            if(!CollectionUtils.isEmpty(fsDeployRsp.getFail())) {
                LOGGER.error("deploy fs failed. fail num = ", fsDeployRsp.getFail().size());
                throw new InnerErrorServiceException("deploy in fs failed!");
            }
        }

        fillLanCidrByFsRsp(activeNeIpsecs, acInactiveNeIpsecs, fsDeployRsp);

        ResultRsp<SbiNeIpSec> acDeployRsp = new ResultRsp<>();
        acDeployRsp.setSuccessed(activeNeIpsecs);
        acDeployRsp.setFail(new ArrayList<FailData<SbiNeIpSec>>());
        if(!CollectionUtils.isEmpty(acInactiveNeIpsecs)) {
            CreateUtil.createByAc(acInactiveNeIpsecs, acDeployRsp);
            if(!CollectionUtils.isEmpty(acDeployRsp.getFail())) {
                LOGGER.error("deploy ac failed. fail num = ", acDeployRsp.getFail().size());
                throw new InnerErrorServiceException("deploy in ac failed!");
            }
        }

        ResultRsp<NbiIpSec> deployRsp =
                MergeRspUtil.mergeAllCreateRsp(nbiIpsecs, sbiNeIpsecs, acDeployRsp, fsDeployRsp, OperationType.DEPLOY);

        if(deployRsp.getSuccessed().size() == ipsecIds.size()) {
            LOGGER.info("ipsec deploy success! ");
            return ipsecIds;
        }

        LOGGER.error("deploy failed. success num = ", deployRsp.getSuccessed().size());
        throw new InnerErrorServiceException("deploy failed!");
    }

    private static void checkSbiStatus(List<SbiNeIpSec> acInactiveNeIpsecs, List<SbiNeIpSec> fsInactiveNeIpsecs,
            List<SbiNeIpSec> activeNeIpsecs, List<SbiNeIpSec> sbiNeIpsecs) {
        for(SbiNeIpSec sbiNeIpSec : sbiNeIpsecs) {
            if(DeployStatus.DEPLOY.getName().equals(sbiNeIpSec.getDeployStatus())) {
                activeNeIpsecs.add(sbiNeIpSec);
            } else if(NeRoleType.VPC.getName().equals(sbiNeIpSec.getLocalNeRole())) {
                fsInactiveNeIpsecs.add(sbiNeIpSec);
            } else {
                acInactiveNeIpsecs.add(sbiNeIpSec);
            }
        }

    }

    private static List<SbiNeIpSec> checkDataAndGetSbiNeIpsec(List<String> ipsecIds, List<NbiIpSec> nbiIpsecs)
            throws ServiceException {
        List<SbiNeIpSec> sbiNeIpSecs = ActionUtil.checkDataAndQueryIpsecInDb(ipsecIds, nbiIpsecs);

        for(NbiIpSec nbiIpsec : nbiIpsecs) {
            if(!StringUtils.isEmpty(nbiIpsec.getIkePolicy())) {
                nbiIpsec.setIkePolicyData(JsonUtil.fromJson(nbiIpsec.getIkePolicy(), SbiIkePolicy.class));
            }
            if(!StringUtils.isEmpty(nbiIpsec.getIpsecPolicy())) {
                nbiIpsec.setIpSecPolicyData(JsonUtil.fromJson(nbiIpsec.getIpsecPolicy(), SbiIpSecPolicy.class));
            }

            for(SbiNeIpSec sbiNeIpsec : sbiNeIpSecs) {
                if(nbiIpsec.getUuid().equals(sbiNeIpsec.getConnectionServiceId())) {
                    sbiNeIpsec.setNqa(nbiIpsec.getNqa());
                    sbiNeIpsec.setIkePolicy(nbiIpsec.getIkePolicyData());
                    sbiNeIpsec.setIpSecPolicy(nbiIpsec.getIpSecPolicyData());
                }
            }
        }

        return sbiNeIpSecs;
    }

    private static void checkNeAndFillSbi(List<SbiNeIpSec> acInactiveNeIpsecs) throws ServiceException {
        if(CollectionUtils.isEmpty(acInactiveNeIpsecs)) {
            return;
        }

        Set<String> neIds = new HashSet<>();
        for(SbiNeIpSec tmpSbiIpsec : acInactiveNeIpsecs) {
            neIds.add(tmpSbiIpsec.getNeId());
        }
        CheckNeUtil.checkNesResourceAndFillSbi(neIds, acInactiveNeIpsecs);

        for(SbiNeIpSec tmpSbiIpsec : acInactiveNeIpsecs) {
            tmpSbiIpsec.setOperationStatus(OperationStatus.DEPLOYING.getName());
        }

    }

    private static void fillLanCidrByFsRsp(List<SbiNeIpSec> activeIpsecs, List<SbiNeIpSec> acInativeIpsecs,
            ResultRsp<SbiNeIpSec> fsDeplyResult) throws ServiceException {
        List<SbiNeIpSec> updateSbiNeIpSecList = new ArrayList<>();
        List<SbiNeIpSec> updateSbiNeIpSecByFsList = fsDeplyResult.getSuccessed();

        for(SbiNeIpSec fsSbiNeIpSec : fsDeplyResult.getSuccessed()) {
            fillAcByFs(acInativeIpsecs, updateSbiNeIpSecList, fsSbiNeIpSec);
        }

        for(SbiNeIpSec fsSbiNeIpSec : activeIpsecs) {
            if(!NeRoleType.VPC.getName().equals(fsSbiNeIpSec.getLocalNeRole())) {
                continue;
            }

            fillAcByFs(acInativeIpsecs, updateSbiNeIpSecList, fsSbiNeIpSec);
        }

        new InventoryDaoUtil<SbiNeIpSec>().getInventoryDao().update(SbiNeIpSec.class, updateSbiNeIpSecList,
                "peerAddress, peerLanCidrs");
        new InventoryDaoUtil<SbiNeIpSec>().getInventoryDao().update(SbiNeIpSec.class, updateSbiNeIpSecByFsList,
                "sourceAddress, sourceLanCidrs");

    }

    private static void fillAcByFs(List<SbiNeIpSec> acSbiNeIpSecs, List<SbiNeIpSec> updateSbiNeIpSecs,
            SbiNeIpSec fsSbiNeIpSec) {
        for(SbiNeIpSec acSbiNeIpSec : acSbiNeIpSecs) {
            if(fsSbiNeIpSec.getNeId().equals(acSbiNeIpSec.getPeerNeId())
                    && fsSbiNeIpSec.getConnectionServiceId().equals(acSbiNeIpSec.getConnectionServiceId())) {
                acSbiNeIpSec.setPeerAddress(fsSbiNeIpSec.getSourceAddress());
                acSbiNeIpSec.setPeerLanCidrs(fsSbiNeIpSec.getSourceLanCidrs());

                updateSbiNeIpSecs.add(acSbiNeIpSec);
            }
        }
    }
}
