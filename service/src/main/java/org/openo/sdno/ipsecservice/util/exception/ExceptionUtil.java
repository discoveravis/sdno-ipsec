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

package org.openo.sdno.ipsecservice.util.exception;

import org.openo.baseservice.remoteservice.exception.ServiceException;
import org.openo.sdno.exception.HttpCode;
import org.openo.sdno.overlayvpn.errorcode.ErrorCode;
import org.openo.sdno.overlayvpn.result.ResultRsp;
import org.openo.sdno.overlayvpn.result.SvcExcptUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Throw exception implementation. <br>
 * 
 * @author
 * @version SDNO 0.5 June 20, 2016
 */
public class ExceptionUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExceptionUtil.class);

    /**
     * Constructor<br>
     * 
     * @since SDNO 0.5
     */
    private ExceptionUtil() {
    }

    /**
     * It is used to throw exception when the connection id is existed. <br>
     * 
     * @param connectionId The connection id
     * @throws ServiceException Throw 400 error
     * @since SDNO 0.5
     */
    public static void throwConnectionIdIsExisted(String connectionId) throws ServiceException {
        LOGGER.error(String.format("Connection id (%s) is existed", connectionId));
        String message = "Connection id (" + connectionId + ") is existed";
        String advice = "Connection id is existed, please modify data and try again";
        SvcExcptUtil.throwBadReqSvcExptionWithInfo(ErrorCode.OVERLAYVPN_PARAMETER_INVALID, message, message, message,
                advice);
    }

    /**
     * It is used to throw exception when the tenant id is missing. <br>
     * 
     * @throws ServiceException Throw 400 error
     * @since SDNO 0.5
     */
    public static void throwTenantIdMissing() throws ServiceException {
        LOGGER.error("Tenant id data missing");
        String message = "Tenant id missing";
        String advice = "Tenant id missing, please modify data and try again";
        SvcExcptUtil.throwBadReqSvcExptionWithInfo(ErrorCode.OVERLAYVPN_TENANT_INVALID, message, message, message,
                advice);
    }

    /**
     * It is used to throw exception when read security file failed. <br>
     * 
     * @throws ServiceException Throw 400 error
     * @since SDNO 0.5
     */
    public static void throwReadSecurityFileFailed() throws ServiceException {
        LOGGER.error("Read security file failed");
        String message = "Read security file failed";
        String advice = "Read security file failed, please modify data and try again";
        SvcExcptUtil.throwBadReqSvcExptionWithInfo(ErrorCode.OVERLAYVPN_READ_SECURITY_FILE_FAILED, message, message,
                message, advice);
    }

    /**
     * It is used to throw exception when the tenant id that pass by caller doesn't match with
     * current
     * user. <br>
     * 
     * @param exptTenantId The tenant id that pass by caller
     * @param realTenantId The tenant id of current user
     * @throws ServiceException Throw 400 error
     * @since SDNO 0.5
     */
    public static void throwTenantIdInvalid(String exptTenantId, String realTenantId) throws ServiceException {
        LOGGER.error("TenantIds do not match, expt = " + exptTenantId + ", real = " + realTenantId);
        String message = "TenantIds do not match";
        String advice = "TenantIds do not match, please modify data and try again";
        SvcExcptUtil.throwBadReqSvcExptionWithInfo(ErrorCode.OVERLAYVPN_TENANT_INVALID, message, message, message,
                advice);
    }

    /**
     * It is used to throw exception when the UUIDs don't match. <br>
     * 
     * @param obj1 The object1
     * @param uuid1 The uuid1
     * @param obj2 The object2
     * @param uuid2 The uuid2
     * @throws ServiceException Throw 400 error
     * @since SDNO 0.5
     */
    public static void throwUuidNotConsistency(String obj1, String uuid1, String obj2, String uuid2)
            throws ServiceException {
        LOGGER.error("Uuids do not match, [obj1 = " + obj1 + ", uuid = " + uuid1 + "], [obj2 = " + obj2 + ", uuid = "
                + uuid2 + "]");

        String message = "Uuids do not match,  [obj1 = " + obj1 + ",  uuid = " + uuid1 + "],  [obj2 = " + obj2
                + ", uuid  = " + uuid2 + "]";
        String advice = "Uuids do not match,  [obj1 = " + obj1 + ",  uuid = " + uuid1 + "],  [obj2 = " + obj2
                + ", uuid  = " + uuid2 + "], please modify data and try again";
        SvcExcptUtil.throwBadReqSvcExptionWithInfo(ErrorCode.OVERLAYVPN_PARAMETER_INVALID, message, message, message,
                advice);
    }

    /**
     * It is used to throw exception when parameter is invalid. <br>
     * 
     * @param description The description
     * @throws ServiceException Throw 400 error
     * @since SDNO 0.5
     */
    public static void throwParameterInvalid(String description) throws ServiceException {
        LOGGER.error(description);

        String message = description;
        String advice = description + ", please modify data and try again";
        SvcExcptUtil.throwBadReqSvcExptionWithInfo(ErrorCode.OVERLAYVPN_PARAMETER_INVALID, message, message, message,
                advice);
    }

    /**
     * It is used to throw exception when the resource is not existed. <br>
     * 
     * @param description The description
     * @throws ServiceException Throw 404 error
     * @since SDNO 0.5
     */
    public static void throwResNotExist(String description) throws ServiceException {
        LOGGER.error(description);
        String message = description;
        String advice = description + ", please modify data and try again";

        SvcExcptUtil.throwBadReqSvcExptionWithInfo(ErrorCode.OVERLAYVPN_RESOURCE_NOT_EXIST, message, message, message,
                advice);
    }

    /**
     * It is used to check the operation result. <br>
     * 
     * @param result The operation result
     * @throws ServiceException Throw 500 error
     * @since SDNO 0.5
     */
    public static void checkRspThrowException(ResultRsp<?> result) throws ServiceException {
        if(result == null) {
            LOGGER.error("operation failed! ErrorCode = " + ErrorCode.OVERLAYVPN_FAILED);
            throw new ServiceException(ErrorCode.OVERLAYVPN_FAILED, HttpCode.ERR_FAILED);
        }

        if(!result.isSuccess()) {
            SvcExcptUtil.throwSvcExptionByResultRsp(result);
        }
    }
}
