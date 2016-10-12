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

package org.openo.sdno.ipsecservice.model;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.openo.sdno.framework.container.util.JsonUtil;
import org.openo.sdno.overlayvpn.model.common.enums.ActionStatus;

/**
 * IpSecReqModelInfo test class. <br>
 * 
 * @author
 * @version SDNO 0.5 June 16, 2016
 */
public class IpSecReqModelInfoTest {

    IpSecReqModelInfo ipSecReqModelInfo;

    @Before
    public void setUp() throws Exception {
        ipSecReqModelInfo = new IpSecReqModelInfo();
    }

    @Test
    public void testGetConnectionId() {
        ipSecReqModelInfo.setConnectionId("123456");
        assertEquals("123456", ipSecReqModelInfo.getConnectionId());
    }

    @Test
    public void testGetActionState() {
        ipSecReqModelInfo.setActionState(ActionStatus.CREATING.getName());
        assertEquals(ActionStatus.CREATING.getName(), ipSecReqModelInfo.getActionState());
    }

    @Test
    public void testGetData() {
        String data = JsonUtil.toJson("{\"param1\" : \"data1\", \"param2\" : \"data2\"}");
        ipSecReqModelInfo.setData(data);
        assertEquals(data, ipSecReqModelInfo.getData());
    }

}
