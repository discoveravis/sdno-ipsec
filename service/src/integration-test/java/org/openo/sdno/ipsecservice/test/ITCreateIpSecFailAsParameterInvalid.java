
package org.openo.sdno.ipsecservice.test;

import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
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

public class ITCreateIpSecFailAsParameterInvalid extends TestManager {

    private static SbiAdapterSuccessServer sbiAdapterServer = new SbiAdapterSuccessServer();

    private static final String CREATE_IPSEC_FAIL_TESTCASE_1 =
            "src/integration-test/resources/testcase/createipsecfail1.json";

    private static final String CREATE_IPSEC_FAIL_TESTCASE_2 =
            "src/integration-test/resources/testcase/createipsecfail2.json";

    private static final String CREATE_IPSEC_FAIL_TESTCASE_3 =
            "src/integration-test/resources/testcase/createipsecfail3.json";

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
    public void testCreateIpSecFailAsUuidInvalid() throws ServiceException {
        try {
            // test create
            HttpRquestResponse httpCreateObject =
                    HttpModelUtils.praseHttpRquestResponseFromFile(CREATE_IPSEC_FAIL_TESTCASE_1);
            HttpRequest createRequest = httpCreateObject.getRequest();
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

    @Test
    public void testCreateIpSecFailAsNoConnections() throws ServiceException {
        try {
            // test create
            HttpRquestResponse httpCreateObject =
                    HttpModelUtils.praseHttpRquestResponseFromFile(CREATE_IPSEC_FAIL_TESTCASE_2);
            HttpRequest createRequest = httpCreateObject.getRequest();
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

    @Test
    public void testCreateIpSecFailAsNoTwoHubEpgs() throws ServiceException {
        try {
            // test create
            HttpRquestResponse httpCreateObject =
                    HttpModelUtils.praseHttpRquestResponseFromFile(CREATE_IPSEC_FAIL_TESTCASE_3);
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

    @Test
    public void testCreateIpSecFailAsNeidInvalid() throws ServiceException {
        try {
            // test create
            HttpRquestResponse httpCreateObject =
                    HttpModelUtils.praseHttpRquestResponseFromFile(CREATE_IPSEC_FAIL_TESTCASE_3);
            HttpRequest createRequest = httpCreateObject.getRequest();
            OverlayVpn newVpnData = JsonUtil.fromJson(createRequest.getData(), OverlayVpn.class);
            List<Connection> connectionList = newVpnData.getVpnConnections();
            List<EndpointGroup> epgList = connectionList.get(0).getEndpointGroups();
            epgList.get(0).setNeId("119988");
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

    @Ignore
    public void testCreateIpSecFailAsNoConnections1() throws ServiceException {
        try {
            // test create
            HttpRquestResponse httpCreateObject =
                    HttpModelUtils.praseHttpRquestResponseFromFile(CREATE_IPSEC_FAIL_TESTCASE_2);
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
            if(HttpCode.ERR_FAILED == response.getStatus()
                    && (response.getData().contains(ErrorCode.OVERLAYVPN_PARAMETER_INVALID)
                            || response.getData().contains(ErrorCode.DB_RETURN_ERROR))) {
                return true;
            }

            return false;
        }

    }

}
