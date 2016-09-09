/*
 * Copyright 2016 Huawei Technologies Co., Ltd.
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

package org.openo.sdno.ipsecservice.test;

import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openo.baseservice.remoteservice.exception.ServiceException;
import org.openo.sdno.exception.HttpCode;
import org.openo.sdno.framework.container.util.JsonUtil;
import org.openo.sdno.ipsecservice.mocoserver.SbiAdapterSuccessServer;
import org.openo.sdno.ipsecservice.util.HttpRest;
import org.openo.sdno.overlayvpn.errorcode.ErrorCode;
import org.openo.sdno.overlayvpn.model.servicemodel.Connection;
import org.openo.sdno.overlayvpn.model.servicemodel.EndpointGroup;
import org.openo.sdno.overlayvpn.model.servicemodel.OverlayVpn;
import org.openo.sdno.testframework.checker.IChecker;
import org.openo.sdno.testframework.http.model.HttpModelUtils;
import org.openo.sdno.testframework.http.model.HttpRequest;
import org.openo.sdno.testframework.http.model.HttpResponse;
import org.openo.sdno.testframework.http.model.HttpRquestResponse;
import org.openo.sdno.testframework.replace.PathReplace;
import org.openo.sdno.testframework.testmanager.TestManager;
import org.openo.sdno.testframework.topology.ResourceType;
import org.openo.sdno.testframework.topology.Topology;

public class ITOperateIpSecSuccess extends TestManager {

    private static SbiAdapterSuccessServer sbiAdapterServer = new SbiAdapterSuccessServer();

    private static final String CREATE_IPSEC_SUCCESS_TESTCASE =
            "src/integration-test/resources/testcase/createipsecsuccess1.json";

    private static final String QUERY_IPSEC_SUCCESS_TESTCASE =
            "src/integration-test/resources/testcase/queryipsecsuccess1.json";

    private static final String DELETE_IPSEC_SUCCESS_TESTCASE =
            "src/integration-test/resources/testcase/deleteipsecsuccess1.json";

    private static final String TOPODATA_PATH = "src/integration-test/resources/topodata";

    private static Topology topo = new Topology(TOPODATA_PATH);

    @BeforeClass
    public static void setup() throws ServiceException {
        topo.createInvTopology();
        sbiAdapterServer.start();
    }

    @AfterClass
    public static void tearDown() throws ServiceException {
        sbiAdapterServer.stop();
        topo.clearInvTopology();
    }

    @Test
    public void testOperateIpSecSuccess() throws ServiceException {
        try {
            // test create
            HttpRquestResponse httpCreateObject =
                    HttpModelUtils.praseHttpRquestResponseFromFile(CREATE_IPSEC_SUCCESS_TESTCASE);
            HttpRequest createRequest = httpCreateObject.getRequest();
            OverlayVpn newVpnData = JsonUtil.fromJson(createRequest.getData(), OverlayVpn.class);
            List<Connection> connectionList = newVpnData.getVpnConnections();
            List<EndpointGroup> epgList = connectionList.get(0).getEndpointGroups();
            epgList.get(0).setNeId(topo.getResourceUuid(ResourceType.NETWORKELEMENT, "Ne1"));
            epgList.get(1).setNeId(topo.getResourceUuid(ResourceType.NETWORKELEMENT, "Ne2"));
            createRequest.setData(JsonUtil.toJson(newVpnData));
            execTestCase(createRequest, new SuccessChecker());

            // test query
            HttpRquestResponse queryHttpObject =
                    HttpModelUtils.praseHttpRquestResponseFromFile(QUERY_IPSEC_SUCCESS_TESTCASE);
            HttpRequest queryRequest = queryHttpObject.getRequest();
            queryRequest.setUri(PathReplace.replaceUuid("connectionid", queryRequest.getUri(), "connection1id"));
            execTestCase(queryRequest, new SuccessChecker());

            // test delete
            HttpRquestResponse deleteHttpObject =
                    HttpModelUtils.praseHttpRquestResponseFromFile(DELETE_IPSEC_SUCCESS_TESTCASE);
            HttpRequest deleteRequest = deleteHttpObject.getRequest();
            deleteRequest.setUri(PathReplace.replaceUuid("connectionid", deleteRequest.getUri(), "connection1id"));
            execTestCase(deleteRequest, new SuccessChecker());
        } finally {
            // clear data
            HttpRquestResponse deleteHttpObject =
                    HttpModelUtils.praseHttpRquestResponseFromFile(DELETE_IPSEC_SUCCESS_TESTCASE);
            HttpRequest deleteRequest = deleteHttpObject.getRequest();
            deleteRequest.setUri(PathReplace.replaceUuid("connectionid", deleteRequest.getUri(), "connection1id"));

            HttpRest.doSend(deleteRequest);
        }
    }

    @Test
    public void testCreateIpSecFiveTimeSuccess() throws ServiceException {
        try {
            // test create 1 ipsec
            HttpRquestResponse httpCreateObject =
                    HttpModelUtils.praseHttpRquestResponseFromFile(CREATE_IPSEC_SUCCESS_TESTCASE);
            HttpRequest createRequest = httpCreateObject.getRequest();
            OverlayVpn newVpnData = JsonUtil.fromJson(createRequest.getData(), OverlayVpn.class);
            List<Connection> connectionList = newVpnData.getVpnConnections();
            List<EndpointGroup> epgList = connectionList.get(0).getEndpointGroups();
            epgList.get(0).setNeId(topo.getResourceUuid(ResourceType.NETWORKELEMENT, "Ne1"));
            epgList.get(1).setNeId(topo.getResourceUuid(ResourceType.NETWORKELEMENT, "Ne2"));
            createRequest.setData(JsonUtil.toJson(newVpnData));
            execTestCase(createRequest, new SuccessChecker());

            // test create 1 ipsec
            Connection connection = newVpnData.getVpnConnections().get(0);
            String uuid = "connection2id";
            connection.setUuid(uuid);
            connection.getEndpointGroups().get(0).setConnectionId(uuid);
            connection.getEndpointGroups().get(1).setConnectionId(uuid);
            createRequest.setData(JsonUtil.toJson(newVpnData));
            execTestCase(createRequest, new SuccessChecker());

            // test create 1 ipsec
            uuid = "connection3id";
            connection.setUuid(uuid);
            connection.getEndpointGroups().get(0).setConnectionId(uuid);
            connection.getEndpointGroups().get(1).setConnectionId(uuid);
            createRequest.setData(JsonUtil.toJson(newVpnData));
            execTestCase(createRequest, new SuccessChecker());

            // test create 1 ipsec
            uuid = "connection4id";
            connection.setUuid(uuid);
            connection.getEndpointGroups().get(0).setConnectionId(uuid);
            connection.getEndpointGroups().get(1).setConnectionId(uuid);
            createRequest.setData(JsonUtil.toJson(newVpnData));
            execTestCase(createRequest, new SuccessChecker());

            // test create 1 ipsec
            uuid = "connection5id";
            connection.setUuid(uuid);
            connection.getEndpointGroups().get(0).setConnectionId(uuid);
            connection.getEndpointGroups().get(1).setConnectionId(uuid);
            createRequest.setData(JsonUtil.toJson(newVpnData));
            execTestCase(createRequest, new SuccessChecker());

        } finally {
            // clear data
            HttpRquestResponse deleteHttpObject =
                    HttpModelUtils.praseHttpRquestResponseFromFile(DELETE_IPSEC_SUCCESS_TESTCASE);
            HttpRequest deleteRequest = deleteHttpObject.getRequest();
            deleteRequest.setUri(PathReplace.replaceUuid("connectionid", deleteRequest.getUri(), "connection1id"));
            HttpRest.doSend(deleteRequest);

            deleteHttpObject = HttpModelUtils.praseHttpRquestResponseFromFile(DELETE_IPSEC_SUCCESS_TESTCASE);
            deleteRequest = deleteHttpObject.getRequest();
            deleteRequest.setUri(PathReplace.replaceUuid("connectionid", deleteRequest.getUri(), "connection2id"));
            HttpRest.doSend(deleteRequest);

            deleteHttpObject = HttpModelUtils.praseHttpRquestResponseFromFile(DELETE_IPSEC_SUCCESS_TESTCASE);
            deleteRequest = deleteHttpObject.getRequest();
            deleteRequest.setUri(PathReplace.replaceUuid("connectionid", deleteRequest.getUri(), "connection3id"));
            HttpRest.doSend(deleteRequest);

            deleteHttpObject = HttpModelUtils.praseHttpRquestResponseFromFile(DELETE_IPSEC_SUCCESS_TESTCASE);
            deleteRequest = deleteHttpObject.getRequest();
            deleteRequest.setUri(PathReplace.replaceUuid("connectionid", deleteRequest.getUri(), "connection4id"));
            HttpRest.doSend(deleteRequest);

            deleteHttpObject = HttpModelUtils.praseHttpRquestResponseFromFile(DELETE_IPSEC_SUCCESS_TESTCASE);
            deleteRequest = deleteHttpObject.getRequest();
            deleteRequest.setUri(PathReplace.replaceUuid("connectionid", deleteRequest.getUri(), "connection5id"));
            HttpRest.doSend(deleteRequest);
        }
    }

    private class SuccessChecker implements IChecker {

        @Override
        public boolean check(HttpResponse response) {
            if(HttpCode.isSucess(response.getStatus())) {
                if(response.getData().contains(ErrorCode.OVERLAYVPN_SUCCESS)) {
                    return true;
                }
            }

            return false;
        }

    }

}
