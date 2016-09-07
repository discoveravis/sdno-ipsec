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

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.codehaus.jackson.type.TypeReference;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openo.baseservice.remoteservice.exception.ServiceException;
import org.openo.sdno.framework.container.util.JsonUtil;
import org.openo.sdno.ipsecservice.util.operation.CommonUtil;
import org.openo.sdno.overlayvpn.brs.invdao.NetworkElementInvDao;
import org.openo.sdno.overlayvpn.brs.invdao.SiteInvDao;
import org.openo.sdno.overlayvpn.brs.model.NetworkElementMO;
import org.openo.sdno.overlayvpn.brs.model.SiteMO;
import org.openo.sdno.overlayvpn.model.common.enums.AdminStatus;
import org.openo.sdno.overlayvpn.model.common.enums.EndpointType;
import org.openo.sdno.overlayvpn.model.common.enums.TechnologyType;
import org.openo.sdno.overlayvpn.model.common.enums.topo.TopologyRole;
import org.openo.sdno.overlayvpn.model.common.enums.topo.TopologyType;
import org.openo.sdno.overlayvpn.model.servicemodel.Connection;
import org.openo.sdno.overlayvpn.model.servicemodel.EndpointGroup;
import org.openo.sdno.overlayvpn.model.servicemodel.Gateway;
import org.openo.sdno.overlayvpn.model.servicemodel.OverlayVpn;

import mockit.Mock;
import mockit.MockUp;

public class CheckOverlayVpnTest {

    Map<String, NetworkElementMO> neIdToNeMap = new ConcurrentHashMap<String, NetworkElementMO>();

    OverlayVpn overlayVpn;

    @Before
    public void setUp() throws Exception {
        neIdToNeMap.clear();

        overlayVpn = new OverlayVpn();

        Connection connection = new Connection();
        List<Connection> connectionList = new ArrayList<>();
        connectionList.add(connection);
        overlayVpn.setVpnConnections(connectionList);

        EndpointGroup endpointGroupAc = new EndpointGroup();
        EndpointGroup endpointGroupDc = new EndpointGroup();
        List<EndpointGroup> endpointGroupList = new ArrayList<>();
        endpointGroupList.add(endpointGroupAc);
        endpointGroupList.add(endpointGroupDc);
        connection.setEndpointGroups(endpointGroupList);

        overlayVpn.setUuid("000001");
        overlayVpn.setName("overlayVpn");
        overlayVpn.setAdminStatus(AdminStatus.ACTIVE.getName());

        connection.setUuid("000002");
        connection.setName("connection");
        connection.setAdminStatus(AdminStatus.ACTIVE.getName());
        connection.setTopology(TopologyType.HUB_SPOKE.getName());
        connection.setTechnology(TechnologyType.IPSEC.getName());
        connection.setCompositeVpnId("000001");

        endpointGroupAc.setUuid("000003");
        endpointGroupAc.setName("endpointGroupAc");
        endpointGroupAc.setAdminStatus(AdminStatus.ACTIVE.getName());
        endpointGroupAc.setType(EndpointType.CIDR.getName());
        endpointGroupAc.setEndpoints("[\"10.8.1.1/24\"]");
        endpointGroupAc.setTopologyRole(TopologyRole.HUB.getName());
        endpointGroupAc.setNeId("100001");
        endpointGroupAc.setConnectionId("000002");

        endpointGroupDc.setUuid("000004");
        endpointGroupDc.setName("endpointGroupDc");
        endpointGroupDc.setAdminStatus(AdminStatus.ACTIVE.getName());
        endpointGroupDc.setType(EndpointType.VPC.getName());
        // cidr|vpcId|osRouterID|routerExternalIP|subnetID
        endpointGroupDc.setEndpoints("[\"10.8.1.2/24|100003|100004|10.8.1.3|100005\"]");
        endpointGroupDc.setTopologyRole(TopologyRole.SPOKE.getName());
        endpointGroupDc.setNeId("100002");
        endpointGroupDc.setConnectionId("000002");
    }

    @Test
    public void testCheckOk() throws ServiceException {
        new MockNeDao();
        new MockSiteDao();
        CheckOverlayVpn.check(overlayVpn, neIdToNeMap);
    }

    @Test(expected = ServiceException.class)
    public void testCheckOverlayVpnNull() throws ServiceException {
        CheckOverlayVpn.check(null, neIdToNeMap);
    }

    @Test(expected = ServiceException.class)
    public void testCheckAdminStatusInvalid() throws ServiceException {
        overlayVpn.setAdminStatus("aaa");
        CheckOverlayVpn.check(overlayVpn, neIdToNeMap);
    }

    @Test(expected = ServiceException.class)
    public void testCheckUuidInvalid() throws ServiceException {
        overlayVpn.setUuid("_)(000001");
        CheckOverlayVpn.check(overlayVpn, neIdToNeMap);
    }

    @Test(expected = ServiceException.class)
    public void testCheckUuidNull() throws ServiceException {
        overlayVpn.getVpnConnections().get(0).setUuid(null);
        CheckOverlayVpn.check(overlayVpn, neIdToNeMap);
    }

    @Ignore
    // 放到getIpSecConnection
    public void testVpnConnectionsNull() throws ServiceException {
        overlayVpn.setVpnConnections(null);
        try {
            CheckOverlayVpn.check(overlayVpn, neIdToNeMap);
            assertTrue(false);
        } catch(ServiceException e) {
            assertTrue("Miss connection in overlayvpn".equals(e.getExceptionArgs().getDescArgs()[0]));
        }
    }

    @Test(expected = ServiceException.class)
    public void testCheckUuidConsistency1() throws ServiceException {
        overlayVpn.setUuid("200001");
        CheckOverlayVpn.check(overlayVpn, neIdToNeMap);
    }

    @Test(expected = ServiceException.class)
    public void testCheckUuidConsistency2() throws ServiceException {
        overlayVpn.getVpnConnections().get(0).getEndpointGroups().get(1).setConnectionId("200001");
        CheckOverlayVpn.check(overlayVpn, neIdToNeMap);
    }

    @Test
    public void testEndpointGroupsNull() throws ServiceException {
        overlayVpn.getVpnConnections().get(0).setEndpointGroups(null);
        try {
            CheckOverlayVpn.check(overlayVpn, neIdToNeMap);
            assertTrue(false);
        } catch(ServiceException e) {
            assertTrue("Miss end point groups in ipsec connection".equals(e.getExceptionArgs().getDescArgs()[0]));
        }
    }

    @Test(expected = ServiceException.class)
    public void testCheckConnectionTechnologyInvalid() throws ServiceException {
        new MockUp<CommonUtil>() {

            @Mock
            public Connection getIpSecConnection(OverlayVpn overlayVpn) throws ServiceException {
                Connection connection = overlayVpn.getVpnConnections().get(0);
                connection.setTechnology(TechnologyType.GRE.getName());

                return connection;
            }
        };

        CheckOverlayVpn.check(overlayVpn, neIdToNeMap);
    }

    @Test(expected = ServiceException.class)
    public void testCheckConnectionTenantIdInvalid() throws ServiceException {
        overlayVpn.getVpnConnections().get(0).setTenantId("123456");
        CheckOverlayVpn.check(overlayVpn, neIdToNeMap);
    }

    @Test(expected = ServiceException.class)
    public void testCheckConnectionEndpointGroupsNull() throws ServiceException {
        overlayVpn.getVpnConnections().get(0).setEndpointGroups(null);
        CheckOverlayVpn.check(overlayVpn, neIdToNeMap);
    }

    @Test(expected = ServiceException.class)
    public void testCheckConnectionVpcEpgNull() throws ServiceException {
        overlayVpn.getVpnConnections().get(0).getEndpointGroups().get(1).setType(EndpointType.CIDR.getName());
        CheckOverlayVpn.check(overlayVpn, neIdToNeMap);
    }

    @Test(expected = ServiceException.class)
    public void testCheckConnectionVpcEpgOverOne() throws ServiceException {
        overlayVpn.getVpnConnections().get(0).getEndpointGroups().get(0).setType(EndpointType.VPC.getName());
        CheckOverlayVpn.check(overlayVpn, neIdToNeMap);
    }

    @Test(expected = ServiceException.class)
    public void testCheckConnectionAcEpgNull() throws ServiceException {
        overlayVpn.getVpnConnections().get(0).getEndpointGroups().remove(0);
        CheckOverlayVpn.check(overlayVpn, neIdToNeMap);
    }

    @Test(expected = ServiceException.class)
    public void testCheckConnectionAcEpgOverOne() throws ServiceException {
        EndpointGroup endpointGroupAc = new EndpointGroup();
        endpointGroupAc.setUuid("000004");
        endpointGroupAc.setName("endpointGroupAc");
        endpointGroupAc.setAdminStatus(AdminStatus.ACTIVE.getName());
        endpointGroupAc.setType(EndpointType.CIDR.getName());
        endpointGroupAc.setEndpoints("[\"10.8.1.1/24\"]");
        endpointGroupAc.setTopologyRole(TopologyRole.SPOKE.getName());
        endpointGroupAc.setNeId("100001");
        endpointGroupAc.setConnectionId("000002");

        overlayVpn.getVpnConnections().get(0).getEndpointGroups().add(endpointGroupAc);
        CheckOverlayVpn.check(overlayVpn, neIdToNeMap);
    }

    @Test(expected = ServiceException.class)
    public void testCheckEndpointGroupTenantIdInvalid() throws ServiceException {
        overlayVpn.getVpnConnections().get(0).getEndpointGroups().get(0).setTenantId("123456");
        CheckOverlayVpn.check(overlayVpn, neIdToNeMap);
    }

    @Test(expected = ServiceException.class)
    public void testCheckEndpointGroupGatewayNotNull() throws ServiceException {
        Gateway gateway = new Gateway();
        gateway.setUuid("000005");
        gateway.setName("gateway");
        gateway.setIpAddress("10.8.1.1");
        gateway.setNeId("000002");

        overlayVpn.getVpnConnections().get(0).getEndpointGroups().get(0).setGateway(gateway);
        CheckOverlayVpn.check(overlayVpn, neIdToNeMap);
    }

    @Test(expected = ServiceException.class)
    public void testCheckEndpointGroupEndpointListNull() throws ServiceException {
        new MockUp<JsonUtil>() {

            @Mock
            public <T> T fromJson(String jsonStr, TypeReference<T> typeRef) {
                return null;
            }
        };

        CheckOverlayVpn.check(overlayVpn, neIdToNeMap);
    }

    @Test(expected = ServiceException.class)
    public void testCheckEndpointGroupEndpointListInvalid() throws ServiceException {
        overlayVpn.getVpnConnections().get(0).getEndpointGroups().get(0).setEndpoints("[\"10.8.1/24\"]");
        CheckOverlayVpn.check(overlayVpn, neIdToNeMap);
    }

    @Test(expected = ServiceException.class)
    public void testCheckResourceInEpgNeNotExist() throws ServiceException {
        new MockUp<NetworkElementInvDao>() {

            @Mock
            public NetworkElementMO query(String id) throws ServiceException {
                return null;
            }
        };

        CheckOverlayVpn.check(overlayVpn, neIdToNeMap);
    }

    @Test(expected = ServiceException.class)
    public void testCheckResourceInEpgDeviceIdNull() throws ServiceException {
        new MockNeDao();
        overlayVpn.getVpnConnections().get(0).getEndpointGroups().get(0).setNeId("0");
        CheckOverlayVpn.check(overlayVpn, neIdToNeMap);
    }

    @Test
    public void testCheckResourceInEpgNeIdToNeMapExistedNe() throws ServiceException {
        new MockNeDao();
        new MockSiteDao();
        String neid = overlayVpn.getVpnConnections().get(0).getEndpointGroups().get(0).getNeId();
        String neid1 = overlayVpn.getVpnConnections().get(0).getEndpointGroups().get(1).getNeId();
        NetworkElementMO neMo = new NetworkElementMO();
        neIdToNeMap.put(neid, neMo);
        CheckOverlayVpn.check(overlayVpn, neIdToNeMap);

        assertTrue(neIdToNeMap.get(neid).getId() == null);
        assertTrue(neIdToNeMap.get(neid1).getId().equals(neid1));
    }

    @Test
    public void testCheckTopoRoleTopologyIsNotHubSpoke() throws ServiceException {
        new MockNeDao();
        new MockSiteDao();
        overlayVpn.getVpnConnections().get(0).setTopology(TopologyType.POINT_TO_POINT.getName());
        overlayVpn.getVpnConnections().get(0).getEndpointGroups().get(0).setTopologyRole(TopologyRole.SPOKE.getName());
        overlayVpn.getVpnConnections().get(0).getEndpointGroups().get(1).setTopologyRole(TopologyRole.SPOKE.getName());
        CheckOverlayVpn.check(overlayVpn, neIdToNeMap);
    }

    @Test
    public void testCheckTopoRoleHubEpgsNull() throws ServiceException {
        new MockNeDao();
        new MockSiteDao();
        overlayVpn.getVpnConnections().get(0).getEndpointGroups().get(0).setTopologyRole(TopologyRole.SPOKE.getName());
        overlayVpn.getVpnConnections().get(0).getEndpointGroups().get(1).setTopologyRole(TopologyRole.SPOKE.getName());
        try {
            CheckOverlayVpn.check(overlayVpn, neIdToNeMap);
            assertTrue(false);
        } catch(ServiceException e) {
            assertTrue("connection topology type no hub".equals(e.getExceptionArgs().getDescArgs()[0]));
        }
    }

    @Test
    public void testCheckTopoRoleHubEpgsOverOne() throws ServiceException {
        new MockNeDao();
        new MockSiteDao();
        overlayVpn.getVpnConnections().get(0).getEndpointGroups().get(0).setTopologyRole(TopologyRole.HUB.getName());
        overlayVpn.getVpnConnections().get(0).getEndpointGroups().get(1).setTopologyRole(TopologyRole.HUB.getName());
        try {
            CheckOverlayVpn.check(overlayVpn, neIdToNeMap);
            assertTrue(false);
        } catch(ServiceException e) {
            assertTrue("connection topology type hub epg more than one".equals(e.getExceptionArgs().getDescArgs()[0]));
        }
    }

    @Test
    public void testCheckTopoRoleSpokeEpgsNull() throws ServiceException {
        new MockNeDao();
        new MockSiteDao();
        overlayVpn.getVpnConnections().get(0).getEndpointGroups().get(1).setTopologyRole(TopologyRole.NONE.getName());

        try {
            CheckOverlayVpn.check(overlayVpn, neIdToNeMap);
            assertTrue(false);
        } catch(ServiceException e) {
            assertTrue("connection topology type no spoke".equals(e.getExceptionArgs().getDescArgs()[0]));
        }
    }

    private class MockNeDao extends MockUp<NetworkElementInvDao> {

        @Mock
        public NetworkElementMO query(String neId) throws ServiceException {
            NetworkElementMO ne = new NetworkElementMO();

            ne.setNativeID(neId + "1");
            if("0".equals(neId)) {
                ne.setNativeID("");
            }

            ne.setId(neId);

            List<String> siteIds = new ArrayList<String>();
            siteIds.add("112233");
            ne.setSiteID(siteIds);

            return ne;
        }
    }

    private class MockSiteDao extends MockUp<SiteInvDao> {

        @Mock
        public SiteMO query(String id) throws ServiceException {
            SiteMO site = new SiteMO();
            site.setId("000005");

            return site;
        }
    }

}
