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

package org.openo.sdno.ipsecservice.util.security;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;
import org.openo.baseservice.remoteservice.exception.ServiceException;
import org.openo.sdno.ipsecservice.util.exception.ThrowException;
import org.openo.sdno.overlayvpn.model.ipsec.IkePolicy;
import org.openo.sdno.overlayvpn.model.ipsec.IpSecPolicy;
import org.openo.sdno.overlayvpn.model.servicemodel.Connection;
import org.openo.sdno.overlayvpn.model.servicemodel.mappingpolicy.IpsecMappingPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Get security policy implementation. <br>
 * 
 * @author
 * @version SDNO 0.5 June 20, 2016
 */
public class Security {

    private static final Logger LOGGER = LoggerFactory.getLogger(Security.class);

    private static final String CFG_KEY = "cfgkey";

    private static final String CFG_VALUE = "cfgvalue";

    private static final String DOMAIN = "security";

    /**
     * Constructor<br>
     * 
     * @since SDNO 0.5
     */
    private Security() {
    }

    /**
     * It is used to get security policy. <br>
     * 
     * @param connection The object use for save the security policy
     * @throws ServiceException
     * @since SDNO 0.5
     */
    public static void getSecurity(Connection connection) throws ServiceException {
        IpsecMappingPolicy ipsecMappingPolicy = new IpsecMappingPolicy();
        List<Map<String, String>> values = getJsonDataFromFile(DOMAIN);
        getIkePolicy(values, ipsecMappingPolicy);
        getIpSecPolicy(values, ipsecMappingPolicy);
        getMappingPolicy(values, ipsecMappingPolicy);
        connection.setIpsecMappingPolicy(ipsecMappingPolicy);
    }

    private static String getValue(List<Map<String, String>> values, String cfgKey) {
        String result = null;
        for(Map<String, String> value : values) {
            String cfgKeyValue = value.get(CFG_KEY);
            if((cfgKeyValue != null) && cfgKeyValue.equals(cfgKey)) {
                result = value.get(CFG_VALUE);
                break;
            }
        }

        return result;
    }

    private static void getIkePolicy(List<Map<String, String>> values, IpsecMappingPolicy ipsecMappingPolicy) {
        IkePolicy ikePolicy = new IkePolicy();
        ikePolicy.setName(getValue(values, SecurityConfigKeyConst.IKE_POLICY_NAME));
        ikePolicy.setAuthAlgorithm(getValue(values, SecurityConfigKeyConst.IKE_AUTH_ALGORITHM));
        ikePolicy.setEncryptionAlgorithm(getValue(values, SecurityConfigKeyConst.IKE_ENCRYPTION_ALGORITHM));
        ikePolicy.setPfs(getValue(values, SecurityConfigKeyConst.IKE_PFS));
        ikePolicy.setLifeTime(getValue(values, SecurityConfigKeyConst.IKE_LIFETIME));
        ikePolicy.setIkeVersion(getValue(values, SecurityConfigKeyConst.IKE_VERSION));
        ipsecMappingPolicy.setIkePolicy(ikePolicy);
    }

    private static void getIpSecPolicy(List<Map<String, String>> values, IpsecMappingPolicy ipsecMappingPolicy) {
        IpSecPolicy ipSecPolicy = new IpSecPolicy();
        ipSecPolicy.setName(getValue(values, SecurityConfigKeyConst.IPSEC_POLICY_NAME));
        ipSecPolicy.setAuthAlgorithm(getValue(values, SecurityConfigKeyConst.IPSEC_AUTH_ALGORITHM));
        ipSecPolicy.setEncryptionAlgorithm(getValue(values, SecurityConfigKeyConst.IPSEC_ENCRYPTION_ALGORITHM));
        ipSecPolicy.setPfs(getValue(values, SecurityConfigKeyConst.IPSEC_PFS));
        ipSecPolicy.setLifeTime(getValue(values, SecurityConfigKeyConst.IPSEC_LIFETIME));
        ipSecPolicy.setTransformProtocol(getValue(values, SecurityConfigKeyConst.IPSEC_TRANSFORM_PROTOCOL));
        ipSecPolicy.setEncapsulationMode(getValue(values, SecurityConfigKeyConst.IPSEC_ENCAPSULTION_MODE));
        ipsecMappingPolicy.setIpSecPolicy(ipSecPolicy);
    }

    private static void getMappingPolicy(List<Map<String, String>> values, IpsecMappingPolicy ipsecMappingPolicy) {
        ipsecMappingPolicy.setName(getValue(values, SecurityConfigKeyConst.MAPPING_POLICY_NAME));
        ipsecMappingPolicy.setType(getValue(values, SecurityConfigKeyConst.MAPPING_TYPE));
        ipsecMappingPolicy.setRouteMode(getValue(values, SecurityConfigKeyConst.MAPPING_ROUTE_MODE));
        ipsecMappingPolicy.setAuthMode(getValue(values, SecurityConfigKeyConst.MAPPING_AUTH_MODE));
        ipsecMappingPolicy.setPsk(getValue(values, SecurityConfigKeyConst.MAPPING_PSK));
    }

    private static List<Map<String, String>> getJsonDataFromFile(String domain) throws ServiceException {
        try {
            String path = "generalconfig/" + domain + ".json";
            ObjectMapper mapper = new ObjectMapper();
            byte[] bytes = Files.readAllBytes(Paths.get(path));
            return mapper.readValue(bytes, List.class);
        } catch(IOException e) {
            LOGGER.warn("Get json file failed!", e);
            ThrowException.throwReadSecurityFileFailed();
        }
        return Collections.EMPTY_LIST;
    }

}
