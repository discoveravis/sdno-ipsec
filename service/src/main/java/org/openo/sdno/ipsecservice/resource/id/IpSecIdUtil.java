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

package org.openo.sdno.ipsecservice.resource.id;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.openo.sdno.framework.container.util.UuidUtils;
import org.openo.sdno.overlayvpn.model.v2.ipsec.NbiIpSec;
import org.springframework.util.CollectionUtils;

/**
 * <br/>
 * <p>
 * </p>
 * 
 * @author
 * @version SDNO 0.5 Jan 10, 2017
 */
public class IpSecIdUtil {

    private IpSecIdUtil() {

    }

    public static List<String> allocTunnelListUuid(List<NbiIpSec> ipsecCons) {
        List<String> inputUuidList = new ArrayList<String>();
        if(CollectionUtils.isEmpty(ipsecCons)) {
            return inputUuidList;
        }

        for(NbiIpSec tmpTunnel : ipsecCons) {
            if(StringUtils.isNotEmpty(tmpTunnel.getUuid())) {
                inputUuidList.add(tmpTunnel.getUuid());
            } else {
                tmpTunnel.setUuid(UuidUtils.createUuid());
            }
        }

        return inputUuidList;
    }

    public static void reAllocTunnelListId(List<NbiIpSec> newTunnels, List<NbiIpSec> dbTunnels) {
        if(CollectionUtils.isEmpty(dbTunnels) || CollectionUtils.isEmpty(newTunnels)) {
            return;
        }

        Set<String> dbUuidSet = new HashSet<String>();
        for(NbiIpSec tmpDbTunnel : dbTunnels) {
            dbUuidSet.add(tmpDbTunnel.getUuid());
        }

        for(NbiIpSec tmpGreTunnel : newTunnels) {
            String tempUuid = tmpGreTunnel.getUuid();
            if(dbUuidSet.contains(tempUuid)) {
                String tempNewUuid = UuidUtils.createUuid();
                while(dbUuidSet.contains(tempNewUuid)) {
                    tempNewUuid = UuidUtils.createUuid();
                }
                tmpGreTunnel.setUuid(tempNewUuid);
            }
        }
    }

}
