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

package org.openo.sdno.ipsecservice.service.create;

import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.openo.baseservice.remoteservice.exception.ServiceException;
import org.openo.sdno.exception.ParameterServiceException;
import org.openo.sdno.overlayvpn.model.v2.ipsec.NbiIpSec;
import org.openo.sdno.overlayvpn.model.v2.ipsec.SbiIp;

/**
 * <br/>
 * <p>
 * </p>
 * 
 * @author
 * @version SDNO 0.5 Feb 6, 2017
 */
public class FillDataUtilTest {

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
    public void testFillPolicyWithEmptyPolicy() throws ServiceException {
        List<NbiIpSec> ipsecs = new ArrayList<NbiIpSec>();
        NbiIpSec ipsec = new NbiIpSec();

        ipsecs.add(ipsec);
        FillDataUtil.fillPolicy(ipsecs);
        assertNull(ipsecs.get(0).getIkePolicyData());
        assertNull(ipsecs.get(0).getIpSecPolicyData());
    }

    @Test(expected = ParameterServiceException.class)
    public void testFillPolicyWithErrPolicy() throws ServiceException {
        List<NbiIpSec> ipsecs = new ArrayList<NbiIpSec>();
        NbiIpSec ipsec = new NbiIpSec();
        ipsec.setIkePolicy("errorIke");
        ipsecs.add(ipsec);
        FillDataUtil.fillPolicy(ipsecs);
        assertNull(ipsecs.get(0).getIkePolicyData());
        assertNull(ipsecs.get(0).getIpSecPolicyData());
    }

    @Test
    public void testFillDeviceIdAndIpWithNullDeviceId() {
        Map<String, SbiIp> map = new HashMap<String, SbiIp>();
        NbiIpSec ipsec = new NbiIpSec();
        FillDataUtil.fillDeviceIdAndIp(map, ipsec, "", "");
        assertNull(ipsec.getSrcIp());
        assertNull(ipsec.getDestIp());
    }

}
