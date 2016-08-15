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

package org.openo.sdno.ipsecservice.util.check;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.codehaus.jackson.type.TypeReference;
import org.openo.baseservice.remoteservice.exception.ServiceException;
import org.openo.sdno.framework.container.util.JsonUtil;
import org.openo.sdno.ipsecservice.util.exception.ThrowException;
import org.openo.sdno.ipsecservice.util.operation.CommonUtil;
import org.openo.sdno.overlayvpn.brs.invdao.NetworkElementInvDao;
import org.openo.sdno.overlayvpn.brs.invdao.SiteInvDao;
import org.openo.sdno.overlayvpn.brs.model.NetworkElementMO;
import org.openo.sdno.overlayvpn.brs.model.SiteMO;
import org.openo.sdno.overlayvpn.consts.ValidationConsts;
import org.openo.sdno.overlayvpn.model.common.enums.EndpointType;
import org.openo.sdno.overlayvpn.model.common.enums.TechnologyType;
import org.openo.sdno.overlayvpn.model.common.enums.topo.TopologyRole;
import org.openo.sdno.overlayvpn.model.common.enums.topo.TopologyType;
import org.openo.sdno.overlayvpn.model.servicemodel.Connection;
import org.openo.sdno.overlayvpn.model.servicemodel.EndpointGroup;
import org.openo.sdno.overlayvpn.model.servicemodel.OverlayVpn;
import org.openo.sdno.overlayvpn.util.check.ValidationUtil;
import org.openo.sdno.overlayvpn.util.objreflectoper.UuidAllocUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

/**
 * It is used to check parameters that pass by caller. <br/>
 * 
 * @author
 * @version SDNO 0.5 Jun 20, 2016
 */
public class CheckOverlayVpn {

    private static final Logger LOGGER = LoggerFactory.getLogger(CheckOverlayVpn.class);

    /**
     * Constructor<br/>
     * 
     * @since SDNO 0.5
     */
    private CheckOverlayVpn() {
    }

    /**
     * It is used to check parameters. <br/>
     * 
     * @param overlayVpn The object that want to be checked
     * @param neIdToNeMap NE ID to NE information mapping
     * @throws ServiceException When check failed
     * @since SDNO 0.5
     */
    public static void check(OverlayVpn overlayVpn, Map<String, NetworkElementMO> neIdToNeMap) throws ServiceException {

        // check parameters by annotation in OverlayVpn class
        checkModelData(overlayVpn);

        // check uuids are existed or not
        UuidAllocUtil.checkUuid(overlayVpn);

        // check uuids are consistency or not
        Connection connection = CommonUtil.getIpSecConnection(overlayVpn);
        checkModelUuidConsistency(overlayVpn.getUuid(), connection);

        // check connection
        String tenantId = overlayVpn.getTenantId();
        List<EndpointGroup> epgList = checkConnection(tenantId, connection);

        // check end points
        checkEndpointGroups(tenantId, epgList, neIdToNeMap);

        // check topology role
        checkTopoRole(connection);
    }

    private static void checkModelData(OverlayVpn overlayVpn) throws ServiceException {
        ValidationUtil.validateModel(overlayVpn);

        LOGGER.info("check cloudvpn model OK, name = " + overlayVpn.getName());
    }

    private static void checkModelUuidConsistency(String overlayVpnUuid, Connection connection)
            throws ServiceException {
        if(!overlayVpnUuid.equals(connection.getCompositeVpnId())) {
            ThrowException.throwUuidNotConsistency("Connection", connection.getUuid(), "OverlayVpn", overlayVpnUuid);
        }

        if(CollectionUtils.isEmpty(connection.getEndpointGroups())) {
            ThrowException.throwParameterInvalid("Miss end point groups in ipsec connection");
        }

        for(EndpointGroup tempEndpointGroup : connection.getEndpointGroups()) {
            if(!connection.getUuid().equals(tempEndpointGroup.getConnectionId())) {
                ThrowException.throwUuidNotConsistency("EndpointGroup", tempEndpointGroup.getUuid(), "Connection",
                        connection.getUuid());
            }
        }
    }

    private static List<EndpointGroup> checkConnection(String tenantId, Connection connection) throws ServiceException {
        int epgAcNum = 0;
        int epgVpcNum = 0;

        if(!(TechnologyType.IPSEC.getName().equals(connection.getTechnology()))) {
            ThrowException.throwParameterInvalid("Technology is not ipsec");
        }

        if(!CommonUtil.checkStringEqual(tenantId, connection.getTenantId())) {
            ThrowException.throwTenantIdInvalid(connection.getTenantId(), tenantId);
        }

        List<EndpointGroup> tempEndpointGroups = connection.getEndpointGroups();
        if(CollectionUtils.isEmpty(tempEndpointGroups)) {
            ThrowException.throwParameterInvalid("EndpointGroups is null");
        }

        for(EndpointGroup tempEndpointGroup : tempEndpointGroups) {
            if(EndpointType.VPC.getName().equals(tempEndpointGroup.getType())) {
                epgVpcNum++;
            } else {
                epgAcNum++;
            }
        }

        if(epgVpcNum == 0) {
            ThrowException.throwParameterInvalid("EndpointGroup of vpc is not existed");
        }

        if(epgVpcNum > 1) {
            ThrowException.throwParameterInvalid("EndpointGroup of vpc is over 1");
        }

        if(epgAcNum == 0) {
            ThrowException.throwParameterInvalid("EndpointGroup beside vpc is not existed");
        }

        if(epgAcNum > 1) {
            ThrowException.throwParameterInvalid("EndpointGroup beside vpc is over 1");
        }

        return tempEndpointGroups;
    }

    private static void checkEndpointGroups(String tenantId, List<EndpointGroup> epgList,
            Map<String, NetworkElementMO> neIdToNeMap) throws ServiceException {
        for(EndpointGroup epg : epgList) {
            if(!CommonUtil.checkStringEqual(tenantId, epg.getTenantId())) {
                ThrowException.throwTenantIdInvalid(epg.getTenantId(), tenantId);
            }

            if(null != epg.getGateway()) {
                ThrowException.throwParameterInvalid("Gateway is not Null");
            }
            checkEndpoints(epg);
            checkResourceInEpg(epg, neIdToNeMap);
        }
    }

    private static void checkEndpoints(EndpointGroup epg) throws ServiceException {
        String endpoints = epg.getEndpoints();

        List<String> endpointList = JsonUtil.fromJson(endpoints, new TypeReference<List<String>>() {});
        if(CollectionUtils.isEmpty(endpointList)) {
            ThrowException.throwParameterInvalid("Endpoints is null");
        }

        if(EndpointType.CIDR.getName().equals(epg.getType())) {
            for(String endpoint : endpointList) {
                if(!endpoint.matches(ValidationConsts.IP_MASK_REGEX)) {
                    ThrowException.throwParameterInvalid("Endpoints is invalid");
                }
            }
        }

        epg.setEndpointList(endpointList);
    }

    private static void checkResourceInEpg(EndpointGroup epg, Map<String, NetworkElementMO> neIdToNeMap)
            throws ServiceException {
        String epgNeId = epg.getNeId();

        NetworkElementInvDao neDao = new NetworkElementInvDao();
        NetworkElementMO tempNe = neDao.query(epgNeId);

        if(null == tempNe) {
            ThrowException.throwResNotExist("Ne (" + epgNeId + ") is not existed");

            // It's never be implemented as return in ThrowException.throwResNotExist, and here
            // just used to resolve code static checking.
            return;
        }

        String deviceId = tempNe.getNativeID();
        if(!StringUtils.hasLength(deviceId)) {
            ThrowException.throwParameterInvalid("device id (" + deviceId + ") is not existed");
        }

        epg.setDeviceId(deviceId);
        if(!neIdToNeMap.containsKey(epgNeId)) {
            neIdToNeMap.put(epgNeId, tempNe);
        }

        SiteInvDao siteDao = new SiteInvDao();
        SiteMO siteMO = siteDao.query(tempNe.getSiteID().get(0));
        epg.setSiteType(siteMO.getType());
    }

    @SuppressWarnings("unchecked")
    private static void checkTopoRole(Connection connection) throws ServiceException {
        if(!TopologyType.HUB_SPOKE.getName().equals(connection.getTopology())) {
            return;
        }

        List<EndpointGroup> hubEpgs =
                new ArrayList<>(CollectionUtils.select(connection.getEndpointGroups(), new Predicate() {

                    @Override
                    public boolean evaluate(Object arg0) {
                        EndpointGroup epg = (EndpointGroup)arg0;
                        return TopologyRole.HUB.getName().equals(epg.getTopologyRole());
                    }
                }));

        List<EndpointGroup> spokeEpgs =
                new ArrayList<>(CollectionUtils.select(connection.getEndpointGroups(), new Predicate() {

                    @Override
                    public boolean evaluate(Object arg0) {
                        EndpointGroup epg = (EndpointGroup)arg0;
                        return TopologyRole.SPOKE.getName().equals(epg.getTopologyRole());
                    }
                }));

        if(CollectionUtils.isEmpty(hubEpgs)) {
            ThrowException.throwParameterInvalid("connection topology type no hub");
        }

        if(hubEpgs.size() > 1) {
            ThrowException.throwParameterInvalid("connection topology type hub epg more than one");
        }

        if(CollectionUtils.isEmpty(spokeEpgs)) {
            ThrowException.throwParameterInvalid("connection topology type no spoke");
        }
    }
}
