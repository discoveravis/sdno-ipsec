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
import org.openo.sdno.ipsecservice.mocoserver.SbiAdapterCreateIpSecFailInDcServer;
import org.openo.sdno.ipsecservice.util.HttpRest;
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

/**
 * ITCreateIpSecFailAsDcErr test class. <br>
 * 
 * @author
 * @version SDNO 0.5 June 16, 2016
 */
public class ITCreateIpSecFailAsDcErr extends TestManager {

    private static SbiAdapterCreateIpSecFailInDcServer sbiAdapterServer = new SbiAdapterCreateIpSecFailInDcServer();

    private static final String CREATE_IPSEC_FAIL_TESTCASE_5 =
            "src/integration-test/resources/testcase/createipsecfail5.json";

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
    public void testCreateIpSecFail() throws ServiceException {
        try {
            // test create
            HttpRquestResponse httpCreateObject =
                    HttpModelUtils.praseHttpRquestResponseFromFile(CREATE_IPSEC_FAIL_TESTCASE_5);
            HttpRequest createRequest = httpCreateObject.getRequest();
            OverlayVpn newVpnData = JsonUtil.fromJson(createRequest.getData(), OverlayVpn.class);
            List<Connection> connectionList = newVpnData.getVpnConnections();
            List<EndpointGroup> epgList = connectionList.get(0).getEndpointGroups();
            epgList.get(0).setNeId(topo.getResourceUuid(ResourceType.NETWORKELEMENT, "Ne1"));
            epgList.get(1).setNeId(topo.getResourceUuid(ResourceType.NETWORKELEMENT, "Ne2"));
            createRequest.setData(JsonUtil.toJson(newVpnData));
            execTestCase(createRequest, new CheckerParameterInvalid());

        } finally {
            // clear data
            HttpRquestResponse deleteHttpObject =
                    HttpModelUtils.praseHttpRquestResponseFromFile(DELETE_IPSEC_SUCCESS_TESTCASE);
            HttpRequest deleteRequest = deleteHttpObject.getRequest();
            deleteRequest.setUri(PathReplace.replaceUuid("connectionid", deleteRequest.getUri(), "connection1id"));

            HttpRest.doSend(deleteRequest);
        }
    }

    private class CheckerParameterInvalid implements IChecker {

        @Override
        public boolean check(HttpResponse response) {
            if(HttpCode.ERR_FAILED == response.getStatus()) {
                return true;
            }

            return false;
        }

    }

}
