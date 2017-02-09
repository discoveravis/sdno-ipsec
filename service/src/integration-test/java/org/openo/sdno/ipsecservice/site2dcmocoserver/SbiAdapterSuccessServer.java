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

package org.openo.sdno.ipsecservice.site2dcmocoserver;

import java.util.List;

import org.codehaus.jackson.type.TypeReference;
import org.openo.sdno.framework.container.util.JsonUtil;
import org.openo.sdno.overlayvpn.errorcode.ErrorCode;
import org.openo.sdno.overlayvpn.model.v2.ipsec.SbiNeIpSec;
import org.openo.sdno.overlayvpn.result.ResultRsp;
import org.openo.sdno.testframework.http.model.HttpRequest;
import org.openo.sdno.testframework.http.model.HttpResponse;
import org.openo.sdno.testframework.http.model.HttpRquestResponse;
import org.openo.sdno.testframework.moco.MocoHttpServer;
import org.openo.sdno.testframework.moco.responsehandler.MocoResponseHandler;

/**
 * SbiAdapterSuccessServer class for success test cases. <br>
 * 
 * @author
 * @version SDNO 0.5 June 16, 2016
 */
public class SbiAdapterSuccessServer extends MocoHttpServer {

    private static final String CREATE_IPSEC_SUCCESS_IN_AC_FILE =
            "src/integration-test/resources/site2dcacsbiadapter/createipsecsuccess.json";

    private static final String DELETE_IPSEC_SUCCESS_IN_AC_FILE =
            "src/integration-test/resources/site2dcacsbiadapter/deleteipsecsuccess.json";

    private static final String CREATE_IPSEC_SUCCESS_IN_FS_FILE =
            "src/integration-test/resources/site2dcfssbiadapter/createipsecsuccess.json";

    private static final String DELETE_IPSEC_SUCCESS_IN_FS_FILE =
            "src/integration-test/resources/site2dcfssbiadapter/deleteipsecsuccess.json";

    private static final String UPDATE_IPSEC_SUCCESS_IN_FS_FILE =
            "src/integration-test/resources/site2dcfssbiadapter/updateipsecsuccess.json";

    private static final String QUERY_PORT_IP_SUCCESS_FILE =
            "src/integration-test/resources/site2dcportsbiadapter/queryportipsuccess.json";

    public SbiAdapterSuccessServer() {
        // super(12307);
    }

    @Override
    public void addRequestResponsePairs() {

        this.addRequestResponsePair(CREATE_IPSEC_SUCCESS_IN_AC_FILE, new CreateIpSecSuccessInAcResponseHandler());

        this.addRequestResponsePair(DELETE_IPSEC_SUCCESS_IN_AC_FILE, new CreateIpSecSuccessInAcResponseHandler());

        this.addRequestResponsePair(QUERY_PORT_IP_SUCCESS_FILE);

        this.addRequestResponsePair(CREATE_IPSEC_SUCCESS_IN_FS_FILE, new CreateIpSecSuccessInFsResponseHandler());

        this.addRequestResponsePair(DELETE_IPSEC_SUCCESS_IN_FS_FILE, new CreateIpSecSuccessInFsResponseHandler());

        this.addRequestResponsePair(UPDATE_IPSEC_SUCCESS_IN_FS_FILE, new CreateIpSecSuccessInFsResponseHandler());
    }

    private class CreateIpSecSuccessInAcResponseHandler extends MocoResponseHandler {

        @Override
        public void processRequestandResponse(HttpRquestResponse httpObject) {

            HttpRequest httpRequest = httpObject.getRequest();
            HttpResponse httpResponse = httpObject.getResponse();
            List<SbiNeIpSec> inputInstanceList =
                    JsonUtil.fromJson(httpRequest.getData(), new TypeReference<List<SbiNeIpSec>>() {});

            ResultRsp<SbiNeIpSec> newResult = new ResultRsp<SbiNeIpSec>(ErrorCode.OVERLAYVPN_SUCCESS);

            newResult.setData(inputInstanceList.get(0));
            newResult.setSuccessed(inputInstanceList);
            httpResponse.setData(JsonUtil.toJson(newResult));
        }
    }

    private class CreateIpSecSuccessInFsResponseHandler extends MocoResponseHandler {

        @Override
        public void processRequestandResponse(HttpRquestResponse httpObject) {

            HttpRequest httpRequest = httpObject.getRequest();
            HttpResponse httpResponse = httpObject.getResponse();
            List<SbiNeIpSec> inputInstanceList =
                    JsonUtil.fromJson(httpRequest.getData(), new TypeReference<List<SbiNeIpSec>>() {});

            ResultRsp<SbiNeIpSec> newResult = new ResultRsp<SbiNeIpSec>(ErrorCode.OVERLAYVPN_SUCCESS);

            newResult.setData(inputInstanceList.get(0));
            newResult.setSuccessed(inputInstanceList);
            httpResponse.setData(JsonUtil.toJson(newResult));
        }
    }

}
