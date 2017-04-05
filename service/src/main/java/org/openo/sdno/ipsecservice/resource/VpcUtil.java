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

import java.text.MessageFormat;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.type.TypeReference;
import org.openo.baseservice.remoteservice.exception.ServiceException;
import org.openo.baseservice.roa.util.restclient.RestfulParametes;
import org.openo.baseservice.roa.util.restclient.RestfulResponse;
import org.openo.sdno.framework.container.resthelper.RestfulProxy;
import org.openo.sdno.framework.container.util.JsonUtil;
import org.openo.sdno.ipsecservice.model.consts.AdapterUrlConst;
import org.openo.sdno.overlayvpn.model.netmodel.vpc.Subnet;
import org.openo.sdno.overlayvpn.model.netmodel.vpc.Vpc;
import org.openo.sdno.overlayvpn.security.authentication.HttpContext;
import org.openo.sdno.rest.ResponseUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

/**
 * Vpc util class.<br/>
 * 
 * @author
 * @version SDNO 0.5 Mar 4, 2017
 */
public class VpcUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(VpcUtil.class);

    /**
     * Query Vpc By Id.<br>
     * 
     * @param vpcId Vpc Id
     * @return Vpc queried out
     * @throws ServiceException when query failed
     * @since SDNO 0.5
     */
    public static Vpc queryById(String vpcId) throws ServiceException {
        if(StringUtils.isEmpty(vpcId)) {
            LOGGER.error("Query vpc fail, vpcId is invalid");
            throw new ServiceException("Query vpc fail, vpcId is invalid");
        }

        RestfulParametes parameter = new RestfulParametes();
        parameter.putHttpContextHeader(HttpContext.CONTENT_TYPE_HEADER, HttpContext.MEDIA_TYPE_JSON);
        String url = MessageFormat.format(AdapterUrlConst.QUERY_VPC_URL, vpcId);

        RestfulResponse response = RestfulProxy.get(url, parameter);
        String rspContent = ResponseUtils.transferResponse(response);
        Vpc resultVpc = JsonUtil.fromJson(rspContent, Vpc.class);

        return resultVpc;
    }

    /**
     * Query subnets by vpc id.<br>
     * 
     * @param vpcId Vpc Id
     * @return List of subnets queried out
     * @throws ServiceException when query failed
     * @since SDNO 0.5
     */
    public static List<Subnet> querySubnetByVpcId(String vpcId) throws ServiceException {
        if(StringUtils.isEmpty(vpcId)) {
            LOGGER.error("Query subnet fail, vpcId is invalid");
            throw new ServiceException("Query subnet fail, vpcId is invalid");
        }

        RestfulParametes parameter = new RestfulParametes();
        parameter.putHttpContextHeader(HttpContext.CONTENT_TYPE_HEADER, HttpContext.MEDIA_TYPE_JSON);
        parameter.put("vpcId", vpcId);

        RestfulResponse response = RestfulProxy.get(AdapterUrlConst.QUERY_SUBNET_URL, parameter);
        String rspContent = ResponseUtils.transferResponse(response);
        List<Subnet> subnetList = JsonUtil.fromJson(rspContent, new TypeReference<List<Subnet>>() {});
        if(CollectionUtils.isEmpty(subnetList)) {
            LOGGER.error("No subnets queried out in this vpc");
            throw new ServiceException("No subnets queried out in this vpc");
        }

        return subnetList;
    }
}
