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
import org.openo.sdno.overlayvpn.model.port.WanSubInterface;
import org.openo.sdno.overlayvpn.result.ResultRsp;

/**
 * Wan south branch interface. <br>
 * 
 * @author
 * @version SDNO 0.5 Jun 21, 2016
 */
public interface IWanSubInfSbiService extends IService {

    /**
     * It is used to query wan interface. <br>
     * 
     * @param ctrlUuid The uuid of controller
     * @param deviceId The device id
     * @param subInterUsedType The interface type
     * @return The list of wan interace
     * @throws ServiceException When query failed.
     * @since SDNO 0.5
     */
    ResultRsp<List<WanSubInterface>> queryNeWanSubInterface(String ctrlUuid, String deviceId, String subInterUsedType)
            throws ServiceException;

}
