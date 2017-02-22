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

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.formula.functions.T;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openo.baseservice.remoteservice.exception.ServiceException;
import org.openo.sdno.overlayvpn.dao.common.InventoryDao;
import org.openo.sdno.overlayvpn.errorcode.ErrorCode;
import org.openo.sdno.overlayvpn.model.v2.ipsec.NbiIpSec;
import org.openo.sdno.overlayvpn.model.v2.ipsec.SbiNeIpSec;
import org.openo.sdno.overlayvpn.result.ResultRsp;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import mockit.Mock;
import mockit.MockUp;

/**
 * <br/>
 * <p>
 * </p>
 * 
 * @author
 * @version SDNO 0.5 Feb 23, 2017
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath*:/spring/applicationContext.xml",
                "classpath*:META-INF/spring/service.xml", "classpath*:spring/service.xml"})
public class CreateUtilTest {

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testFillVpcLanCidrAndPortIpToNbi() throws ServiceException {
        new MockUp<InventoryDao<T>>() {

            @Mock
            ResultRsp<NbiIpSec> update(Class clazz, List<NbiIpSec> oriUpdateList, String updateFieldListStr)
                    throws ServiceException {

                NbiIpSec ipsec = new NbiIpSec();
                ipsec.setName("test");
                ResultRsp<NbiIpSec> resp = new ResultRsp<NbiIpSec>(ErrorCode.OVERLAYVPN_SUCCESS, ipsec);
                return resp;

            }

        };

        List<NbiIpSec> nbiIpsecs = new ArrayList<>();
        org.openo.sdno.overlayvpn.model.v2.result.ResultRsp<SbiNeIpSec> fsCreateResult =
                new org.openo.sdno.overlayvpn.model.v2.result.ResultRsp<>();
        NbiIpSec nbiIpSec = new NbiIpSec();
        nbiIpSec.setUuid("111");
        nbiIpSec.setSrcNeId("ne1");
        nbiIpSec.setDestNeId("ne2");
        nbiIpsecs.add(nbiIpSec);
        SbiNeIpSec sbiNeIpSec = new SbiNeIpSec();
        sbiNeIpSec.setConnectionServiceId("111");
        sbiNeIpSec.setNeId("ne1");
        sbiNeIpSec.setPeerNeId("ne2");
        sbiNeIpSec.setSourceAddress("sourceAddress");
        fsCreateResult.setData(sbiNeIpSec);
        List<SbiNeIpSec> success = new ArrayList<>();
        success.add(sbiNeIpSec);
        fsCreateResult.setSuccessed(success);
        CreateUtil.fillVpcLanCidrAndPortIpToNbi(nbiIpsecs, fsCreateResult);
        assertEquals(nbiIpsecs.get(0).getSrcPortIp(), "sourceAddress");
    }

}
