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
