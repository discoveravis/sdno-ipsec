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
 * Enumeration class of operation status.<br/>
 * 
 * @author
 * @version SDNO 0.5 Jan 10, 2017
 */
public enum OperationStatus {
    NONE(0), NORMAL(1), CREATING(2), CREATE_EXCEPTION(3), DELETING(4), DELET_EXCEPTION(5), UPDATING(6),
    UPDAT_EXCEPTION(7), DEPLOYING(8), DEPLOY_EXCEPTION(9), CHECKING(10), CHECK_EXCEPTION(11), UNDEPLOYING(12),
    UNDEPLOY_EXCEPTION(13);

    private int value;

    public static final String NORMAL_CONST = "normal";

    public static final String CREATING_CONST = "creating";

    public static final String CHECK_EXCEPTION_CONST = "check_exception";

    public static final String CREAT_EXCEPTION_CONST = "create_exception";

    public static final String DEPLOY_EXCEPTION_CONST = "deploy_exception";

    public static final String DEPLOYING_CONST = "deploying";

    OperationStatus(int value) {
        this.setValue(value);
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    /**
     * Get name of enumeration.<br/>
     * 
     * @return enumeration name
     * @since SDNO 0.5
     */
    public String getName() {
        switch(value) {
            case 0: {
                return "none";
            }
            case 1: {
                return "normal";
            }
            case 2: {
                return "creating";
            }
            case 3: {
                return "create_exception";
            }
            case 4: {
                return "deleting";
            }
            case 5: {
                return "delete_exception";
            }
            case 6: {
                return "updating";
            }
            case 7: {
                return "update_exception";
            }
            case 8: {
                return "deploying";
            }
            case 9: {
                return "deploy_exception";
            }
            case 10: {
                return "checking";
            }
            case 11: {
                return "check_exception";
            }
            case 12: {
                return "undeploying";
            }
            case 13: {
                return "undeploy_exception";
            }
            default: {
                return "";
            }
        }

    }
}
