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

package org.openo.sdno.ipsecservice.model.enums;

/**
 * Enumeration class of operation type.<br/>
 * 
 * @author
 * @version SDNO 0.5 Jan 12, 2017
 */
public enum OperationType {
    CREATE("create"), DELETE("delete"), UPDATE("update"), DEPLOY("deploy"), UNDEPLOY("undeploy"), REPAIR("repair");

    public static final String CREATE_CONST = "create";

    public static final String DELETE_CONST = "delete";

    public static final String UPDATE_CONST = "update";

    public static final String DEPLOY_CONST = "deploy";

    public static final String UNDEPLOY_CONST = "undeploy";

    public static final String REPAIR_CONST = "repair";

    private String commonName;

    OperationType(final String commonName) {
        this.commonName = commonName;
    }

    public String getCommonName() {
        return commonName;
    }
}
