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

package org.openo.sdno.ipsecservice.sbi.impl;

import java.text.MessageFormat;
import java.util.List;

import org.codehaus.jackson.type.TypeReference;
import org.openo.baseservice.remoteservice.exception.ServiceException;
import org.openo.baseservice.roa.util.restclient.RestfulParametes;
import org.openo.baseservice.roa.util.restclient.RestfulResponse;
import org.openo.sdno.exception.HttpCode;
import org.openo.sdno.framework.container.resthelper.RestfulProxy;
import org.openo.sdno.framework.container.util.JsonUtil;
import org.openo.sdno.ipsecservice.sbi.inf.INeIpSecConnSbiService;
import org.openo.sdno.ipsecservice.util.operation.RestfulParametesUtil;
import org.openo.sdno.overlayvpn.consts.UrlAdapterConst;
import org.openo.sdno.overlayvpn.errorcode.ErrorCode;
import org.openo.sdno.overlayvpn.model.netmodel.ipsec.NeIpSecConnection;
import org.openo.sdno.overlayvpn.result.ResultRsp;
import org.openo.sdno.rest.ResponseUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AC controller south branch interface implementation. <br>
 * 
 * @author
 * @version SDNO 0.5 Jun 22, 2016
 */
public class NeIpSecConnSbiService implements INeIpSecConnSbiService {

    private static final Logger LOGGER = LoggerFactory.getLogger(NeIpSecConnSbiService.class);

    @Override
    public ResultRsp<List<NeIpSecConnection>> createIpSecNeConnection(List<NeIpSecConnection> ipSecConnectionList)
            throws ServiceException {

        String ctrlUuid = ipSecConnectionList.get(0).getControllerId();
        RestfulParametes restfulParametes =
                RestfulParametesUtil.getRestfulParametesWithBody(JsonUtil.toJson(ipSecConnectionList), ctrlUuid);
        restfulParametes.put("resource", UrlAdapterConst.CREATE_AC_IPSEC_CONNECTION);

        String url = UrlAdapterConst.IPSEC_ADAPTER_BASE_URL + UrlAdapterConst.CREATE_AC_IPSEC_CONNECTION;

        LOGGER.info("createIpSecNeConnection begin: " + url + "\n" + restfulParametes.getRawData());

        RestfulResponse response = RestfulProxy.post(url, restfulParametes);
        if(response.getStatus() == HttpCode.NOT_FOUND) {
            return new ResultRsp<List<NeIpSecConnection>>(ErrorCode.RESTFUL_COMMUNICATION_FAILED, null, null,
                    "connect to controller failed", "connect to controller failed, please check");
        }

        String rspContent = ResponseUtils.transferResponse(response);
        ResultRsp<List<NeIpSecConnection>> restResult =
                JsonUtil.fromJson(rspContent, new TypeReference<ResultRsp<List<NeIpSecConnection>>>() {});

        LOGGER.info("createIpSecNeConnection end, result = " + restResult.toString());

        return restResult;
    }

    @Override
    public ResultRsp<String> deleteIpSecConnection(List<NeIpSecConnection> ipSecConnectionList)
            throws ServiceException {

        NeIpSecConnection ipSecConnection = ipSecConnectionList.get(0);
        String ctrlUuid = ipSecConnection.getControllerId();

        String deleteUrl = UrlAdapterConst.IPSEC_ADAPTER_BASE_URL
                + MessageFormat.format(UrlAdapterConst.DELETE_AC_IPSEC_CONNECTION, ipSecConnection.getUuid());

        LOGGER.info("deleteIpSecConnection begin: " + deleteUrl);

        RestfulResponse response = RestfulProxy.delete(deleteUrl, RestfulParametesUtil.getRestfulParametes(ctrlUuid));
        if(HttpCode.NOT_FOUND == response.getStatus()) {
            return new ResultRsp<String>(ErrorCode.RESTFUL_COMMUNICATION_FAILED, null, null,
                    "connect to ac controller failed", "connect to ac controller failed, please check");
        }

        String rspContent = ResponseUtils.transferResponse(response);
        ResultRsp<String> restResult = JsonUtil.fromJson(rspContent, new TypeReference<ResultRsp<String>>() {});

        LOGGER.info("deleteIpSecConnection end, result =  " + restResult.toString());

        return restResult;
    }
}
