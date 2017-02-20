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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.codehaus.jackson.type.TypeReference;
import org.openo.baseservice.remoteservice.exception.ServiceException;
import org.openo.baseservice.roa.util.restclient.RestfulParametes;
import org.openo.baseservice.roa.util.restclient.RestfulResponse;
import org.openo.sdno.exception.HttpCode;
import org.openo.sdno.framework.container.resthelper.RestfulProxy;
import org.openo.sdno.framework.container.util.JsonUtil;
import org.openo.sdno.ipsecservice.sbi.inf.WanSubInfSbiService;
import org.openo.sdno.overlayvpn.consts.CommConst;
import org.openo.sdno.overlayvpn.consts.UrlAdapterConst;
import org.openo.sdno.overlayvpn.errorcode.ErrorCode;
import org.openo.sdno.overlayvpn.model.port.WanSubInterface;
import org.openo.sdno.overlayvpn.result.ResultRsp;
import org.openo.sdno.overlayvpn.security.authentication.HttpContext;
import org.openo.sdno.overlayvpn.security.authentication.TokenDataHolder;
import org.openo.sdno.rest.ResponseUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wan south branch interface implementation. <br>
 * 
 * @author
 * @version SDNO 0.5 June 21, 2016
 */
public class WanSubInfSbiServiceImpl implements WanSubInfSbiService {

    private static final Logger LOGGER = LoggerFactory.getLogger(WanSubInfSbiServiceImpl.class);

    @Override
    public ResultRsp<List<WanSubInterface>> queryNeWanSubInterface(String ctrlUuid, String deviceId,
            String subInterUsedType) throws ServiceException {
        RestfulParametes restfulParametes = getQueryWanInterfaceParam(subInterUsedType, ctrlUuid);
        String queryUrl = UrlAdapterConst.WAN_INTERFACE_ADAPTER_BASE_URL
                + MessageFormat.format(UrlAdapterConst.QUERY_WAN_INTERFACE, deviceId);
        LOGGER.info("queryNeWanSubInterface begin: " + queryUrl + "\n" + restfulParametes.getRawData());

        RestfulResponse response = RestfulProxy.get(queryUrl, restfulParametes);
        if(response.getStatus() == HttpCode.NOT_FOUND) {
            return new ResultRsp<>(ErrorCode.RESTFUL_COMMUNICATION_FAILED, null, null,
                    "connect to controller failed", "connect to controller failed, please check");
        }
        String content = ResponseUtils.transferResponse(response);
        ResultRsp<List<WanSubInterface>> result =
                JsonUtil.fromJson(content, new TypeReference<ResultRsp<List<WanSubInterface>>>() {});
        LOGGER.info("queryNeWanSubInterface end, result = " + result.toString());

        return result;
    }

    private RestfulParametes getQueryWanInterfaceParam(String subInterUsedType, String ctrlUuid) {
        RestfulParametes restfulParametes = new RestfulParametes();
        restfulParametes.putHttpContextHeader(HttpContext.CONTENT_TYPE_HEADER, HttpContext.MEDIA_TYPE_JSON);
        restfulParametes.putHttpContextHeader("X-Driver-Parameter", "extSysID=" + ctrlUuid);
        TokenDataHolder.addToken2HttpRequest(restfulParametes);

        Map<String, String> queryParamMap = new ConcurrentHashMap<>();
        queryParamMap.put(CommConst.DEVICE_WAN_SUB_INTERFACE_TYPE_PARAMETER, subInterUsedType);
        restfulParametes.setParamMap(queryParamMap);

        return restfulParametes;
    }

}
