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

import org.codehaus.jackson.map.ObjectMapper;
import org.openo.baseservice.remoteservice.exception.ServiceException;
import org.openo.sdno.exception.InnerErrorServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class of restful transfer util.<br/>
 * 
 * @author
 * @version SDNO 0.5 Jan 11, 2017
 */
public class RestTransferUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(RestTransferUtil.class);

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private RestTransferUtil() {
    }

    /**
     * Transfer response.<br/>
     * 
     * @param responseJons Json string
     * @param responseClass response class.
     * @return transfer result
     * @throws ServiceException when transfer failed
     * @since SDNO 0.5
     */
    public static <T> T transferResponse(String responseJons, Class<T> responseClass) throws ServiceException {
        try {
            return MAPPER.readValue(responseJons, responseClass);
        } catch(Exception e) {
            LOGGER.error("transferResponse exception. e: ", e);
            throw new InnerErrorServiceException("transferResponse failed!");
        }
    }

    /**
     * Transfer request.<br/>
     * 
     * @param pojoObject object
     * @return transfer result
     * @throws ServiceException when transfer failed
     * @since SDNO 0.5
     */
    public static String transferRequest(Object pojoObject) throws ServiceException {
        try {
            return MAPPER.writeValueAsString(pojoObject);
        } catch(Exception e) {
            LOGGER.error("transferRequest exception. e: ", e);
            throw new InnerErrorServiceException("transferRequest failed!");
        }
    }
}
