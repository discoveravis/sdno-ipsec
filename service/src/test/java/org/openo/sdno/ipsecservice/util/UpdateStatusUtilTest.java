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

package org.openo.sdno.ipsecservice.util;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.openo.baseservice.remoteservice.exception.ServiceException;
import org.openo.sdno.ipsecservice.model.enums.OperationType;
import org.openo.sdno.ipsecservice.util.site2dc.UpdateStatusUtil;
import org.openo.sdno.overlayvpn.model.v2.ipsec.NbiIpSec;
import org.openo.sdno.overlayvpn.model.v2.ipsec.SbiNeIpSec;

/**
 * <br/>
 * <p>
 * </p>
 * 
 * @author
 * @version SDNO 0.5 Jan 26, 2017
 */
public class UpdateStatusUtilTest {

    /**
     * <br/>
     * 
     * @throws java.lang.Exception
     * @since SDNO 0.5
     */
    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testUpdateNbiIpsecsException() throws ServiceException {
        OperationType operType = OperationType.CREATE;
        List<SbiNeIpSec> sbiNeTunnels = new ArrayList<SbiNeIpSec>();
        List<NbiIpSec> nbiIpsecs = new ArrayList<NbiIpSec>();
        NbiIpSec nbiIpsec = new NbiIpSec();
        nbiIpsec.setUuid("1");
        nbiIpsec.setSrcNeId("srcNeId");
        nbiIpsec.setDestNeId("destNeId");
        nbiIpsecs.add(nbiIpsec);

        SbiNeIpSec sbiIpsec = new SbiNeIpSec();
        sbiIpsec.setUuid("2");
        sbiNeTunnels.add(sbiIpsec);
        try {
            UpdateStatusUtil.updateNbiIpsecs(nbiIpsecs, sbiNeTunnels, operType);
            assertTrue(false);
        } catch(ServiceException e) {
            assertTrue(true);
        }
    }

}
