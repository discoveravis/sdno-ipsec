/*
 * Copyright (c) 2016, Huawei Technologies Co., Ltd.
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

package org.openo.sdno.ipsecservice.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.openo.baseservice.remoteservice.exception.ServiceException;
import org.openo.sdno.ipsecservice.sbi.inf.IDcGwIpSecConnSbiService;
import org.openo.sdno.ipsecservice.sbi.inf.INeIpSecConnSbiService;
import org.openo.sdno.ipsecservice.sbi.inf.IWanSubInfSbiService;
import org.openo.sdno.ipsecservice.service.inf.IIpSecService;
import org.openo.sdno.ipsecservice.util.db.DcGwIpSecConnDbOper;
import org.openo.sdno.ipsecservice.util.db.IpSecReqDbOper;
import org.openo.sdno.ipsecservice.util.db.NeIpSecConnDbOper;
import org.openo.sdno.ipsecservice.util.exception.ThrowException;
import org.openo.sdno.ipsecservice.util.operation.CommonUtil;
import org.openo.sdno.ipsecservice.util.operation.VpcInfo;
import org.openo.sdno.ipsecservice.util.security.Security;
import org.openo.sdno.overlayvpn.brs.model.ControllerMO;
import org.openo.sdno.overlayvpn.brs.model.NetworkElementMO;
import org.openo.sdno.overlayvpn.enums.WanInterfaceUsedType;
import org.openo.sdno.overlayvpn.errorcode.ErrorCode;
import org.openo.sdno.overlayvpn.model.common.enums.ActionStatus;
import org.openo.sdno.overlayvpn.model.common.enums.EndpointType;
import org.openo.sdno.overlayvpn.model.netmodel.ipsec.DcGwIpSecConnection;
import org.openo.sdno.overlayvpn.model.netmodel.ipsec.NeIpSecConnection;
import org.openo.sdno.overlayvpn.model.port.WanSubInterface;
import org.openo.sdno.overlayvpn.model.servicemodel.Connection;
import org.openo.sdno.overlayvpn.model.servicemodel.EndpointGroup;
import org.openo.sdno.overlayvpn.model.servicemodel.OverlayVpn;
import org.openo.sdno.overlayvpn.model.servicemodel.mappingpolicy.IpsecMappingPolicy;
import org.openo.sdno.overlayvpn.result.ResultRsp;
import org.openo.sdno.overlayvpn.util.ctrlconnection.ControllerUtil;
import org.openo.sdno.overlayvpn.util.objreflectoper.UuidAllocUtil;
import org.openo.sdno.util.ip.IpUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * IpSec service implementation. <br/>
 * 
 * @author
 * @version SDNO 0.5 Jun 16, 2016
 */
@Service
public class IpSecSvcImpl implements IIpSecService {

    private static final Logger LOGGER = LoggerFactory.getLogger(IpSecSvcImpl.class);

    private static final String ACTION_STATE = "actionState";

    private static final String OPER_STATUS = "operStatus";

    @Resource
    private IWanSubInfSbiService wanSubInfSbiService;

    @Resource
    private INeIpSecConnSbiService neIpSecConnSbiService;

    @Resource
    private IDcGwIpSecConnSbiService dcGwIpSecConnSbiService;

    public IWanSubInfSbiService getWanSubInfSbiService() {
        return wanSubInfSbiService;
    }

    public void setWanSubInfSbiService(IWanSubInfSbiService wanSubInfSbiService) {
        this.wanSubInfSbiService = wanSubInfSbiService;
    }

    public INeIpSecConnSbiService getNeIpSecConnSbiService() {
        return neIpSecConnSbiService;
    }

    public void setNeIpSecConnSbiService(INeIpSecConnSbiService neIpSecConnSbiService) {
        this.neIpSecConnSbiService = neIpSecConnSbiService;
    }

    public IDcGwIpSecConnSbiService getDcGwIpSecConnSbiService() {
        return dcGwIpSecConnSbiService;
    }

    public void setDcGwIpSecConnSbiService(IDcGwIpSecConnSbiService dcGwIpSecConnSbiService) {
        this.dcGwIpSecConnSbiService = dcGwIpSecConnSbiService;
    }

    @Override
    public ResultRsp<OverlayVpn> create(HttpServletRequest req, HttpServletResponse resp, OverlayVpn overlayVpn,
            Map<String, NetworkElementMO> neIdToNeMap) throws ServiceException {

        // get epgs from connection
        EndpointGroup vpcEndpointGroup = null;
        EndpointGroup acEndpointGroup = null;

        Connection connection = CommonUtil.getIpSecConnection(overlayVpn);
        List<EndpointGroup> endpointGroups = connection.getEndpointGroups();
        for(EndpointGroup tempEndpointGroup : endpointGroups) {
            if(EndpointType.VPC.getName().equals(tempEndpointGroup.getType())) {
                vpcEndpointGroup = tempEndpointGroup;
            } else {
                acEndpointGroup = tempEndpointGroup;
            }
        }

        if(vpcEndpointGroup == null || acEndpointGroup == null) {
            ThrowException.throwParameterInvalid("epgs in connection are invalid, it need a VPC epg and an other epg");

            // It's never be implemented as return in ThrowException.throwParameterInvalid, and here
            // just used to resolve code static checking.
            return null;
        }

        // get controller by NE information
        Map<String, ControllerMO> deviceIdToCtrlMapInAc = queryControllerMo(neIdToNeMap.get(acEndpointGroup.getNeId()));
        Map<String, ControllerMO> deviceIdToCtrlMapInDc =
                queryControllerMo(neIdToNeMap.get(vpcEndpointGroup.getNeId()));

        // query wan interface in AC Branch
        ResultRsp<Map<String, WanSubInterface>> wanInterfaceRsp = queryNeWanSubInterface(deviceIdToCtrlMapInAc);
        if(!wanInterfaceRsp.isValid()) {
            LOGGER.error("query wan-sub-interface failed.");
            return new ResultRsp<OverlayVpn>(wanInterfaceRsp);
        }

        Map<String, WanSubInterface> deviceIdToWansubInfMap = wanInterfaceRsp.getData();

        // get ikePolicy & ipSecPolicy from configuration file
        Security.getSecurity(connection);

        // build SBI datas
        List<NeIpSecConnection> neIpSecNeConnectionList = new ArrayList<NeIpSecConnection>();
        List<DcGwIpSecConnection> dcGwIpSecConnectionList = new ArrayList<DcGwIpSecConnection>();
        buildNeIpSecConnection(connection, vpcEndpointGroup, acEndpointGroup, deviceIdToWansubInfMap,
                deviceIdToCtrlMapInAc, neIpSecNeConnectionList);
        buildDcGwIpSecConnection(connection, vpcEndpointGroup, acEndpointGroup, deviceIdToWansubInfMap,
                deviceIdToCtrlMapInDc, dcGwIpSecConnectionList);

        // save data
        NeIpSecConnDbOper.insert(neIpSecNeConnectionList);
        DcGwIpSecConnDbOper.insert(dcGwIpSecConnectionList);

        // create ipsec connection in AC Branch
        createNeIpSecConnection(neIpSecNeConnectionList);

        // create ipsec connection in DC Gateway
        createDcGwIpSecConnection(dcGwIpSecConnectionList);

        return new ResultRsp<OverlayVpn>(ErrorCode.OVERLAYVPN_SUCCESS, overlayVpn);
    }

    @Override
    public ResultRsp<OverlayVpn> query(HttpServletRequest req, HttpServletResponse resp, String connectionId)
            throws ServiceException {

        // query data
        OverlayVpn overlayVpn = IpSecReqDbOper.query(connectionId);
        if(null == overlayVpn) {
            ThrowException.throwResNotExist("connectionId (" + connectionId + ") is not existed");
        }

        return new ResultRsp<OverlayVpn>(ErrorCode.OVERLAYVPN_SUCCESS, overlayVpn);
    }

    @Override
    public ResultRsp<String> delete(HttpServletRequest req, HttpServletResponse resp, String connectionId)
            throws ServiceException {

        // delete ipsec connection in AC Branch
        deleteNeIpSecConnection(connectionId);

        // delete ipsec connection in DC Gateway
        deleteDcGwIpSecConnection(connectionId);

        return new ResultRsp<String>(ErrorCode.OVERLAYVPN_SUCCESS);
    }

    private Map<String, ControllerMO> queryControllerMo(NetworkElementMO neMo) throws ServiceException {
        Map<String, ControllerMO> deviceIdToCtrlMap = new HashMap<String, ControllerMO>();
        // get neid list
        List<String> neUuidList = new ArrayList<String>();
        neUuidList.add(neMo.getId());

        // test the controller is reachable
        ResultRsp<Map<String, ControllerMO>> testCtrlResult = ControllerUtil.testCtrlConnection(neUuidList);
        Map<String, ControllerMO> neUuidToCtrlMap = testCtrlResult.getData();

        deviceIdToCtrlMap.put(neMo.getNativeID(), neUuidToCtrlMap.get(neMo.getId()));

        return deviceIdToCtrlMap;
    }

    private ResultRsp<Map<String, WanSubInterface>> queryNeWanSubInterface(Map<String, ControllerMO> deviceIdToCtrlMap)
            throws ServiceException {
        Map<String, WanSubInterface> deviceIdToWanSubInfMap = new HashMap<String, WanSubInterface>();
        for(Entry<String, ControllerMO> entry : deviceIdToCtrlMap.entrySet()) {
            String ctrlUuid = entry.getValue().getObjectId();
            String deviceId = entry.getKey();

            // query wan interface
            ResultRsp<List<WanSubInterface>> queryResult =
                    wanSubInfSbiService.queryNeWanSubInterface(ctrlUuid, deviceId, WanInterfaceUsedType.IPSEC.getName());
            if(!queryResult.isValid()) {
                LOGGER.error("failed to query wan sub interface for deviceid: " + deviceId);
                return new ResultRsp<Map<String, WanSubInterface>>(queryResult);
            }

            // throw exception when failed to get ip address
            WanSubInterface tempWanSubInterface = queryResult.getData().get(0);
            if(StringUtils.isEmpty(tempWanSubInterface.getIpAddress())) {
                LOGGER.error("failed to get ip, deviceid: " + deviceId);
                return new ResultRsp<Map<String, WanSubInterface>>(ErrorCode.OVERLAYVPN_FAILED, null, null,
                        "query wan sub interface failed", "query wan sub interface, please check");
            }

            deviceIdToWanSubInfMap.put(deviceId, tempWanSubInterface);
        }

        return new ResultRsp<>(ErrorCode.OVERLAYVPN_SUCCESS, deviceIdToWanSubInfMap);
    }

    private void buildNeIpSecConnection(Connection connection, EndpointGroup vpcEndpointGroup,
            EndpointGroup acEndpointGroup, Map<String, WanSubInterface> deviceIdToWansubInfMap,
            Map<String, ControllerMO> deviceIdToCtrlMapInAc, List<NeIpSecConnection> neIpSecNeConnectionList) {

        NeIpSecConnection neIpSecNeConnection = new NeIpSecConnection();
        neIpSecNeConnectionList.add(neIpSecNeConnection);

        UuidAllocUtil.allocUuid(neIpSecNeConnection);
        neIpSecNeConnection.setActionState(ActionStatus.CREATING.getName());
        neIpSecNeConnection.setConnectionServiceId(connection.getUuid());
        neIpSecNeConnection.setTenantId(connection.getTenantId());
        neIpSecNeConnection.setName(acEndpointGroup.getName());
        neIpSecNeConnection.setAdminStatus(acEndpointGroup.getAdminStatus());
        neIpSecNeConnection.setOperStatus(acEndpointGroup.getOperStatus());
        neIpSecNeConnection.setDescription(acEndpointGroup.getDescription());

        neIpSecNeConnection.setNeId(acEndpointGroup.getDeviceId());
        neIpSecNeConnection.setPeerNeId(vpcEndpointGroup.getDeviceId());
        neIpSecNeConnection.setTopoRole(acEndpointGroup.getTopologyRole());
        neIpSecNeConnection.setControllerId(deviceIdToCtrlMapInAc.get(acEndpointGroup.getDeviceId()).getObjectId());

        WanSubInterface currNeWanSubInf = deviceIdToWansubInfMap.get(acEndpointGroup.getDeviceId());
        neIpSecNeConnection.setSoureIfName(currNeWanSubInf.getName());
        neIpSecNeConnection.setSourceAddress(
                currNeWanSubInf.getIpAddress() + "/" + IpUtils.maskToPrefix(currNeWanSubInf.getMask()));

        VpcInfo vpcInfo = new VpcInfo(vpcEndpointGroup.getEndpointList().get(0));
        neIpSecNeConnection.setPeerAddress(vpcInfo.getRouterExternalIp());

        IpsecMappingPolicy ipsecMappingPolicy = connection.getIpsecMappingPolicy();
        neIpSecNeConnection.setAuthMode(ipsecMappingPolicy.getAuthMode());
        neIpSecNeConnection.setPsk(ipsecMappingPolicy.getPsk());
        neIpSecNeConnection.setIkePolicyId(ipsecMappingPolicy.getIkePolicyId());
        neIpSecNeConnection.setIpsecPolicyId(ipsecMappingPolicy.getIpsecPolicyId());
        neIpSecNeConnection.setIkePolicy(ipsecMappingPolicy.getIkePolicy());
        neIpSecNeConnection.setIpSecPolicy(ipsecMappingPolicy.getIpSecPolicy());

        neIpSecNeConnection.getIkePolicy().setTenantId(connection.getTenantId());
        neIpSecNeConnection.getIpSecPolicy().setTenantId(connection.getTenantId());

        // createModelTime
        // source
    }

    private void buildDcGwIpSecConnection(Connection connection, EndpointGroup vpcEndpointGroup,
            EndpointGroup acEndpointGroup, Map<String, WanSubInterface> deviceIdToWansubInfMap,
            Map<String, ControllerMO> deviceIdToCtrlMapInDc, List<DcGwIpSecConnection> dcGwIpSecConnectionList) {

        VpcInfo vpcInfo = new VpcInfo(vpcEndpointGroup.getEndpointList().get(0));

        DcGwIpSecConnection dcGwIpSecConnection = new DcGwIpSecConnection();
        dcGwIpSecConnectionList.add(dcGwIpSecConnection);

        UuidAllocUtil.allocUuid(dcGwIpSecConnection);
        dcGwIpSecConnection.setActionState(ActionStatus.CREATING.getName());
        dcGwIpSecConnection.setConnectionServiceId(connection.getUuid());
        dcGwIpSecConnection.setTenantId(vpcInfo.getVpcId());
        dcGwIpSecConnection.setName(vpcEndpointGroup.getName());
        dcGwIpSecConnection.setAdminStatus(vpcEndpointGroup.getAdminStatus());
        dcGwIpSecConnection.setOperStatus(vpcEndpointGroup.getOperStatus());
        dcGwIpSecConnection.setDescription(vpcEndpointGroup.getDescription());

        dcGwIpSecConnection.setTopoRole(vpcEndpointGroup.getTopologyRole());
        dcGwIpSecConnection.setControllerId(deviceIdToCtrlMapInDc.get(vpcEndpointGroup.getDeviceId()).getObjectId());

        dcGwIpSecConnection.setSubnetId(vpcInfo.getSubnetId());
        dcGwIpSecConnection.setRouterId(vpcInfo.getRouterId());
        dcGwIpSecConnection.setVpcId(vpcInfo.getVpcId());

        StringBuilder tempLansubCidr = new StringBuilder();
        for(String tempStr : acEndpointGroup.getEndpointList()) {
            tempLansubCidr.append(tempStr);
            tempLansubCidr.append(',');
        }
        dcGwIpSecConnection.setPeerSubnetCidrs(tempLansubCidr.toString());

        WanSubInterface currNeWanSubInf = deviceIdToWansubInfMap.get(acEndpointGroup.getDeviceId());
        dcGwIpSecConnection.setSourceAddress(vpcInfo.getRouterExternalIp());
        dcGwIpSecConnection
                .setPeerAddress(currNeWanSubInf.getIpAddress() + "/" + IpUtils.maskToPrefix(currNeWanSubInf.getMask()));

        IpsecMappingPolicy ipsecMappingPolicy = connection.getIpsecMappingPolicy();
        dcGwIpSecConnection.setAuthMode(ipsecMappingPolicy.getAuthMode());
        dcGwIpSecConnection.setPsk(ipsecMappingPolicy.getPsk());
        dcGwIpSecConnection.setIkePolicyId(ipsecMappingPolicy.getIkePolicyId());
        dcGwIpSecConnection.setIpsecPolicyId(ipsecMappingPolicy.getIpsecPolicyId());
        dcGwIpSecConnection.setIkePolicy(ipsecMappingPolicy.getIkePolicy());
        dcGwIpSecConnection.setIpSecPolicy(ipsecMappingPolicy.getIpSecPolicy());

        dcGwIpSecConnection.getIkePolicy().setTenantId(vpcInfo.getVpcId());
        dcGwIpSecConnection.getIpSecPolicy().setTenantId(vpcInfo.getVpcId());

    }

    private void createNeIpSecConnection(List<NeIpSecConnection> neIpSecNeConnectionList) throws ServiceException {

        // update actionState to exception firstly
        for(NeIpSecConnection neIpSecConnection : neIpSecNeConnectionList) {
            neIpSecConnection.setActionState(ActionStatus.CREATE_EXCEPTION.getName());
        }

        NeIpSecConnDbOper.update(neIpSecNeConnectionList, ACTION_STATE);

        // send restful message to adapter
        ResultRsp<List<NeIpSecConnection>> resultRsp =
                neIpSecConnSbiService.createIpSecNeConnection(neIpSecNeConnectionList);

        // check the response for error code and throw an exception in case of failure
        ThrowException.checkRspThrowException(resultRsp);

        // update actionState to normal and update operStatus that get from response
        for(NeIpSecConnection neIpSecConnection : resultRsp.getData()) {
            neIpSecConnection.setActionState(ActionStatus.NORMAL.getName());
        }

        NeIpSecConnDbOper.update(resultRsp.getData(), ACTION_STATE + "," + OPER_STATUS);
    }

    private void createDcGwIpSecConnection(List<DcGwIpSecConnection> dcGwIpSecConnectionList) throws ServiceException {

        // update actionState to exception firstly
        for(DcGwIpSecConnection dcGwIpSecConnection : dcGwIpSecConnectionList) {
            dcGwIpSecConnection.setActionState(ActionStatus.CREATE_EXCEPTION.getName());
        }

        DcGwIpSecConnDbOper.update(dcGwIpSecConnectionList, ACTION_STATE);

        // send restful message to adapter
        ResultRsp<List<DcGwIpSecConnection>> resultRsp =
                dcGwIpSecConnSbiService.createIpSecNeConnection(dcGwIpSecConnectionList);

        // check the response for error code and throw an exception in case of failure
        ThrowException.checkRspThrowException(resultRsp);

        // update actionState to normal and update operStatus that get from response
        for(DcGwIpSecConnection dcGwIpSecConnection : resultRsp.getData()) {
            dcGwIpSecConnection.setActionState(ActionStatus.NORMAL.getName());
        }

        DcGwIpSecConnDbOper.update(resultRsp.getData(), ACTION_STATE + "," + OPER_STATUS);
    }

    private void deleteNeIpSecConnection(String connectionId) throws ServiceException {
        // query data
        ResultRsp<List<NeIpSecConnection>> ipSecConnectionList = NeIpSecConnDbOper.query(connectionId);
        if(CollectionUtils.isEmpty(ipSecConnectionList.getData())) {
            LOGGER.warn("ne ipsec connectionId (" + connectionId + ") is not found");
            return;
        }

        // update actionState to exception firstly
        for(NeIpSecConnection neIpSecConnection : ipSecConnectionList.getData()) {
            neIpSecConnection.setActionState(ActionStatus.DELETE_EXCEPTION.getName());
        }

        NeIpSecConnDbOper.update(ipSecConnectionList.getData(), ACTION_STATE);

        // send restful message to adapter
        ResultRsp<String> resultRsp = neIpSecConnSbiService.deleteIpSecConnection(ipSecConnectionList.getData());

        // check the response for error code and throw an exception in case of failure
        ThrowException.checkRspThrowException(resultRsp);

        // delete data
        NeIpSecConnDbOper.delete(connectionId);
    }

    private void deleteDcGwIpSecConnection(String connectionId) throws ServiceException {
        // query data
        ResultRsp<List<DcGwIpSecConnection>> ipSecConnectionList = DcGwIpSecConnDbOper.query(connectionId);
        if(CollectionUtils.isEmpty(ipSecConnectionList.getData())) {
            LOGGER.warn("dc gateway ipsec connectionId (" + connectionId + ") is not found");
            return;
        }

        // update actionState to exception firstly
        for(DcGwIpSecConnection dcGwIpSecConnection : ipSecConnectionList.getData()) {
            dcGwIpSecConnection.setActionState(ActionStatus.DELETE_EXCEPTION.getName());
        }

        DcGwIpSecConnDbOper.update(ipSecConnectionList.getData(), ACTION_STATE);

        // send restful message to adapter
        ResultRsp<String> resultRsp = dcGwIpSecConnSbiService.deleteIpSecConnection(ipSecConnectionList.getData());

        // check the response for error code and throw an exception in case of failure
        ThrowException.checkRspThrowException(resultRsp);

        // delete data
        DcGwIpSecConnDbOper.delete(connectionId);
    }
}
