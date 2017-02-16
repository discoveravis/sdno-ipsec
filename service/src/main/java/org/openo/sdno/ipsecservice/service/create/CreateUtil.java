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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.collections.CollectionUtils;
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
import org.openo.sdno.ipsecservice.util.site2dc.RestParameterUtil;
import org.openo.sdno.overlayvpn.brs.model.NetworkElementMO;
import org.openo.sdno.overlayvpn.consts.HttpCode;
import org.openo.sdno.overlayvpn.inventory.sdk.util.InventoryDaoUtil;
import org.openo.sdno.overlayvpn.model.v2.ipsec.NbiIpSec;
import org.openo.sdno.overlayvpn.model.v2.ipsec.SbiNeIpSec;
import org.openo.sdno.overlayvpn.model.v2.result.ResultRsp;
import org.openo.sdno.overlayvpn.security.authentication.HttpContext;
import org.openo.sdno.rest.ResponseUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Create ipsec utils.<br/>
 * 
 * @author
 * @version SDNO 0.5 Jan 9, 2017
 */
public class CreateUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(CreateUtil.class);

    private CreateUtil() {
    }

    /**
     * Create ipsec in Fs.<br/>
     * 
     * @param sbiNeIpsecList List of Sbi ipsec objects
     * @param fsCreateResult Result object of creation
     * @throws ServiceException when create failed
     * @since SDNO 0.5
     */
    public static void createByFs(List<SbiNeIpSec> sbiNeIpsecList, ResultRsp<SbiNeIpSec> fsCreateResult)
            throws ServiceException {

        ResultRsp<SbiNeIpSec> createRst = createSbiNeIpsecToFs(sbiNeIpsecList);
        if(!CollectionUtils.isEmpty(createRst.getSuccessed())) {
            fsCreateResult.getSuccessed().addAll(createRst.getSuccessed());
        }
        if(!CollectionUtils.isEmpty(createRst.getFail())) {
            fsCreateResult.getFail().addAll(createRst.getFail());
        }
    }

    /**
     * Fill map of device id and controller id.<br/>
     * 
     * @param deviceIdToNeMap Map of device id and ne
     * @param deviceIdToCtrollMap Map of device id and controller id
     * @param neIdToControllerMapRs Map of ne id and controller id
     * @since SDNO 0.5
     */
    public static void fillDeviceIdToCtrlMap(Map<String, NetworkElementMO> deviceIdToNeMap,
            Map<String, String> deviceIdToCtrollMap, Map<String, String> neIdToControllerMapRs) {
        for(Entry<String, NetworkElementMO> entry : deviceIdToNeMap.entrySet()) {
            String ctrl = neIdToControllerMapRs.get(entry.getValue().getId());
            if(null == ctrl) {
                continue;
            }
            deviceIdToCtrollMap.put(entry.getKey(), ctrl);
        }
    }

    /**
     * Fill map of device id and port name.<br/>
     * 
     * @param neIdPortNameToPortNameMap Map of ne id and port name
     * @param deviceIdPortNameToPortNameMap Map of device id and port name
     * @param deviceIdToNeMap Map of device id and ne
     * @since SDNO 0.5
     */
    public static void fillDeviceIdToPortMap(Map<String, String> neIdPortNameToPortNameMap,
            Map<String, String> deviceIdPortNameToPortNameMap, Map<String, NetworkElementMO> deviceIdToNeMap) {
        for(Entry<String, String> neEntry : neIdPortNameToPortNameMap.entrySet()) {
            for(Entry<String, NetworkElementMO> entry : deviceIdToNeMap.entrySet()) {
                if(neEntry.getKey().contains(entry.getValue().getId())) {
                    deviceIdPortNameToPortNameMap.put(
                            neEntry.getKey().replace(entry.getValue().getId(), entry.getKey()), neEntry.getValue());
                    break;
                }
            }
        }

    }

    /**
     * Fill Sbi ipsecs of Ac by response of Fs.<br/>
     * 
     * @param acSbiNeIpsecList List of Ac Sbi ipsec objects
     * @param fsCreateResult Result object of creation
     * @throws ServiceException when fill data failed
     * @since SDNO 0.5
     */
    public static void fillDataByFsRsp(List<SbiNeIpSec> acSbiNeIpsecList, ResultRsp<SbiNeIpSec> fsCreateResult)
            throws ServiceException {

        List<SbiNeIpSec> updateSbiNeIpSecList = new ArrayList<SbiNeIpSec>();
        List<SbiNeIpSec> updateSbiNeIpSecByFsList = fsCreateResult.getSuccessed();

        for(SbiNeIpSec fsSbiNeIpSec : fsCreateResult.getSuccessed()) {
            fillAcByFs(acSbiNeIpsecList, updateSbiNeIpSecList, fsSbiNeIpSec);
        }

        new InventoryDaoUtil<SbiNeIpSec>().getInventoryDao().update(SbiNeIpSec.class, updateSbiNeIpSecList,
                "peerAddress, peerLanCidrs");
        new InventoryDaoUtil<SbiNeIpSec>().getInventoryDao().update(SbiNeIpSec.class, updateSbiNeIpSecByFsList,
                "sourceAddress, sourceLanCidrs,additionalInfo");
    }

    /**
     * Fill vpc Lan cidr in Nbi ipsec objects.<br/>
     * 
     * @param nbiIpsecs List of Nbi ipsec objects
     * @param fsCreateResult Result object of creation
     * @throws ServiceException when fill data failed
     * @since SDNO 0.5
     */
    public static void fillVpcLanCidrToNbi(List<NbiIpSec> nbiIpsecs, ResultRsp<SbiNeIpSec> fsCreateResult)
            throws ServiceException {
        if(CollectionUtils.isEmpty(fsCreateResult.getSuccessed()) || CollectionUtils.isEmpty(nbiIpsecs)) {
            return;
        }

        List<NbiIpSec> updateSrcLanCidr = new ArrayList<NbiIpSec>();
        List<NbiIpSec> updateDestLanCidr = new ArrayList<NbiIpSec>();
        for(NbiIpSec nbiIpsec : nbiIpsecs) {
            for(SbiNeIpSec sbiNeIpSec : fsCreateResult.getSuccessed()) {
                if(nbiIpsec.getUuid().equals(sbiNeIpSec.getConnectionServiceId())) {
                    if(nbiIpsec.getSrcNeId().equals(sbiNeIpSec.getNeId())
                            && nbiIpsec.getDestNeId().equals(sbiNeIpSec.getPeerNeId())) {
                        nbiIpsec.setSourceLanCidrs(sbiNeIpSec.getSourceLanCidrs());
                        updateSrcLanCidr.add(nbiIpsec);
                    } else {
                        nbiIpsec.setDestLanCidrs(sbiNeIpSec.getSourceLanCidrs());
                        updateDestLanCidr.add(nbiIpsec);
                    }
                    break;
                }
            }
        }

        new InventoryDaoUtil<NbiIpSec>().getInventoryDao().update(NbiIpSec.class, updateSrcLanCidr, "sourceLanCidrs");
        new InventoryDaoUtil<NbiIpSec>().getInventoryDao().update(NbiIpSec.class, updateDestLanCidr, "destLanCidrs");

    }

    /**
     * Set Ac Sbi ipsec object's operation status.<br/>
     * 
     * @param sbiNeTunnels List of Sbi ipsec objects
     * @throws ServiceException when set status failed
     * @since SDNO 0.5
     */
    public static void setAcSbiIpSecOperStatus(List<SbiNeIpSec> sbiNeTunnels) throws ServiceException {
        for(SbiNeIpSec tmpSbiIpsec : sbiNeTunnels) {
            if(NeRoleType.VPC.getName().equals(tmpSbiIpsec.getLocalNeRole())
                    || DeployStatus.UNDEPLOY.getName().equals(tmpSbiIpsec.getDeployStatus())) {
                LOGGER.error("setAcSbiIpSecOperStatus fail. NeRoleType:" + tmpSbiIpsec.getLocalNeRole()
                        + ", DeployStatus:" + tmpSbiIpsec.getDeployStatus());
                throw new InnerErrorServiceException("create ipsec failed!");
            }
            tmpSbiIpsec.setOperationStatus(OperationStatus.CREATING.getName());
        }
    }

    /**
     * Create ipsec in Ac.<br/>
     * 
     * @param sbiNeIpsecList List of Sbi ipsec objects
     * @param AcCreateResult Result object of creation
     * @throws ServiceException when create failed
     * @since SDNO 0.5
     */
    public static void createByAc(List<SbiNeIpSec> sbiNeIpsecList, ResultRsp<SbiNeIpSec> AcCreateResult)
            throws ServiceException {
        ResultRsp<SbiNeIpSec> createRst = createSbiNeIpsecToAc(sbiNeIpsecList);
        if(!CollectionUtils.isEmpty(createRst.getSuccessed())) {
            AcCreateResult.getSuccessed().addAll(createRst.getSuccessed());
        }
        if(!CollectionUtils.isEmpty(createRst.getFail())) {
            AcCreateResult.getFail().addAll(createRst.getFail());
        }
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

    private static ResultRsp<SbiNeIpSec> createSbiNeIpsecToFs(List<SbiNeIpSec> sbiNeIpsecList) throws ServiceException {
        LOGGER.info("begin create by fs.");

        RestfulParametes restPara = RestParameterUtil.getCreateGreTunnelParam(sbiNeIpsecList);
        RestfulResponse rsp =
                RestfulProxy.post(AdapterUrlConst.ADAPTER_BASE_URL + AdapterUrlConst.CREATE_IPSECS_FS, restPara);

        LOGGER.info("Fs ipsec create finish. httpcode: " + rsp.getStatus() + ", body is " + rsp.getResponseContent());

        try {
            String rspContent = ResponseUtils.transferResponse(rsp);
            return JsonUtil.fromJson(rspContent, new TypeReference<ResultRsp<SbiNeIpSec>>() {});
        } catch(ServiceException e) {
            LOGGER.error("createSbiNeIpsecToFs exception. e: ", e);
            throw new InnerErrorServiceException("createSbiNeIpsecToFs failed!");
        }
    }

    private static ResultRsp<SbiNeIpSec> createSbiNeIpsecToAc(List<SbiNeIpSec> sbiNeIpsecList) throws ServiceException {
        LOGGER.info("begin create by fs.");
        String ctrlId = sbiNeIpsecList.get(0).getControllerId();
        RestfulParametes restPara = new RestfulParametes();
        String strJsonReq = JsonUtil.toJson(sbiNeIpsecList);
        restPara.putHttpContextHeader(HttpContext.CONTENT_TYPE_HEADER, HttpContext.MEDIA_TYPE_JSON);
        restPara.putHttpContextHeader("X-Driver-Parameter", "extSysID=" + ctrlId);
        restPara.setRawData(strJsonReq);

        RestfulResponse rsp =
                RestfulProxy.post(AdapterUrlConst.ADAPTER_BASE_URL + AdapterUrlConst.BATCH_CREATE_IPSECS, restPara);

        LOGGER.info("Ac ipsec create finish. httpcode: " + rsp.getStatus() + ", body is " + rsp.getResponseContent());

        if(HttpCode.isSucess(rsp.getStatus())) {
            try {
                String rspContent = ResponseUtils.transferResponse(rsp);
                return JsonUtil.fromJson(rspContent, new TypeReference<ResultRsp<SbiNeIpSec>>() {});
            } catch(ServiceException e) {
                LOGGER.error("createSbiNeIpsecToAc exception. e: ", e);
                throw new InnerErrorServiceException("createSbiNeIpsecToAc failed!");
            }
        } else {
            LOGGER.error("createSbiNeIpsecToAc fail.  response is: " + JsonUtil.toJson(rsp));
            throw new InnerErrorServiceException("createSbiNeIpsecToAc failed!");
        }

    }
}
