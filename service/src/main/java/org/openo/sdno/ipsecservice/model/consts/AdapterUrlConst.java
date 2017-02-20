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

package org.openo.sdno.ipsecservice.model.consts;

/**
 * Class of Sbi adapter url.<br/>
 * 
 * @author
 * @version SDNO 0.5 Jan 9, 2017
 */
public class AdapterUrlConst {

    public static final String ADAPTER_BASE_URL = "/openoapi/sbi-ipsec/v1";

    public static final String QUERY_NE_PORT_URL = "/device/{0}/ports";

    public static final String BATCH_CREATE_IPSECS = "/device/batch-create-ipsecs";

    public static final String BATCH_DELETE_DEVICE_IPSECS = "/device/{0}/ipsecs/{1}/batch-delete-ipsec";

    public static final String CREATE_IPSECS_FS = "/dc-gateway/ipsec-connections";

    public static final String UNDEPLOY_IPSECS_FS = "/dc-gateway/ipsec-connections/batch-delete";

    private AdapterUrlConst() {

    }
}
