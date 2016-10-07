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

package org.openo.sdno.ipsecservice.sbi.inf;

import java.util.List;

import org.openo.baseservice.remoteservice.exception.ServiceException;
import org.openo.sdno.framework.container.service.IService;
import org.openo.sdno.overlayvpn.model.netmodel.ipsec.DcGwIpSecConnection;
import org.openo.sdno.overlayvpn.result.ResultRsp;

/**
 * DC gateway controller south branch interface. <br>
 * 
 * @author
 * @version SDNO 0.5 Jun 22, 2016
 */
public interface DcGwIpSecConnSbiService extends IService {

    /**
     * It is used to create ipsec connection. <br>
     * 
     * @param ipSecConnectionList The ipsec connection data
     * @return The create result
     * @throws ServiceException When create failed.
     * @since SDNO 0.5
     */
    ResultRsp<List<DcGwIpSecConnection>> createIpSecNeConnection(List<DcGwIpSecConnection> ipSecConnectionList)
            throws ServiceException;

    /**
     * It is used to delete ipsec connection. <br>
     * 
     * @param ipSecConnectionList The ipsec connection data
     * @return The delete result
     * @throws ServiceException When create failed.
     * @since SDNO 0.5
     */
    ResultRsp<String> deleteIpSecConnection(List<DcGwIpSecConnection> ipSecConnectionList) throws ServiceException;
}
