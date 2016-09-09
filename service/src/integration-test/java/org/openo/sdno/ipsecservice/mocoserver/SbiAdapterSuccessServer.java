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

package org.openo.sdno.ipsecservice.mocoserver;

import java.util.List;

import org.codehaus.jackson.type.TypeReference;
import org.openo.sdno.framework.container.util.JsonUtil;
import org.openo.sdno.overlayvpn.errorcode.ErrorCode;
import org.openo.sdno.overlayvpn.model.netmodel.ipsec.DcGwIpSecConnection;
import org.openo.sdno.overlayvpn.model.netmodel.ipsec.NeIpSecConnection;
import org.openo.sdno.overlayvpn.result.ResultRsp;
import org.openo.sdno.testframework.http.model.HttpRequest;
import org.openo.sdno.testframework.http.model.HttpResponse;
import org.openo.sdno.testframework.http.model.HttpRquestResponse;
import org.openo.sdno.testframework.moco.MocoHttpServer;
import org.openo.sdno.testframework.moco.responsehandler.MocoResponseHandler;

public class SbiAdapterSuccessServer extends MocoHttpServer {

    private static final String CREATE_IPSEC_SUCCESS_IN_AC_FILE =
            "src/integration-test/resources/acsbiadapter/createipsecsuccess.json";

    private static final String DELETE_IPSEC_SUCCESS_IN_AC_FILE =
            "src/integration-test/resources/acsbiadapter/deleteipsecsuccess.json";

    private static final String QUERY_WANSUBINF_IN_AC_SUCCESS_FILE =
            "src/integration-test/resources/acsbiadapter/querywansubinterfacesuccess.json";

    private static final String CREATE_IPSEC_SUCCESS_IN_OS_FILE =
            "src/integration-test/resources/ossbiadapter/createipsecsuccess.json";

    private static final String DELETE_IPSEC_SUCCESS_IN_OS_FILE =
            "src/integration-test/resources/ossbiadapter/deleteipsecsuccess.json";

    public SbiAdapterSuccessServer() {

    }

    public SbiAdapterSuccessServer(String configFile) {
        super(configFile);
    }

    @Override
    public void addRequestResponsePairs() {

        this.addRequestResponsePair(CREATE_IPSEC_SUCCESS_IN_AC_FILE, new CreateIpSecSuccessInAcResponseHandler());

        this.addRequestResponsePair(DELETE_IPSEC_SUCCESS_IN_AC_FILE);

        this.addRequestResponsePair(QUERY_WANSUBINF_IN_AC_SUCCESS_FILE);

        this.addRequestResponsePair(CREATE_IPSEC_SUCCESS_IN_OS_FILE, new CreateIpSecSuccessInOsResponseHandler());

        this.addRequestResponsePair(DELETE_IPSEC_SUCCESS_IN_OS_FILE);
    }

    private class CreateIpSecSuccessInAcResponseHandler extends MocoResponseHandler {

        @Override
        public void processRequestandResponse(HttpRquestResponse httpObject) {

            HttpRequest httpRequest = httpObject.getRequest();
            HttpResponse httpResponse = httpObject.getResponse();
            List<NeIpSecConnection> inputInstanceList =
                    JsonUtil.fromJson(httpRequest.getData(), new TypeReference<List<NeIpSecConnection>>() {});

            ResultRsp<List<NeIpSecConnection>> newResult =
                    new ResultRsp<List<NeIpSecConnection>>(ErrorCode.OVERLAYVPN_SUCCESS);

            newResult.setData(inputInstanceList);
            httpResponse.setData(JsonUtil.toJson(newResult));
        }
    }

    private class CreateIpSecSuccessInOsResponseHandler extends MocoResponseHandler {

        @Override
        public void processRequestandResponse(HttpRquestResponse httpObject) {

            HttpRequest httpRequest = httpObject.getRequest();
            HttpResponse httpResponse = httpObject.getResponse();
            List<DcGwIpSecConnection> inputInstanceList =
                    JsonUtil.fromJson(httpRequest.getData(), new TypeReference<List<DcGwIpSecConnection>>() {});

            ResultRsp<List<DcGwIpSecConnection>> newResult =
                    new ResultRsp<List<DcGwIpSecConnection>>(ErrorCode.OVERLAYVPN_SUCCESS);

            newResult.setData(inputInstanceList);
            httpResponse.setData(JsonUtil.toJson(newResult));
        }
    }

}
