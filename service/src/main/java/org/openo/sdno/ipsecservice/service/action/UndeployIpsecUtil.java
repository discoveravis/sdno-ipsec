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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.codehaus.jackson.type.TypeReference;
import org.openo.baseservice.remoteservice.exception.ServiceException;
import org.openo.baseservice.roa.util.restclient.RestfulParametes;
import org.openo.baseservice.roa.util.restclient.RestfulResponse;
import org.openo.sdno.exception.InnerErrorServiceException;
import org.openo.sdno.framework.container.resthelper.RestfulProxy;
import org.openo.sdno.framework.container.util.JsonUtil;
import org.openo.sdno.ipsecservice.model.consts.AdapterUrlConst;
import org.openo.sdno.ipsecservice.model.enums.DeployStatus;
import org.openo.sdno.ipsecservice.model.enums.NeRoleType;
import org.openo.sdno.ipsecservice.model.enums.OperationStatus;
import org.openo.sdno.ipsecservice.model.enums.OperationType;
import org.openo.sdno.ipsecservice.util.site2dc.ActionUtil;
import org.openo.sdno.ipsecservice.util.site2dc.MergeRspUtil;
import org.openo.sdno.overlayvpn.consts.HttpCode;
import org.openo.sdno.overlayvpn.inventory.sdk.util.InventoryDaoUtil;
import org.openo.sdno.overlayvpn.model.v2.ipsec.NbiIpSec;
import org.openo.sdno.overlayvpn.model.v2.ipsec.SbiIkePolicy;
import org.openo.sdno.overlayvpn.model.v2.ipsec.SbiIpSecPolicy;
import org.openo.sdno.overlayvpn.model.v2.ipsec.SbiNeIpSec;
import org.openo.sdno.overlayvpn.model.v2.result.ResultRsp;
import org.openo.sdno.overlayvpn.result.FailData;
import org.openo.sdno.overlayvpn.security.authentication.HttpContext;
import org.openo.sdno.overlayvpn.util.FilterDataUtil;
import org.openo.sdno.rest.ResponseUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

/**
 * <br/>
 * <p>
 * </p>
 * 
 * @author
 * @version SDNO 0.5 Jan 15, 2017
 */
public class UndeployIpsecUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(UndeployIpsecUtil.class);

    private UndeployIpsecUtil() {

    }

    /**
     * <br/>
     * 
     * @param undeploy
     * @return
     * @throws ServiceException
     * @since SDNO 0.5
     */
    public static List<String> doUndeploy(List<String> ipsecIds) throws ServiceException {
        List<NbiIpSec> nbiIpsecs = new ArrayList<NbiIpSec>();
        List<SbiNeIpSec> sbiNeIpsecs = ActionUtil.checkDataAndQueryIpsecInDb(ipsecIds, nbiIpsecs);

        List<SbiNeIpSec> acActiveNeIpsecs = new ArrayList<SbiNeIpSec>();
        List<SbiNeIpSec> fsActiveNeIpsecs = new ArrayList<SbiNeIpSec>();
        List<SbiNeIpSec> inactiveNeIpsecs = new ArrayList<SbiNeIpSec>();
        checkSbiStatus(fsActiveNeIpsecs, acActiveNeIpsecs, inactiveNeIpsecs, sbiNeIpsecs);
        if(CollectionUtils.isEmpty(acActiveNeIpsecs) && CollectionUtils.isEmpty(fsActiveNeIpsecs)) {
            return ipsecIds;
        }

        for(SbiNeIpSec sbiNeIpsec : acActiveNeIpsecs) {
            sbiNeIpsec.setOperationStatus(OperationStatus.UNDEPLOYING.getName());
        }
        for(SbiNeIpSec sbiNeIpsec : fsActiveNeIpsecs) {
            sbiNeIpsec.setOperationStatus(OperationStatus.UNDEPLOYING.getName());
        }

        ResultRsp<SbiNeIpSec> acUndeployRsp = new ResultRsp<SbiNeIpSec>();
        acUndeployRsp.setSuccessed(new ArrayList<SbiNeIpSec>());
        acUndeployRsp.setFail(new ArrayList<FailData<SbiNeIpSec>>());
        ResultRsp<SbiNeIpSec> fsUndeployRsp = new ResultRsp<SbiNeIpSec>();
        fsUndeployRsp.setSuccessed(new ArrayList<SbiNeIpSec>());
        fsUndeployRsp.setFail(new ArrayList<FailData<SbiNeIpSec>>());

        undeployByAc(acActiveNeIpsecs, acUndeployRsp);
        LOGGER.info("undeployByAc. success num = ", acUndeployRsp.getSuccessed().size());

        undeployByFs(fsActiveNeIpsecs, fsUndeployRsp);
        LOGGER.error("undeployByFs. success num = ", fsUndeployRsp.getSuccessed().size());

        ResultRsp<NbiIpSec> undeployRsp = MergeRspUtil.mergeAllCreateRsp(nbiIpsecs, sbiNeIpsecs, acUndeployRsp,
                fsUndeployRsp, OperationType.UNDEPLOY);

        if(undeployRsp.getSuccessed().size() == ipsecIds.size()) {
            LOGGER.info("ipsec undeploy success! ");
            return ipsecIds;
        }

        LOGGER.error("undeploy failed. success num = ", undeployRsp.getSuccessed().size());
        throw new InnerErrorServiceException("undeploy failed!");
    }

    private static void undeployByFs(List<SbiNeIpSec> fsActiveNeIpsecs, ResultRsp<SbiNeIpSec> fsUndeployRsp)
            throws ServiceException {
        if(CollectionUtils.isEmpty(fsActiveNeIpsecs)) {
            return;
        }

        List<SbiNeIpSec> delSbiData = new ArrayList<SbiNeIpSec>();
        for(SbiNeIpSec sbiNeIpsec : fsActiveNeIpsecs) {
            String filer = FilterDataUtil.getFilterData("sbiServiceId", Arrays.asList(sbiNeIpsec.getUuid()));
            List<SbiIkePolicy> ikePolicys = new InventoryDaoUtil<SbiIkePolicy>().getInventoryDao()
                    .batchQuery(SbiIkePolicy.class, filer).getData();
            List<SbiIpSecPolicy> ipSecPolicys = new InventoryDaoUtil<SbiIpSecPolicy>().getInventoryDao()
                    .batchQuery(SbiIpSecPolicy.class, filer).getData();

            if(CollectionUtils.isEmpty(ikePolicys) || CollectionUtils.isEmpty(ipSecPolicys)) {
                LOGGER.error("undeployByFs failed. ikePoicys or ipSecPoicys is empty!");
                throw new InnerErrorServiceException("undeployByFs failed!");
            }
            sbiNeIpsec.setIkePolicy(ikePolicys.get(0));
            sbiNeIpsec.setIpSecPolicy(ipSecPolicys.get(0));

            delSbiData.add(sbiNeIpsec);
        }

        RestfulParametes restPara = new RestfulParametes();
        restPara.putHttpContextHeader(HttpContext.CONTENT_TYPE_HEADER, HttpContext.MEDIA_TYPE_JSON);
        String ctrlId = fsActiveNeIpsecs.get(0).getControllerId();
        restPara.putHttpContextHeader("X-Driver-Parameter", "extSysID=" + ctrlId);
        restPara.setRawData(JsonUtil.toJson(delSbiData));

        String url = AdapterUrlConst.ADAPTER_BASE_URL + AdapterUrlConst.UNDEPLOY_IPSECS_FS;

        RestfulResponse rsp = RestfulProxy.post(url, restPara);
        LOGGER.info("fs ipsec undeploy finish. httpcode: " + rsp.getStatus() + ", body is " + rsp.getResponseContent());

        if(HttpCode.isSucess(rsp.getStatus())) {
            try {
                String rspContent = ResponseUtils.transferResponse(rsp);
                ResultRsp<SbiNeIpSec> restResult =
                        JsonUtil.fromJson(rspContent, new TypeReference<ResultRsp<SbiNeIpSec>>() {});
                fsUndeployRsp.getSuccessed().addAll(restResult.getSuccessed());
                return;
            } catch(ServiceException e) {
                LOGGER.error("undeployByFs exception. e: ", e);
                throw new InnerErrorServiceException("undeployByFs failed!");
            }
        } else {
            LOGGER.error("undeployByFs fail.  response is: " + JsonUtil.toJson(rsp));
            throw new InnerErrorServiceException("undeployByFs failed!");
        }
    }

    private static void undeployByAc(List<SbiNeIpSec> acActiveNeIpsecs, ResultRsp<SbiNeIpSec> acUndeployRsp)
            throws ServiceException {
        if(CollectionUtils.isEmpty(acActiveNeIpsecs)) {
            return;
        }
        String ctrlId = acActiveNeIpsecs.get(0).getControllerId();
        String extIpsecId = acActiveNeIpsecs.get(0).getExternalIpSecId();
        String deviceId = acActiveNeIpsecs.get(0).getDeviceId();

        RestfulParametes restPara = new RestfulParametes();
        restPara.putHttpContextHeader(HttpContext.CONTENT_TYPE_HEADER, HttpContext.MEDIA_TYPE_JSON);
        restPara.putHttpContextHeader("X-Driver-Parameter", "extSysID=" + ctrlId);
        restPara.setRawData(JsonUtil.toJson(acActiveNeIpsecs));

        String url = AdapterUrlConst.ADAPTER_BASE_URL
                + MessageFormat.format(AdapterUrlConst.BATCH_DELETE_DEVICE_IPSECS, deviceId, extIpsecId);

        RestfulResponse rsp = RestfulProxy.post(url, restPara);
        LOGGER.info("Ac ipsec undeploy finish. httpcode: " + rsp.getStatus() + ", body is " + rsp.getResponseContent());

        if(HttpCode.isSucess(rsp.getStatus())) {
            try {
                String rspContent = ResponseUtils.transferResponse(rsp);
                ResultRsp<SbiNeIpSec> restResult =
                        JsonUtil.fromJson(rspContent, new TypeReference<ResultRsp<SbiNeIpSec>>() {});

                acUndeployRsp.getSuccessed().addAll(restResult.getSuccessed());
                return;
            } catch(ServiceException e) {
                LOGGER.error("undeployByAc exception. e: ", e);
                throw new InnerErrorServiceException("undeployByAc failed!");
            }
        } else {
            LOGGER.error("undeployByAc fail.  response is: " + JsonUtil.toJson(rsp));
            throw new InnerErrorServiceException("undeployByAc failed!");
        }
    }

    private static void checkSbiStatus(List<SbiNeIpSec> fsActiveNeIpsecs, List<SbiNeIpSec> acActiveNeIpsecs,
            List<SbiNeIpSec> inactiveNeIpsecs, List<SbiNeIpSec> sbiNeIpsecs) {
        for(SbiNeIpSec sbiNeIpSec : sbiNeIpsecs) {
            if(DeployStatus.UNDEPLOY.getName().equals(sbiNeIpSec.getDeployStatus())) {
                inactiveNeIpsecs.add(sbiNeIpSec);
            } else if(NeRoleType.VPC.getName().equals(sbiNeIpSec.getLocalNeRole())) {
                fsActiveNeIpsecs.add(sbiNeIpSec);
            } else {
                acActiveNeIpsecs.add(sbiNeIpSec);
            }
        }

    }

}
