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

package org.openo.sdno.ipsecservice.service.update;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.openo.baseservice.remoteservice.exception.ServiceException;
import org.openo.baseservice.roa.util.restclient.RestfulParametes;
import org.openo.baseservice.roa.util.restclient.RestfulResponse;
import org.openo.sdno.exception.InnerErrorServiceException;
import org.openo.sdno.exception.ParameterServiceException;
import org.openo.sdno.framework.container.resthelper.RestfulProxy;
import org.openo.sdno.framework.container.util.JsonUtil;
import org.openo.sdno.ipsecservice.model.consts.AdapterUrlConst;
import org.openo.sdno.ipsecservice.model.enums.DeployStatus;
import org.openo.sdno.ipsecservice.model.enums.NeRoleType;
import org.openo.sdno.ipsecservice.model.enums.OperationStatus;
import org.openo.sdno.ipsecservice.util.site2dc.RestParameterUtil;
import org.openo.sdno.overlayvpn.consts.HttpCode;
import org.openo.sdno.overlayvpn.inventory.sdk.util.InventoryDaoUtil;
import org.openo.sdno.overlayvpn.model.v2.ipsec.NbiIpSec;
import org.openo.sdno.overlayvpn.model.v2.ipsec.SbiNeIpSec;
import org.openo.sdno.overlayvpn.util.FilterDataUtil;
import org.openo.sdno.overlayvpn.util.check.ValidationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <br/>
 * <p>
 * </p>
 * 
 * @author
 * @version SDNO 0.5 Jan 15, 2017
 */
public class UpdateService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UpdateService.class);

    private UpdateService() {
    }

    public static void doUpdate(List<NbiIpSec> nbiIpsecs) throws ServiceException {

        Set<String> uuids = checkData(nbiIpsecs);

        List<NbiIpSec> nbiDbData = getDbNbiData(uuids, nbiIpsecs);

        List<SbiNeIpSec> updateSbiData = new ArrayList<SbiNeIpSec>();

        getAndFillDbSbiData(nbiDbData, uuids, updateSbiData);

        boolean isUpdateSuccess = updateFs(updateSbiData);

        updateNbiStatus(nbiDbData, isUpdateSuccess);

        if(!isUpdateSuccess) {
            LOGGER.error("updateFs. failed");
            throw new InnerErrorServiceException("update ipsec failed!");
        }
    }

    private static boolean updateFs(List<SbiNeIpSec> updateSbiData) throws ServiceException {
        boolean isUpdateSuccess = true;
        for(SbiNeIpSec sbiNeIpSec : updateSbiData) {
            if(DeployStatus.UNDEPLOY.getName().equals(sbiNeIpSec.getDeployStatus())) {
                sbiNeIpSec.setOperationStatus(OperationStatus.NORMAL.getName());
                LOGGER.info("sbi data is undeploy, sbi uuid is " + sbiNeIpSec.getUuid());
                continue;
            }
            RestfulParametes restPara = RestParameterUtil.getUpdateIpsecParam(sbiNeIpSec);
            String externalIpSecId = sbiNeIpSec.getExternalIpSecId();
            RestfulResponse rsp = RestfulProxy.put(
                    AdapterUrlConst.ADAPTER_BASE_URL + AdapterUrlConst.CREATE_IPSECS_FS + "/" + externalIpSecId,
                    restPara);

            LOGGER.info(
                    "Fs ipsec update finish. httpcode: " + rsp.getStatus() + ", body is " + rsp.getResponseContent());

            if(HttpCode.isSucess(rsp.getStatus())) {
                sbiNeIpSec.setOperationStatus(OperationStatus.NORMAL.getName());
            } else {
                isUpdateSuccess = false;
                sbiNeIpSec.setOperationStatus(OperationStatus.UPDAT_EXCEPTION.getName());
                LOGGER.error("updateFs fail.fs response is: " + JsonUtil.toJson(rsp));

                break;
            }
        }

        new InventoryDaoUtil<SbiNeIpSec>().getInventoryDao().update(SbiNeIpSec.class, updateSbiData,
                "peerLanCidrs,operationStatus");
        return isUpdateSuccess;
    }

    private static Set<String> checkData(List<NbiIpSec> nbiIpsecs) throws ServiceException {
        ValidationUtil.validateModel(nbiIpsecs);

        Set<String> uuids = new HashSet<String>();
        for(NbiIpSec nbiIpsec : nbiIpsecs) {
            if(StringUtils.isEmpty(nbiIpsec.getUuid())) {
                LOGGER.error("Update failed. uuid is empty.");
                throw new ParameterServiceException("Update failed. uuid is empty!");
            }
            uuids.add(nbiIpsec.getUuid());
        }
        return uuids;
    }

    private static List<NbiIpSec> getDbNbiData(Set<String> uuids, List<NbiIpSec> nbiIpsecs) throws ServiceException {
        List<NbiIpSec> dbNbiIpsecs = new InventoryDaoUtil<NbiIpSec>().getInventoryDao()
                .batchQuery(NbiIpSec.class, new ArrayList<String>(uuids)).getData();

        for(NbiIpSec nbiIpSec : nbiIpsecs) {
            boolean isFind = false;
            for(NbiIpSec dbNbiIpsec : dbNbiIpsecs) {
                if(nbiIpSec.getUuid().equals(dbNbiIpsec.getUuid())) {
                    isFind = true;
                    if(!NeRoleType.VPC.getName().equals(dbNbiIpsec.getSrcNeRole())) {
                        dbNbiIpsec.setSourceLanCidrs(nbiIpSec.getSourceLanCidrs());
                    }
                    if(!NeRoleType.VPC.getName().equals(dbNbiIpsec.getDestNeRole())) {
                        dbNbiIpsec.setDestLanCidrs(nbiIpSec.getDestLanCidrs());
                    }
                    break;
                }
            }
            if(!isFind) {
                LOGGER.error("update fail.nbi data not found. id: ", nbiIpSec.getUuid());
                throw new InnerErrorServiceException("update fail.nbi data not found!");
            }
        }

        return dbNbiIpsecs;
    }

    private static List<SbiNeIpSec> getAndFillDbSbiData(List<NbiIpSec> nbiDbData, Set<String> uuids,
            List<SbiNeIpSec> updateSbiData) throws ServiceException {

        List<SbiNeIpSec> dbSbiNeIpsecs = new InventoryDaoUtil<SbiNeIpSec>().getInventoryDao()
                .batchQuery(SbiNeIpSec.class, FilterDataUtil.getFilterData("connectionServiceId", uuids)).getData();

        for(SbiNeIpSec sbiNeIpSec : dbSbiNeIpsecs) {
            if(!NeRoleType.VPC.getName().equals(sbiNeIpSec.getLocalNeRole())) {
                continue;
            }

            NbiIpSec tmpNbiIpsec = null;
            for(NbiIpSec dbNbiIpsec : nbiDbData) {
                if(sbiNeIpSec.getConnectionServiceId().equals(dbNbiIpsec.getUuid())) {
                    tmpNbiIpsec = dbNbiIpsec;
                    break;
                }
            }

            if(null == tmpNbiIpsec) {
                LOGGER.error("update fail.sbi data not found in nbi data. sbi uuid: ", sbiNeIpSec.getUuid());
                throw new InnerErrorServiceException("update fail.sbi data not found in nbi data!");
            }

            if(sbiNeIpSec.getNeId().equals(tmpNbiIpsec.getSrcNeId())) {
                sbiNeIpSec.setPeerLanCidrs(tmpNbiIpsec.getDestLanCidrs());
            }
            if(sbiNeIpSec.getNeId().equals(tmpNbiIpsec.getDestNeId())) {
                sbiNeIpSec.setPeerLanCidrs(tmpNbiIpsec.getSourceLanCidrs());
            }
            updateSbiData.add(sbiNeIpSec);
        }
        return dbSbiNeIpsecs;
    }

    private static void updateNbiStatus(List<NbiIpSec> nbiDbData, boolean isUpdateSuccess) throws ServiceException {
        for(NbiIpSec nbiIpSec : nbiDbData) {
            if(isUpdateSuccess) {
                nbiIpSec.setOperationStatus(OperationStatus.NORMAL.getName());
            } else {
                nbiIpSec.setOperationStatus(OperationStatus.UPDAT_EXCEPTION.getName());
            }
        }

        new InventoryDaoUtil<NbiIpSec>().getInventoryDao().update(NbiIpSec.class, nbiDbData,
                "sourceLanCidrs,destLanCidrs,operationStatus");
    }
}
