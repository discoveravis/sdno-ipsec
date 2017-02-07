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
import org.openo.sdno.ipsecservice.model.enums.DeployStatus;
import org.openo.sdno.ipsecservice.model.enums.OperationStatus;
import org.openo.sdno.ipsecservice.model.enums.OperationType;
import org.openo.sdno.overlayvpn.dao.common.InventoryDao;
import org.openo.sdno.overlayvpn.inventory.sdk.util.InventoryDaoUtil;
import org.openo.sdno.overlayvpn.model.v2.ipsec.NbiIpSec;
import org.openo.sdno.overlayvpn.model.v2.ipsec.SbiNeIpSec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <br/>
 * <p>
 * </p>
 * 
 * @author
 * @version SDNO 0.5 Jan 12, 2017
 */
public class UpdateStatusUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(UpdateStatusUtil.class);

    private UpdateStatusUtil() {
    }

    public static void updateNbiIpsecs(List<NbiIpSec> nbiIpsecs, List<SbiNeIpSec> sbiNeTunnels, OperationType operType)
            throws ServiceException {
        for(NbiIpSec tmpNbiTunnel : nbiIpsecs) {
            String tmpIpsecId = tmpNbiTunnel.getUuid();
            String tmpSrcNeId = tmpNbiTunnel.getSrcNeId();
            String tmpDestNeId = tmpNbiTunnel.getDestNeId();

            SbiNeIpSec tmpSrcNeTunnel = null;
            SbiNeIpSec tmpDestNeTunnel = null;

            for(SbiNeIpSec tmpSbiNeTunnel : sbiNeTunnels) {
                if(tmpIpsecId.equals(tmpSbiNeTunnel.getConnectionServiceId())
                        && (tmpSrcNeId.equals(tmpSbiNeTunnel.getNeId()))) {
                    tmpSrcNeTunnel = tmpSbiNeTunnel;
                }
                if(tmpIpsecId.equals(tmpSbiNeTunnel.getConnectionServiceId())
                        && (tmpDestNeId.equals(tmpSbiNeTunnel.getNeId()))) {
                    tmpDestNeTunnel = tmpSbiNeTunnel;
                }

                if((null != tmpSrcNeTunnel) && (null != tmpDestNeTunnel)) {
                    break;
                }
            }

            if(null == tmpSrcNeTunnel || null == tmpDestNeTunnel) {
                LOGGER.error("updateNbiIpsecs: tmpSrcNeTunnel or tmpDestNeTunnel is null ");
                throw new InnerErrorServiceException("updateNbiIpsecs failed!");
            }

            updateDeployStatus(tmpNbiTunnel, tmpSrcNeTunnel, tmpDestNeTunnel);

            updateOperationStatus(tmpNbiTunnel, tmpSrcNeTunnel, tmpDestNeTunnel, operType);
        }

        InventoryDao<NbiIpSec> nbiTunnelDao = new InventoryDaoUtil<NbiIpSec>().getInventoryDao();
        nbiTunnelDao.update(NbiIpSec.class, nbiIpsecs, "deployStatus,operationStatus");
    }

    private static void updateDeployStatus(NbiIpSec tmpNbiTunnel, SbiNeIpSec tmpSrcNeTunnel,
            SbiNeIpSec tmpDestNeTunnel) {
        if(DeployStatus.DEPLOY.getName().equals(tmpSrcNeTunnel.getDeployStatus())
                && DeployStatus.DEPLOY.getName().equals(tmpDestNeTunnel.getDeployStatus())) {
            tmpNbiTunnel.setDeployStatus(DeployStatus.DEPLOY.getName());
        } else if(DeployStatus.UNDEPLOY.getName().equals(tmpSrcNeTunnel.getDeployStatus())
                && DeployStatus.UNDEPLOY.getName().equals(tmpDestNeTunnel.getDeployStatus())) {
            tmpNbiTunnel.setDeployStatus(DeployStatus.UNDEPLOY.getName());
        } else {
            tmpNbiTunnel.setDeployStatus(DeployStatus.PART_DEPLOY.getName());
        }
    }

    private static void updateOperationStatus(NbiIpSec tmpNbiTunnel, SbiNeIpSec tmpSrcNeTunnel,
            SbiNeIpSec tmpDestNeTunnel, OperationType operType) {
        if(OperationStatus.NORMAL.getName().equals(tmpSrcNeTunnel.getOperationStatus())
                && OperationStatus.NORMAL.getName().equals(tmpDestNeTunnel.getOperationStatus())) {
            tmpNbiTunnel.setOperationStatus(OperationStatus.NORMAL.getName());
            return;
        }

        switch(operType.getCommonName()) {
            case OperationType.CREATE_CONST:
                tmpNbiTunnel.setOperationStatus(OperationStatus.CREATE_EXCEPTION.getName());
                break;
            case OperationType.DEPLOY_CONST:
                tmpNbiTunnel.setOperationStatus(OperationStatus.DEPLOY_EXCEPTION.getName());
                break;
            case OperationType.UNDEPLOY_CONST:
                tmpNbiTunnel.setOperationStatus(OperationStatus.UNDEPLOY_EXCEPTION.getName());
                break;
            default:
                LOGGER.error("OperationType not support: ", operType.getCommonName());
                break;
        }

    }

}
