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

package org.openo.sdno.ipsecservice.util.operation;

import org.openo.baseservice.remoteservice.exception.ServiceException;
import org.openo.sdno.ipsecservice.util.exception.ThrowException;
import org.openo.sdno.overlayvpn.model.common.enums.TechnologyType;
import org.openo.sdno.overlayvpn.model.servicemodel.Connection;
import org.openo.sdno.overlayvpn.model.servicemodel.OverlayVpn;
import org.springframework.util.CollectionUtils;

/**
 * It is common util. <br>
 * 
 * @author
 * @version SDNO 0.5 Jun 20, 2016
 */
public class CommonUtil {

    /**
     * Constructor<br>
     * 
     * @since SDNO 0.5
     */
    private CommonUtil() {
    }

    /**
     * It is used to get ipsec connection from overlayvpn. <br>
     * 
     * @param overlayVpn The object of OverlayVpn
     * @return The ipsec connection
     * @throws ServiceException When miss ipsec connection in overlayvpn
     * @since SDNO 0.5
     */
    public static Connection getIpSecConnection(OverlayVpn overlayVpn) throws ServiceException {
        Connection retConnection = null;

        if(CollectionUtils.isEmpty(overlayVpn.getVpnConnections())) {
            ThrowException.throwParameterInvalid("Miss connection in overlayvpn");
        }

        for(Connection tempConnection : overlayVpn.getVpnConnections()) {
            if(TechnologyType.IPSEC.getName().equals(tempConnection.getTechnology())) {
                retConnection = tempConnection;
                break;
            }
        }

        if(null == retConnection) {
            ThrowException.throwParameterInvalid("Miss ipsec connection in overlayvpn");
        }

        return retConnection;
    }

    /**
     * It is used to check string1 and string2 are same or not. <br>
     * 
     * @param string1 The string1
     * @param string2 The string2
     * @return True when the strings are same
     * @since SDNO 0.5
     */
    public static boolean checkStringEqual(String string1, String string2) {
        if(null == string1 && null == string2) {
            return true;
        }

        if(null != string1 && string1.equals(string2)) {
            return true;
        }

        return false;
    }
}
