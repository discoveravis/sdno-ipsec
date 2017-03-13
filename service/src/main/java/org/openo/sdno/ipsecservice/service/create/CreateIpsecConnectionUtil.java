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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.collections.CollectionUtils;
import org.openo.baseservice.remoteservice.exception.ServiceException;
import org.openo.sdno.exception.InnerErrorServiceException;
import org.openo.sdno.exception.ParameterServiceException;
import org.openo.sdno.framework.container.util.JsonUtil;
import org.openo.sdno.framework.container.util.UuidUtils;
import org.openo.sdno.ipsecservice.model.enums.NeRoleType;
import org.openo.sdno.ipsecservice.model.enums.OperationStatus;
import org.openo.sdno.ipsecservice.model.enums.OperationType;
import org.openo.sdno.ipsecservice.resource.AllocateResUtil;
import org.openo.sdno.ipsecservice.resource.id.IpSecIdUtil;
import org.openo.sdno.ipsecservice.resource.port.PortUtil;
import org.openo.sdno.ipsecservice.service.db.SbiIpsecDbOperUtil;
import org.openo.sdno.ipsecservice.util.site2dc.MergeRspUtil;
import org.openo.sdno.ipsecservice.util.site2dc.NbiModelToSbiModel;
import org.openo.sdno.ipsecservice.util.site2dc.checkres.CheckControllerUtil;
import org.openo.sdno.ipsecservice.util.site2dc.checkres.CheckIpsecConCreateUtil;
import org.openo.sdno.ipsecservice.util.site2dc.checkres.CheckNeUtil;
import org.openo.sdno.overlayvpn.brs.model.NetworkElementMO;
import org.openo.sdno.overlayvpn.dao.common.InventoryDao;
import org.openo.sdno.overlayvpn.inventory.sdk.util.InventoryDaoUtil;
import org.openo.sdno.overlayvpn.model.v2.ipsec.NbiIpSec;
import org.openo.sdno.overlayvpn.model.v2.ipsec.SbiIp;
import org.openo.sdno.overlayvpn.model.v2.ipsec.SbiNeIpSec;
import org.openo.sdno.overlayvpn.model.v2.result.ResultRsp;
import org.openo.sdno.overlayvpn.result.FailData;
import org.openo.sdno.overlayvpn.util.check.ValidationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

/**
 * Create ipsec connection util.<br/>
 * 
 * @author
 * @version SDNO 0.5 Jan 7, 2017
 */
public class CreateIpsecConnectionUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(CreateIpsecConnectionUtil.class);

    private CreateIpsecConnectionUtil() {
    }

    /**
     * Create ipsec connections.<br/>
     * 
     * @param req HttpServletRequest Object
     * @param nbiIpsecs List of ipsec to be created
     * @return result of creation
     * @throws ServiceException when create failed
     * @since SDNO 0.5
     */
    public static ResultRsp<NbiIpSec> doCreate(List<NbiIpSec> nbiIpsecs) throws ServiceException {
        long beginTime = System.currentTimeMillis();

        Map<String, String> neIdPortNameToPortNameMap = new ConcurrentHashMap<>();
        Map<String, String> deviceIdPortNameToPortNameMap = new ConcurrentHashMap<>();
        Map<String, NetworkElementMO> deviceIdToNeMap = new ConcurrentHashMap<>();
        Map<String, String> deviceIdToCtrollMap = new ConcurrentHashMap<>();
        Map<String, String> neIdToRulePortNameMap = new ConcurrentHashMap<>();

        checkData(nbiIpsecs, neIdPortNameToPortNameMap, deviceIdToNeMap, neIdToRulePortNameMap);

        LOGGER.info("Create Ipsec tunnel. CheckData finished. cost time = " + (System.currentTimeMillis() - beginTime));

        // Check controller and query port IP
        Map<String, SbiIp> deviceIdPortNameToIpMap = checkController(neIdPortNameToPortNameMap,
                deviceIdPortNameToPortNameMap, deviceIdToNeMap, deviceIdToCtrollMap);

        // S2DC does not need to fill ruleIp. Fill Ip data here
        fillTunnelData(nbiIpsecs, deviceIdToNeMap, deviceIdPortNameToIpMap);

        return createInDbAndAc(nbiIpsecs, deviceIdToCtrollMap);

    }

    /**
     * Process input Nbi ipsec objects, generate uuid and fill policy.<br/>
     * 
     * @param ipsecs List of ipsec
     * @return List of ipsec objects
     * @throws ServiceException when process failed
     * @since SDNO 0.5
     */
    public static List<NbiIpSec> getGreTunnelList(List<NbiIpSec> ipsecs) throws ServiceException {

        if(CollectionUtils.isEmpty(ipsecs)) {
            LOGGER.error("input ipsecs is empty");
            throw new ParameterServiceException("Input ipsecs is empty");
        }

        for(NbiIpSec ipsec : ipsecs) {
            if(!StringUtils.hasLength(ipsec.getUuid())) {
                ipsec.setUuid(UuidUtils.createUuid());
            }
        }

        FillDataUtil.fillPolicy(ipsecs);
        return ipsecs;

    }

    private static Set<String> checkData(List<NbiIpSec> ipsecCons, Map<String, String> neIdPortNameToPortNameMap,
            Map<String, NetworkElementMO> deviceIdToNeMap, Map<String, String> neIdToRulePortNameMap)
            throws ServiceException {

        Set<String> neIdSet = new HashSet<>();
        for(NbiIpSec temGreTunnel : ipsecCons) {
            nbiIpSecBasicCheck(temGreTunnel);

            String srcNeId = FillDataUtil.fillSrcNeData(neIdPortNameToPortNameMap, neIdSet, temGreTunnel);
            String destNeId = FillDataUtil.fillDestNeData(neIdPortNameToPortNameMap, neIdSet, temGreTunnel);

            if(StringUtils.hasLength(temGreTunnel.getRuleSrcPortName())) {
                neIdToRulePortNameMap.put(srcNeId, temGreTunnel.getRuleSrcPortName());
            }

            if(StringUtils.hasLength(temGreTunnel.getRuleDestPortName())) {
                neIdToRulePortNameMap.put(destNeId, temGreTunnel.getRuleDestPortName());
            }
        }

        CheckNeUtil.checkNesResource(neIdSet, deviceIdToNeMap);
        return neIdSet;

    }

    private static void nbiIpSecBasicCheck(NbiIpSec ipsecCon) throws ServiceException {

        try {
            ValidationUtil.validateModel(ipsecCon);
            if(StringUtils.hasLength(ipsecCon.getUuid())) {
                ipsecCon.checkUuid();
            }
        } catch(ServiceException e) {
            LOGGER.error("param error!", e);
            throw new ParameterServiceException("NbiIpSec validateModel fail, param error!");
        }

        if(ipsecCon.getSrcNeId().equals(ipsecCon.getDestNeId())) {
            LOGGER.error("param error! SrcNeId equal with DestNeId.");
            throw new ParameterServiceException("Check NbiIpSec ne id data fail, param error!");
        }

    }

    private static Map<String, SbiIp> checkController(Map<String, String> neIdPortNameToPortNameMap,
            Map<String, String> deviceIdPortNameToPortNameMap, Map<String, NetworkElementMO> deviceIdToNeMap,
            Map<String, String> deviceIdToCtrollMap) throws ServiceException {
        Map<String, String> neIdToControllerMapRs =
                CheckControllerUtil.testCtrlConnection(new ArrayList<NetworkElementMO>(deviceIdToNeMap.values()));

        CreateUtil.fillDeviceIdToCtrlMap(deviceIdToNeMap, deviceIdToCtrollMap, neIdToControllerMapRs);
        CreateUtil.fillDeviceIdToPortMap(neIdPortNameToPortNameMap, deviceIdPortNameToPortNameMap, deviceIdToNeMap);

        return PortUtil.getPortIpMap(deviceIdToNeMap, deviceIdPortNameToPortNameMap, deviceIdToCtrollMap);
    }

    private static void fillTunnelData(List<NbiIpSec> ipsecTunnls, Map<String, NetworkElementMO> deviceIdToNeMap,
            Map<String, SbiIp> deviceIdPortNameToIpMap) {

        for(NbiIpSec tempGreTunnel : ipsecTunnls) {
            String tempSrcNeId = tempGreTunnel.getSrcNeId();
            String tempDestNeId = tempGreTunnel.getDestNeId();
            String tempSrcDeviceId = null;
            String tempDestDeviceId = null;

            for(Entry<String, NetworkElementMO> tempEntry : deviceIdToNeMap.entrySet()) {
                if(tempSrcNeId.equals(tempEntry.getValue().getId())) {
                    tempSrcDeviceId = tempEntry.getKey();
                    continue;
                }

                if(tempDestNeId.equals(tempEntry.getValue().getId())) {
                    tempDestDeviceId = tempEntry.getKey();
                    continue;
                }

                if((null != tempSrcDeviceId) && (null != tempDestDeviceId)) {
                    break;
                }
            }

            FillDataUtil.fillDeviceIdAndIp(deviceIdPortNameToIpMap, tempGreTunnel, tempSrcDeviceId, tempDestDeviceId);

        }
    }

    private static ResultRsp<NbiIpSec> createInDbAndAc(List<NbiIpSec> nbiIpsecs,
            Map<String, String> deviceIdToCtrollMap) throws ServiceException {

        List<NbiIpSec> insertDataList = getIdAndInsertDb(nbiIpsecs);

        LOGGER.info("ipsec insertDb complete. ");

        List<SbiNeIpSec> acSbiNeIpsecList = new ArrayList<>();
        List<SbiNeIpSec> vpcSbiNeIpsecList = new ArrayList<>();
        List<SbiNeIpSec> sbiNeIpsecList =
                nbiMdelToSbiModel(insertDataList, acSbiNeIpsecList, vpcSbiNeIpsecList, deviceIdToCtrollMap);
        LOGGER.info("ipsec nbiMdelToSbiModel complete. ");

        ResultRsp<SbiNeIpSec> fsCreateRsp = new ResultRsp<>();
        fsCreateRsp.setSuccessed(new ArrayList<SbiNeIpSec>());
        fsCreateRsp.setFail(new ArrayList<FailData<SbiNeIpSec>>());

        ResultRsp<SbiNeIpSec> acCreateRsp = new ResultRsp<>();
        acCreateRsp.setSuccessed(new ArrayList<SbiNeIpSec>());
        acCreateRsp.setFail(new ArrayList<FailData<SbiNeIpSec>>());

        CheckIpsecConCreateUtil.checkIpsecConnIsCreated(acSbiNeIpsecList, vpcSbiNeIpsecList, fsCreateRsp.getSuccessed(),
                acCreateRsp.getSuccessed());
        LOGGER.info("ipsec checkIpsecConnIsCreated complete. ");

        CreateUtil.createByFs(vpcSbiNeIpsecList, fsCreateRsp);
        if(!CollectionUtils.isEmpty(fsCreateRsp.getFail())) {
            LOGGER.error("create fs failed. fail num = ", fsCreateRsp.getFail().size());
            throw new InnerErrorServiceException("create failed!");
        }
        LOGGER.info("ipsec createByFs complete. ");

        CreateUtil.fillDataByFsRsp(acSbiNeIpsecList, fsCreateRsp);

        CreateUtil.fillVpcLanCidrAndPortIpToNbi(insertDataList, fsCreateRsp);

        CreateUtil.setAcSbiIpSecOperStatus(acSbiNeIpsecList);

        CreateUtil.createByAc(acSbiNeIpsecList, acCreateRsp);
        if(!CollectionUtils.isEmpty(acCreateRsp.getFail())) {
            LOGGER.error("create ac failed. fail num = ", acCreateRsp.getFail().size());
            throw new InnerErrorServiceException("create failed!");
        }
        LOGGER.info("ipsec createByAc complete. ");

        return MergeRspUtil.mergeAllCreateRsp(insertDataList, sbiNeIpsecList, acCreateRsp, fsCreateRsp,
                OperationType.CREATE);

    }

    private static List<NbiIpSec> getIdAndInsertDb(List<NbiIpSec> nbiIpsecs) throws ServiceException {

        List<String> inputUuidList = IpSecIdUtil.allocTunnelListUuid(nbiIpsecs);

        InventoryDao<NbiIpSec> greTunnelDao = new InventoryDaoUtil<NbiIpSec>().getInventoryDao();

        if(CollectionUtils.isNotEmpty(inputUuidList)) {
            Map<String, List<String>> filterMap = new HashMap<>();
            filterMap.put("uuid", inputUuidList);

            List<NbiIpSec> dbTunnelList =
                    greTunnelDao.queryByFilter(NbiIpSec.class, JsonUtil.toJson(filterMap), null).getData();

            if(CollectionUtils.isNotEmpty(dbTunnelList)) {
                checkAndReAllocNbiIpsecsUuids(dbTunnelList, inputUuidList, nbiIpsecs);
            }
        }

        List<NbiIpSec> insertDataList = new ArrayList<>();
        for(NbiIpSec nbiIpsec : nbiIpsecs) {
            insertDataList.add(nbiIpsec);
        }

        greTunnelDao.batchInsert(insertDataList);
        return insertDataList;
    }

    private static void checkAndReAllocNbiIpsecsUuids(List<NbiIpSec> dbTunnelList, List<String> inputUuidList,
            List<NbiIpSec> nbiIpsecs) throws ServiceException {
        for(NbiIpSec tmpIpsec : dbTunnelList) {
            if(inputUuidList.contains(tmpIpsec.getUuid())) {
                LOGGER.error("Input Ipsec uuid exist in db. id: ", tmpIpsec.getUuid());
                throw new ParameterServiceException("Ipsec uuid exist in db! id:" + tmpIpsec.getUuid());
            }
        }
        // re create uuids for these conflict uuids which are created in this service
        IpSecIdUtil.reAllocTunnelListId(nbiIpsecs, dbTunnelList);
    }

    private static List<SbiNeIpSec> nbiMdelToSbiModel(List<NbiIpSec> nbiTunnels, List<SbiNeIpSec> acSbiNeIpsecs,
            List<SbiNeIpSec> vpcSbiNeIpsecs, Map<String, String> deviceIdToCtrollMap) throws ServiceException {
        List<SbiNeIpSec> sbiNeTunnels = NbiModelToSbiModel.convertToNeIpsec(nbiTunnels, deviceIdToCtrollMap);

        for(SbiNeIpSec tmpSbiNeTunnel : sbiNeTunnels) {
            tmpSbiNeTunnel.setOperationStatus(OperationStatus.CREATING.getName());

            if(NeRoleType.VPC.getName().equals(tmpSbiNeTunnel.getLocalNeRole())) {
                vpcSbiNeIpsecs.add(tmpSbiNeTunnel);
            } else {
                acSbiNeIpsecs.add(tmpSbiNeTunnel);
            }
        }

        try {
            AllocateResUtil.allocateExternalId(sbiNeTunnels);
            SbiIpsecDbOperUtil.insertNeIpsecList(sbiNeTunnels);
        } catch(ServiceException e) {
            LOGGER.error("insert sbiNeIpSec to db failed.", e);
            throw new InnerErrorServiceException("insert db failed!");
        }

        return sbiNeTunnels;

    }

}
