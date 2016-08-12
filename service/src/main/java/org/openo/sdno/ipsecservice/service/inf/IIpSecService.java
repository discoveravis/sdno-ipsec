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

package org.openo.sdno.ipsecservice.service.inf;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openo.baseservice.remoteservice.exception.ServiceException;
import org.openo.sdno.framework.container.service.IService;
import org.openo.sdno.overlayvpn.brs.model.NetworkElementMO;
import org.openo.sdno.overlayvpn.model.servicemodel.OverlayVpn;
import org.openo.sdno.overlayvpn.result.ResultRsp;

/**
 * IpSec service interface. <br/>
 * 
 * @author
 * @version SDNO 0.5 Jun 16, 2016
 */
public interface IIpSecService extends IService {

    /**
     * Create ipsec operation. <br/>
     * 
     * @param req HttpServletRequest Object
     * @param resp HttpServletResponse Object
     * @param overlayVpn The object of OverlayVpn that contain ipsec
     * @param neIdToNeMap NE ID to NE information mapping
     * @return The object of ResultRsp
     * @throws ServiceException When create ipsec failed
     * @since SDNO 0.5
     */
    ResultRsp<OverlayVpn> create(HttpServletRequest req, HttpServletResponse resp, OverlayVpn overlayVpn,
            Map<String, NetworkElementMO> neIdToNeMap) throws ServiceException;

    /**
     * Query ipsec operation. <br/>
     * 
     * @param req HttpServletRequest Object
     * @param resp HttpServletResponse Object
     * @param connectionId The uuid of connection
     * @return The object of ResultRsp
     * @throws ServiceException When query ipsec failed
     * @since SDNO 0.5
     */
    ResultRsp<OverlayVpn> query(HttpServletRequest req, HttpServletResponse resp, String connectionId)
            throws ServiceException;

    /**
     * Delete ipsec operation. <br/>
     * 
     * @param req HttpServletRequest Object
     * @param resp HttpServletResponse Object
     * @param connectionId The uuid of connection
     * @return The object of ResultRsp
     * @throws ServiceException When delete ipsec failed
     * @since SDNO 0.5
     */
    ResultRsp<String> delete(HttpServletRequest req, HttpServletResponse resp, String connectionId)
            throws ServiceException;

}
