/*
 * Copyright 2017 Huawei Technologies Co., Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openo.sdno.ipsecservice.rest;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.poi.ss.formula.functions.T;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openo.baseservice.remoteservice.exception.ServiceException;
import org.openo.baseservice.roa.util.restclient.RestfulParametes;
import org.openo.baseservice.roa.util.restclient.RestfulResponse;
import org.openo.sdno.framework.container.resthelper.RestfulProxy;
import org.openo.sdno.framework.container.util.JsonUtil;
import org.openo.sdno.ipsecservice.model.IpSecReqModelInfo;
import org.openo.sdno.ipsecservice.util.db.IpSecReqDbOper;
import org.openo.sdno.ipsecservice.util.security.Security;
import org.openo.sdno.overlayvpn.brs.invdao.NetworkElementInvDao;
import org.openo.sdno.overlayvpn.brs.model.ControllerMO;
import org.openo.sdno.overlayvpn.brs.model.NetworkElementMO;
import org.openo.sdno.overlayvpn.consts.UrlAdapterConst;
import org.openo.sdno.overlayvpn.dao.common.InventoryDao;
import org.openo.sdno.overlayvpn.errorcode.ErrorCode;
import org.openo.sdno.overlayvpn.model.common.enums.AdminStatus;
import org.openo.sdno.overlayvpn.model.common.enums.EndpointType;
import org.openo.sdno.overlayvpn.model.common.enums.TechnologyType;
import org.openo.sdno.overlayvpn.model.common.enums.topo.TopologyRole;
import org.openo.sdno.overlayvpn.model.common.enums.topo.TopologyType;
import org.openo.sdno.overlayvpn.model.netmodel.ipsec.DcGwIpSecConnection;
import org.openo.sdno.overlayvpn.model.netmodel.ipsec.NeIpSecConnection;
import org.openo.sdno.overlayvpn.model.port.WanSubInterface;
import org.openo.sdno.overlayvpn.model.servicemodel.Connection;
import org.openo.sdno.overlayvpn.model.servicemodel.EndpointGroup;
import org.openo.sdno.overlayvpn.model.servicemodel.OverlayVpn;
import org.openo.sdno.overlayvpn.result.ResultRsp;
import org.openo.sdno.overlayvpn.util.ctrlconnection.ControllerUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import mockit.Mock;
import mockit.MockUp;
import mockit.Mocked;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath*:/spring/applicationContext.xml",
                "classpath*:META-INF/spring/service.xml", "classpath*:spring/service.xml"})
public class IpSecSvcRoaResourceTest {

    @Mocked
    HttpServletRequest request;

    @Mocked
    HttpServletResponse response;

    @Autowired
    IpSecSvcRoaResource ipSecSvc;

    @Before
    public void setUp() throws Exception {
        new MockInventoryDao();
        new MockRestfulProxy();
    }

    @Test
    public void testCreateSuccess() throws ServiceException {
        new MockUp<IpSecReqDbOper>() {

            @Mock
            void update(String connectionId, String actionState) throws ServiceException {

            }
        };

        new MockNeDao();
        new MockControllerDao();
        new MockSecurity();

        OverlayVpn overlayVpn = buildOverlayVpn();
        ResultRsp<OverlayVpn> resultRsp = ipSecSvc.create(request, response, overlayVpn);
        assertEquals(resultRsp.getErrorCode(), ErrorCode.OVERLAYVPN_SUCCESS);
    }

    @Test
    public void testQuerySuccess() throws ServiceException {
        new MockUp<InventoryDao<T>>() {

            @Mock
            ResultRsp queryByFilter(Class clazz, String filter, String queryResultFields) throws ServiceException {

                IpSecReqModelInfo ipSecReqModelInfo = new IpSecReqModelInfo();
                ipSecReqModelInfo.setData(JsonUtil.toJson(new OverlayVpn()));

                ResultRsp<List<IpSecReqModelInfo>> resp = new ResultRsp<List<IpSecReqModelInfo>>(
                        ErrorCode.OVERLAYVPN_SUCCESS, Arrays.asList(ipSecReqModelInfo));
                return resp;

            }

        };

        ResultRsp<OverlayVpn> resultRsp = ipSecSvc.query(request, response, "1111");
        assertEquals(resultRsp.getErrorCode(), ErrorCode.OVERLAYVPN_SUCCESS);
    }

    @Test
    public void testDeleteSuccess() throws ServiceException {

        new MockUp<InventoryDao<T>>() {

            @Mock
            ResultRsp queryByFilter(Class clazz, String filter, String queryResultFields) throws ServiceException {

                if(IpSecReqModelInfo.class.equals(clazz)) {
                    IpSecReqModelInfo ipSecReqModelInfo = new IpSecReqModelInfo();
                    ipSecReqModelInfo.setData(JsonUtil.toJson(new OverlayVpn()));

                    ResultRsp<List<IpSecReqModelInfo>> resp = new ResultRsp<List<IpSecReqModelInfo>>(
                            ErrorCode.OVERLAYVPN_SUCCESS, Arrays.asList(ipSecReqModelInfo));
                    return resp;
                } else if(NeIpSecConnection.class.equals(clazz)) {
                    NeIpSecConnection neIpSecConnection = new NeIpSecConnection();

                    ResultRsp<List<NeIpSecConnection>> resp = new ResultRsp<List<NeIpSecConnection>>(
                            ErrorCode.OVERLAYVPN_SUCCESS, Arrays.asList(neIpSecConnection));
                    return resp;
                } else if(DcGwIpSecConnection.class.equals(clazz)) {
                    DcGwIpSecConnection dcGwIpSecConnection = new DcGwIpSecConnection();

                    ResultRsp<List<DcGwIpSecConnection>> resp = new ResultRsp<List<DcGwIpSecConnection>>(
                            ErrorCode.OVERLAYVPN_SUCCESS, Arrays.asList(dcGwIpSecConnection));
                    return resp;
                }

                return null;
            }

        };

        ResultRsp<String> resultRsp = ipSecSvc.delete(request, response, "1111");
        assertEquals(resultRsp.getErrorCode(), ErrorCode.OVERLAYVPN_SUCCESS);
    }

    private final class MockInventoryDao<T> extends MockUp<InventoryDao<T>> {

        @Mock
        ResultRsp queryByFilter(Class clazz, String filter, String queryResultFields) throws ServiceException {
            if(IpSecReqModelInfo.class.equals(clazz)) {
                IpSecReqModelInfo ipSecReqModelInfo = null;

                ResultRsp<List<IpSecReqModelInfo>> resp = new ResultRsp<List<IpSecReqModelInfo>>(
                        ErrorCode.OVERLAYVPN_SUCCESS, Arrays.asList(ipSecReqModelInfo));
                resp.setData(null);
                return resp;
            }

            return null;
        }

        @Mock
        ResultRsp<String> batchDelete(Class clazz, List<String> uuids) throws ServiceException {
            return new ResultRsp<String>();
        }

        @Mock
        public ResultRsp update(Class clazz, List oriUpdateList, String updateFieldListStr) {
            return new ResultRsp(ErrorCode.OVERLAYVPN_SUCCESS);
        }

        @Mock
        public ResultRsp<T> insert(T data) throws ServiceException {
            return new ResultRsp(ErrorCode.OVERLAYVPN_SUCCESS);
        }

        @Mock
        public ResultRsp<List<T>> batchInsert(List<T> dataList) {
            return new ResultRsp(ErrorCode.OVERLAYVPN_SUCCESS);
        }
    }

    private final class MockRestfulProxy extends MockUp<RestfulProxy> {

        @Mock
        RestfulResponse get(String uri, RestfulParametes restParametes) throws ServiceException {
            RestfulResponse response = new RestfulResponse();

            if(uri.startsWith(UrlAdapterConst.WAN_INTERFACE_ADAPTER_BASE_URL)) {

                List<WanSubInterface> wanSubInterfaceList = new ArrayList<WanSubInterface>();
                WanSubInterface wanSubInterface = new WanSubInterface();
                wanSubInterface.setName("GigabitEthernet00/0/0.2");
                wanSubInterface.setCeHighVlan(859);
                wanSubInterface.setCeLowVlan(859);
                wanSubInterface.setIpAddress("192.168.1.2");
                wanSubInterface.setMask("255.255.255.0");

                wanSubInterfaceList.add(wanSubInterface);
                ResultRsp<List<WanSubInterface>> sbiRsp =
                        new ResultRsp<List<WanSubInterface>>(ErrorCode.OVERLAYVPN_SUCCESS, wanSubInterfaceList);

                response.setStatus(HttpStatus.SC_OK);
                response.setResponseJson(JsonUtil.toJson(sbiRsp));
            }

            return response;
        }

        @Mock
        RestfulResponse post(String uri, RestfulParametes restParametes) throws ServiceException {
            RestfulResponse response = new RestfulResponse();

            if(uri.startsWith(UrlAdapterConst.IPSEC_ADAPTER_BASE_URL + UrlAdapterConst.CREATE_AC_IPSEC_CONNECTION)) {

                List<NeIpSecConnection> neIpSecConnectionList = new ArrayList<NeIpSecConnection>();
                neIpSecConnectionList.add(new NeIpSecConnection());
                ResultRsp<List<NeIpSecConnection>> sbiRsp =
                        new ResultRsp<List<NeIpSecConnection>>(ErrorCode.OVERLAYVPN_SUCCESS, neIpSecConnectionList);

                response.setStatus(HttpStatus.SC_OK);
                response.setResponseJson(JsonUtil.toJson(sbiRsp));
            } else if(uri.startsWith(
                    UrlAdapterConst.IPSEC_ADAPTER_BASE_URL + UrlAdapterConst.CREATE_DCGW_IPSEC_CONNECTION)) {
                List<DcGwIpSecConnection> dcGwIpSecConnectionList = new ArrayList<DcGwIpSecConnection>();
                dcGwIpSecConnectionList.add(new DcGwIpSecConnection());
                ResultRsp<List<DcGwIpSecConnection>> sbiRsp =
                        new ResultRsp<List<DcGwIpSecConnection>>(ErrorCode.OVERLAYVPN_SUCCESS, dcGwIpSecConnectionList);

                response.setStatus(HttpStatus.SC_OK);
                response.setResponseJson(JsonUtil.toJson(sbiRsp));
            }

            return response;
        }

        @Mock
        RestfulResponse delete(String uri, RestfulParametes restParametes) throws ServiceException {
            RestfulResponse response = new RestfulResponse();

            ResultRsp<String> sbiRsp = new ResultRsp<String>(ErrorCode.OVERLAYVPN_SUCCESS);
            response.setStatus(HttpStatus.SC_OK);
            response.setResponseJson(JsonUtil.toJson(sbiRsp));

            return response;
        }

    }

    private class MockNeDao extends MockUp<NetworkElementInvDao> {

        @Mock
        public NetworkElementMO query(String neId) throws ServiceException {
            NetworkElementMO ne = new NetworkElementMO();

            ne.setNativeID(neId + "1");
            ne.setId(neId);

            return ne;
        }
    }

    private class MockControllerDao extends MockUp<ControllerUtil> {

        @Mock
        ResultRsp<Map<String, ControllerMO>> testCtrlConnection(List<String> neUuids) throws ServiceException {
            Map<String, ControllerMO> map = new HashMap<String, ControllerMO>();
            ControllerMO controllerMO = new ControllerMO();
            controllerMO.setObjectId(neUuids.get(0) + "8");
            map.put(neUuids.get(0), controllerMO);
            return new ResultRsp<Map<String, ControllerMO>>(ErrorCode.OVERLAYVPN_SUCCESS, map);
        }
    }

    private class MockSecurity extends MockUp<Security> {

        @Mock
        List<Map<String, String>> getJsonDataFromFile(String domain) throws ServiceException {
            List<Map<String, String>> result = new ArrayList<Map<String, String>>();
            Map<String, String> map = new HashMap<>();
            map.put("key", "value");
            result.add(map);
            return result;
        }
    }

    private OverlayVpn buildOverlayVpn() {

        OverlayVpn vpn = new OverlayVpn();

        Connection connection = new Connection();
        List<Connection> connectionList = new ArrayList<>();
        connectionList.add(connection);
        vpn.setVpnConnections(connectionList);

        EndpointGroup endpointGroupAc = new EndpointGroup();
        EndpointGroup endpointGroupDc = new EndpointGroup();
        List<EndpointGroup> endpointGroupList = new ArrayList<>();
        endpointGroupList.add(endpointGroupAc);
        endpointGroupList.add(endpointGroupDc);
        connection.setEndpointGroups(endpointGroupList);

        vpn.setUuid("000001");
        vpn.setName("overlayVpn");
        vpn.setAdminStatus(AdminStatus.ACTIVE.getName());

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
        endpointGroupDc.setEndpoints("[\"10.8.1.2/24|100003|100004|10.8.1.3|100005\"]");
        endpointGroupDc.setTopologyRole(TopologyRole.SPOKE.getName());
        endpointGroupDc.setNeId("100002");
        endpointGroupDc.setConnectionId("000002");

        return vpn;
    }

}
