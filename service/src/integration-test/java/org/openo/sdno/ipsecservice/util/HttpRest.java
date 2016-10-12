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

package org.openo.sdno.ipsecservice.util;

import java.util.Map;

import org.openo.baseservice.remoteservice.exception.ServiceException;
import org.openo.baseservice.roa.util.restclient.RestfulParametes;
import org.openo.baseservice.roa.util.restclient.RestfulResponse;
import org.openo.sdno.exception.HttpCode;
import org.openo.sdno.overlayvpn.errorcode.ErrorCode;
import org.openo.sdno.testframework.checker.IChecker;
import org.openo.sdno.testframework.http.model.HttpRequest;
import org.openo.sdno.testframework.http.model.HttpResponse;
import org.openo.sdno.testframework.restclient.HttpRestClient;

/**
 * HttpRest class. <br>
 * 
 * @author
 * @version SDNO 0.5 June 16, 2016
 */
public class HttpRest {

    public static RestfulResponse doSend(HttpRequest request) throws ServiceException {
        String url = request.getUri();
        String method = request.getMethod();
        String body = request.getData();

        HttpRestClient restClient = new HttpRestClient();

        final RestfulParametes restfulParametes = new RestfulParametes();

        Map<String, String> requestHeaders = request.getHeaders();
        if(null != requestHeaders) {
            for(Map.Entry<String, String> curEntity : requestHeaders.entrySet()) {
                restfulParametes.putHttpContextHeader(curEntity.getKey(), curEntity.getValue());
            }
        }

        Map<String, String> paramMap = request.getQueries();
        if(null != paramMap) {
            restfulParametes.setParamMap(paramMap);
        }

        if(null != body) {
            restfulParametes.setRawData(body);
        }

        switch(method) {
            case "post": {
                return restClient.post(url, restfulParametes);
            }
            case "get": {
                return restClient.get(url, restfulParametes);
            }
            case "put": {
                return restClient.put(url, restfulParametes);
            }
            case "delete": {
                return restClient.delete(url, restfulParametes);
            }
            case "head": {
                return restClient.head(url, restfulParametes);
            }
            case "patch": {
                return restClient.patch(url, restfulParametes);
            }
        }

        return null;
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
