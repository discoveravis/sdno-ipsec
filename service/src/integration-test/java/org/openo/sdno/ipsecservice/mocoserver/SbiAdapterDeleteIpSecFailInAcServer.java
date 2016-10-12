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

package org.openo.sdno.ipsecservice.mocoserver;

import org.openo.sdno.testframework.moco.MocoHttpServer;

/**
 * SbiAdapterDeleteIpSecFailInAcServer class for fail test cases. <br>
 * 
 * @author
 * @version SDNO 0.5 June 16, 2016
 */
public class SbiAdapterDeleteIpSecFailInAcServer extends MocoHttpServer {

    private static final String DELETE_IPSEC_FAIL_IN_AC_FILE =
            "src/integration-test/resources/acsbiadapter/deleteipsecfail.json";

    public SbiAdapterDeleteIpSecFailInAcServer() {

    }

    @Override
    public void addRequestResponsePairs() {
        this.addRequestResponsePair(DELETE_IPSEC_FAIL_IN_AC_FILE);
    }

}
