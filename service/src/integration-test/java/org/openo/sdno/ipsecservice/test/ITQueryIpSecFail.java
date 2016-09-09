
package org.openo.sdno.ipsecservice.test;

import org.junit.Test;
import org.openo.baseservice.remoteservice.exception.ServiceException;
import org.openo.sdno.exception.HttpCode;
import org.openo.sdno.testframework.checker.IChecker;
import org.openo.sdno.testframework.http.model.HttpModelUtils;
import org.openo.sdno.testframework.http.model.HttpRequest;
import org.openo.sdno.testframework.http.model.HttpResponse;
import org.openo.sdno.testframework.http.model.HttpRquestResponse;
import org.openo.sdno.testframework.replace.PathReplace;
import org.openo.sdno.testframework.testmanager.TestManager;

public class ITQueryIpSecFail extends TestManager {

    private static final String QUERY_IPSEC_FAIL_TESTCASE =
            "src/integration-test/resources/testcase/queryipsecfail1.json";

    @Test
    public void testQueryIpSecFail() throws ServiceException {

        // test query a not existed data
        HttpRquestResponse queryHttpObject = HttpModelUtils.praseHttpRquestResponseFromFile(QUERY_IPSEC_FAIL_TESTCASE);
        HttpRequest queryRequest = queryHttpObject.getRequest();
        queryRequest.setUri(PathReplace.replaceUuid("connectionid", queryRequest.getUri(), "connection2id"));
        execTestCase(queryRequest, new SuccessChecker());
    }

    private class SuccessChecker implements IChecker {

        @Override
        public boolean check(HttpResponse response) {
            if(!HttpCode.isSucess(response.getStatus())) {
                return true;
            }

            return false;
        }

    }

}
