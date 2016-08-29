/*
 * Copyright (c) 2016, Huawei Technologies Co., Ltd.
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

package org.openo.sdno.ipsecservice.util.operation;

import org.openo.baseservice.roa.util.restclient.RestfulParametes;
import org.openo.sdno.overlayvpn.security.authentication.HttpContext;
import org.openo.sdno.overlayvpn.security.authentication.TokenDataHolder;

/**
 * Build restful parameters object for restful request.<br/>
 * 
 * @author
 * @version     SDNO 0.5  Aug 22, 2016
 */
public class RestfulParametesUtil {
    
    private RestfulParametesUtil() {
    }
    
    /**
     * Get restful parameters with body data.<br/>
     * 
     * @param bodyData Creating body data
     * @param ctrlUuid Controller UUID
     * @return Restful parameters object
     * @since  SDNO 0.5
     */
    public static RestfulParametes getRestfulParametesWithBody(String bodyData, String ctrlUuid) {
        RestfulParametes restfulParametes = new RestfulParametes();

        restfulParametes.putHttpContextHeader(HttpContext.CONTENT_TYPE_HEADER, HttpContext.MEDIA_TYPE_JSON);
        restfulParametes.putHttpContextHeader("X-Driver-Parameter", "extSysID=" + ctrlUuid);
        TokenDataHolder.addToken2HttpRequest(restfulParametes);
        restfulParametes.setRawData(bodyData);

        return restfulParametes;
    }
    
    /**
     * Get restful parameters object.<br/>
     * 
     * @param ctrlUuid Controller UUID
     * @return Restful parameters object
     * @since  SDNO 0.5
     */
    public static RestfulParametes getRestfulParametes(String ctrlUuid) {
        RestfulParametes restfulParametes = new RestfulParametes();
        restfulParametes.putHttpContextHeader(HttpContext.CONTENT_TYPE_HEADER, HttpContext.MEDIA_TYPE_JSON);
        restfulParametes.putHttpContextHeader("X-Driver-Parameter", "extSysID=" + ctrlUuid);
        TokenDataHolder.addToken2HttpRequest(restfulParametes);
        return restfulParametes;
    }

}
