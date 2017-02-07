/*
 * Copyright (c) 2017, Huawei Technologies Co., Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openo.sdno.ipsecservice.util.site2dc;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.openo.baseservice.remoteservice.exception.ServiceException;
import org.openo.baseservice.roa.util.restclient.RestfulParametes;
import org.openo.sdno.ipsecservice.model.consts.AdapterUrlConst;
import org.openo.sdno.overlayvpn.model.v2.ipsec.SbiNeIpSec;
import org.openo.sdno.overlayvpn.security.authentication.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <br/>
 * <p>
 * </p>
 * 
 * @author
 * @version SDNO 0.5 Jan 7, 2017
 */
public class RestParameterUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(RestParameterUtil.class);

    private RestParameterUtil() {
    }

    public static RestfulParametes getQueryPortIpParam(String portName, String ctrlId) {
        RestfulParametes para = new RestfulParametes();
        para.putHttpContextHeader(HttpContext.CONTENT_TYPE_HEADER, HttpContext.MEDIA_TYPE_JSON);
        para.putHttpContextHeader("X-Driver-Parameter", "extSysID=" + ctrlId);
        Map<String, String> queryMap = new ConcurrentHashMap<String, String>();
        queryMap.put("portName", portName);
        para.setParamMap(queryMap);

        return para;
    }

    public static RestfulParametes getCreateGreTunnelParam(List<SbiNeIpSec> sbiNeIpsecList) throws ServiceException {
        RestfulParametes para = new RestfulParametes();
        para.put("resource", AdapterUrlConst.BATCH_CREATE_IPSECS);
        String strJsonReq = RestTransferUtil.transferRequest(sbiNeIpsecList);
        String ctrlId = sbiNeIpsecList.get(0).getControllerId();
        para.putHttpContextHeader(HttpContext.CONTENT_TYPE_HEADER, HttpContext.MEDIA_TYPE_JSON);
        para.putHttpContextHeader("X-Driver-Parameter", "extSysID=" + ctrlId);
        para.setRawData(strJsonReq);

        return para;
    }

    public static RestfulParametes getUpdateIpsecParam(SbiNeIpSec sbiNeIpsec) throws ServiceException {
        RestfulParametes para = new RestfulParametes();
        String ctrlId = sbiNeIpsec.getControllerId();
        para.putHttpContextHeader(HttpContext.CONTENT_TYPE_HEADER, HttpContext.MEDIA_TYPE_JSON);
        para.putHttpContextHeader("X-Driver-Parameter", "extSysID=" + ctrlId);
        String strJsonReq = RestTransferUtil.transferRequest(sbiNeIpsec);
        para.setRawData(strJsonReq);

        return para;
    }

}
