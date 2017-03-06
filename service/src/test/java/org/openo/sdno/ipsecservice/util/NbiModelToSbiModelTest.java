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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.type.TypeReference;
import org.junit.Before;
import org.junit.Test;
import org.openo.baseservice.remoteservice.exception.ServiceException;
import org.openo.sdno.framework.container.util.JsonUtil;
import org.openo.sdno.ipsecservice.util.site2dc.NbiModelToSbiModel;
import org.openo.sdno.overlayvpn.model.v2.ipsec.NbiIpSec;
import org.openo.sdno.overlayvpn.model.v2.ipsec.SbiNeIpSec;
import org.openo.sdno.overlayvpn.model.v2.result.ResultRsp;

/**
 * <br/>
 * <p>
 * </p>
 * 
 * @author
 * @version SDNO 0.5 Feb 6, 2017
 */
public class NbiModelToSbiModelTest {

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
    public void testConvertToNeIpsec() throws ServiceException {
        List<NbiIpSec> ipsecConnections = new ArrayList<NbiIpSec>();
        NbiIpSec nbiIpsec = new NbiIpSec();
        nbiIpsec.setUuid("1");
        nbiIpsec.setSrcNeId("srcNeId");
        nbiIpsec.setDestNeId("destNeId");
        ipsecConnections.add(nbiIpsec);
        Map<String, String> deviceIdToCtrollMap = new HashMap<String, String>();
        List<SbiNeIpSec> rst = NbiModelToSbiModel.convertToNeIpsec(ipsecConnections, deviceIdToCtrollMap);
        assertEquals(2, rst.size());
        assertNull(rst.get(0).getSourceAddress());
    }

    @Test
    public void testConvertToNeIpsecWithEmptyInput() throws ServiceException {
        List<NbiIpSec> ipsecConnections = new ArrayList<NbiIpSec>();
        Map<String, String> deviceIdToCtrollMap = new HashMap<String, String>();
        List<SbiNeIpSec> rst = NbiModelToSbiModel.convertToNeIpsec(ipsecConnections, deviceIdToCtrollMap);
        assertEquals(0, rst.size());

    }

}
