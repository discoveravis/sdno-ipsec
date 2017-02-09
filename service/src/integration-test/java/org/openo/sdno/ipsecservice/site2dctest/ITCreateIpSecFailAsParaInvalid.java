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

package org.openo.sdno.ipsecservice.site2dctest;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openo.baseservice.remoteservice.exception.ServiceException;
import org.openo.sdno.exception.HttpCode;
import org.openo.sdno.ipsecservice.site2dcmocoserver.SbiAdapterSuccessServer;
import org.openo.sdno.ipsecservice.util.DriverRegisterManager;
import org.openo.sdno.ipsecservice.util.HttpRest;
import org.openo.sdno.testframework.checker.IChecker;
import org.openo.sdno.testframework.http.model.HttpModelUtils;
import org.openo.sdno.testframework.http.model.HttpRequest;
import org.openo.sdno.testframework.http.model.HttpResponse;
import org.openo.sdno.testframework.http.model.HttpRquestResponse;
import org.openo.sdno.testframework.replace.PathReplace;
import org.openo.sdno.testframework.testmanager.TestManager;
import org.openo.sdno.testframework.topology.Topology;

/**
 * ITCreateIpSecFailAsAcErr test class. <br>
 * 
 * @author
 * @version SDNO 0.5 June 16, 2016
 */
public class ITCreateIpSecFailAsParaInvalid extends TestManager {

    private static SbiAdapterSuccessServer sbiAdapterServer = new SbiAdapterSuccessServer();

    private static final String CREATE_IPSEC_FAIL_TESTCASE_1 =
            "src/integration-test/resources/site2dctestcase/createipsecfail1.json";

    private static final String DELETE_IPSEC_SUCCESS_TESTCASE =
            "src/integration-test/resources/site2dctestcase/deleteipsecsuccess1.json";

    private static final String TOPODATA_PATH = "src/integration-test/resources/site2dctopodata";

    private static Topology topo = new Topology(TOPODATA_PATH);

    @BeforeClass
    public static void setup() throws ServiceException {
        topo.createInvTopology();
        DriverRegisterManager.registerDriver();
        sbiAdapterServer.start();
    }

    @AfterClass
    public static void tearDown() throws ServiceException {
        sbiAdapterServer.stop();
        DriverRegisterManager.unRegisterDriver();
        topo.clearInvTopology();
    }

    @Test
    public void testCreateIpSecFail() throws ServiceException {
        try {
            // test create
            HttpRquestResponse httpCreateObject =
                    HttpModelUtils.praseHttpRquestResponseFromFile(CREATE_IPSEC_FAIL_TESTCASE_1);
            HttpRequest createRequest = httpCreateObject.getRequest();

            execTestCase(createRequest, new Checker());

        } finally {
            // clear data
            HttpRquestResponse deleteHttpObject =
                    HttpModelUtils.praseHttpRquestResponseFromFile(DELETE_IPSEC_SUCCESS_TESTCASE);
            HttpRequest deleteReq = deleteHttpObject.getRequest();
            deleteReq.setUri(PathReplace.replaceUuid("ipsecConnectionId", deleteReq.getUri(), "ipsecconnection1id"));

            HttpRest.doSend(deleteReq);
        }
    }

    private class Checker implements IChecker {

        @Override
        public boolean check(HttpResponse response) {

            if(HttpCode.BAD_REQUEST == response.getStatus()) {
                return true;
            }

            return false;
        }

    }

}
