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

package org.openo.sdno.ipsecservice.util.site2dc.checkres;

import java.util.Iterator;
import java.util.List;

import org.openo.sdno.framework.container.util.JsonUtil;
import org.openo.sdno.overlayvpn.model.v2.ipsec.SbiIp;
import org.openo.sdno.overlayvpn.model.v2.ipsec.SbiNeIpSec;

/**
 * <br/>
 * <p>
 * </p>
 * 
 * @author
 * @version SDNO 0.5 Jan 11, 2017
 */
public class CheckIpSecConSrcSnkSameUtil {

    private CheckIpSecConSrcSnkSameUtil() {
    }

    public static boolean checkSrcSnkSame(List<SbiNeIpSec> activeIpsecs, Iterator<SbiNeIpSec> iter,
            SbiNeIpSec sbiNeIpSec, SbiNeIpSec dbSbiNeIpSec) {
        if(checkNeIdSame(sbiNeIpSec, dbSbiNeIpSec) && isIpSame(sbiNeIpSec, dbSbiNeIpSec)) {
            sbiNeIpSec.setExternalId(dbSbiNeIpSec.getExternalId());
            sbiNeIpSec.setExternalIpSecId(dbSbiNeIpSec.getExternalIpSecId());
            iter.remove();
            activeIpsecs.add(sbiNeIpSec);
            return true;
        }

        return false;

    }

    private static boolean checkNeIdSame(SbiNeIpSec sbiNeIpSec, SbiNeIpSec dbSbiNeIpSec) {
        return sbiNeIpSec.getNeId().equals(dbSbiNeIpSec.getNeId())
                && sbiNeIpSec.getPeerNeId().equals(dbSbiNeIpSec.getPeerNeId());
    }

    private static boolean isIpSame(SbiNeIpSec sbiNeIpSec, SbiNeIpSec dbSbiNeIpSec) {
        SbiIp srcIp = JsonUtil.fromJson(sbiNeIpSec.getSourceAddress(), SbiIp.class);
        SbiIp destIp = JsonUtil.fromJson(sbiNeIpSec.getPeerAddress(), SbiIp.class);

        SbiIp dbSrcIp = JsonUtil.fromJson(dbSbiNeIpSec.getSourceAddress(), SbiIp.class);
        SbiIp dbDestIp = JsonUtil.fromJson(dbSbiNeIpSec.getPeerAddress(), SbiIp.class);

        if(srcIp.equals(dbSrcIp) && destIp.equals(dbDestIp)) {
            return true;
        }

        return false;
    }
}
