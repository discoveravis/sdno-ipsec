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

package org.openo.sdno.ipsecservice.resource;

import org.openo.baseservice.remoteservice.exception.ServiceException;
import org.openo.baseservice.roa.util.restclient.RestfulParametes;
import org.openo.baseservice.roa.util.restclient.RestfulResponse;
import org.openo.sdno.exception.InnerErrorServiceException;
import org.openo.sdno.framework.container.resthelper.RestfulProxy;
import org.openo.sdno.framework.container.util.JsonUtil;
import org.openo.sdno.ipsecservice.model.consts.AdapterUrlConst;
import org.openo.sdno.overlayvpn.model.netmodel.vpc.Vpc;
import org.openo.sdno.overlayvpn.security.authentication.HttpContext;
import org.openo.sdno.rest.ResponseUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Vpc util class.<br/>
 * 
 * @author
 * @version SDNO 0.5 Mar 4, 2017
 */
public class VpcUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(VpcUtil.class);

    @SuppressWarnings("unchecked")
    public static Vpc queryById(String vpcId) throws ServiceException {
        if(null == vpcId) {
            LOGGER.error("query vpc fail. vpcId is null");
        }
        RestfulParametes para = new RestfulParametes();
        para.putHttpContextHeader(HttpContext.CONTENT_TYPE_HEADER, HttpContext.MEDIA_TYPE_JSON);
        String url = AdapterUrlConst.QUERY_VPC_URL + vpcId;

        try {
            RestfulResponse response = RestfulProxy.get(url, para);

            String rspContent = ResponseUtils.transferResponse(response);
            Vpc rsp = JsonUtil.fromJson(rspContent, Vpc.class);

            return rsp;
        } catch(ServiceException e) {
            LOGGER.error("query port ip exception. e: ", e);
            throw new InnerErrorServiceException("query ne failed!");
        }

    }
}
