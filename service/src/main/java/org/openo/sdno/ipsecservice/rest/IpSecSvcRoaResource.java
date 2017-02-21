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

package org.openo.sdno.ipsecservice.rest;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.openo.baseservice.remoteservice.exception.ServiceException;
import org.openo.sdno.ipsecservice.service.inf.IpSecService;
import org.openo.sdno.ipsecservice.util.check.CheckOverlayVpn;
import org.openo.sdno.ipsecservice.util.db.IpSecReqDbOper;
import org.openo.sdno.ipsecservice.util.exception.ExceptionUtil;
import org.openo.sdno.ipsecservice.util.operation.CommonUtil;
import org.openo.sdno.overlayvpn.brs.model.NetworkElementMO;
import org.openo.sdno.overlayvpn.consts.HttpCode;
import org.openo.sdno.overlayvpn.model.common.enums.ActionStatus;
import org.openo.sdno.overlayvpn.model.servicemodel.OverlayVpn;
import org.openo.sdno.overlayvpn.result.ResultRsp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * The rest interface of IpSec. <br>
 * 
 * @author
 * @version SDNO 0.5 June 16, 2016
 */
@Service
@Path("/sdnoipsec/v1/ipsecs")
public class IpSecSvcRoaResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(IpSecSvcRoaResource.class);

    @Resource
    private IpSecService ipSecService;

    public IpSecService getIpSecService() {
        return ipSecService;
    }

    public void setIpSecService(IpSecService ipSecService) {
        this.ipSecService = ipSecService;
    }

    /**
     * Rest interface to perform create IpSec operation. <br>
     * 
     * @param req HttpServletRequest Object
     * @param resp HttpServletResponse Object
     * @param overlayVpn The object of OverlayVpn that contain IpSec
     * @return The object of ResultRsp
     * @throws ServiceException When create IpSec failed
     * @since SDNO 0.5
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ResultRsp<OverlayVpn> create(@Context HttpServletRequest req, @Context HttpServletResponse resp,
            OverlayVpn overlayVpn) throws ServiceException {

        long infterEnterTime = System.currentTimeMillis();

        // check the connection id is existed or not, and forbid to create if the data is existed
        String connectionId = CommonUtil.getIpSecConnection(overlayVpn).getUuid();
        if(IpSecReqDbOper.checkRecordIsExisted(connectionId)) {
            ExceptionUtil.throwConnectionIdIsExisted(connectionId);
        }

        // check parameters and get mapping from NEID and NE Information
        Map<String, NetworkElementMO> neIdToNeMap = new ConcurrentHashMap<>();
        CheckOverlayVpn.check(overlayVpn, neIdToNeMap);

        // save the request data
        IpSecReqDbOper.insert(overlayVpn);

        // update actionState to exception firstly
        IpSecReqDbOper.update(connectionId, ActionStatus.CREATE_EXCEPTION.getName());

        // call the service method to perform create operation
        ResultRsp<OverlayVpn> resultRsp = ipSecService.create(req, resp, overlayVpn, neIdToNeMap);

        // check the response for error code and throw an exception in case of failure
        ExceptionUtil.checkRspThrowException(resultRsp);

        // update actionState to normal
        IpSecReqDbOper.update(connectionId, ActionStatus.NORMAL.getName());

        // well all-is-well, set the response status as success and return result
        resp.setStatus(HttpCode.CREATE_OK);

        LOGGER.info("Exit create method. cost time = " + (System.currentTimeMillis() - infterEnterTime));

        return resultRsp;
    }

    /**
     * Rest interface to perform query IpSec operation. <br>
     * 
     * @param req HttpServletRequest Object
     * @param resp HttpServletResponse Object
     * @param connectionId The UUID of connection
     * @return The object of ResultRsp
     * @throws ServiceException When query IpSec failed
     * @since SDNO 0.5
     */
    @GET
    @Path("/{connectionid}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ResultRsp<OverlayVpn> query(@Context HttpServletRequest req, @Context HttpServletResponse resp,
            @PathParam("connectionid") String connectionId) throws ServiceException {

        long infterEnterTime = System.currentTimeMillis();

        // call the service method to perform query operation
        ResultRsp<OverlayVpn> resultRsp = ipSecService.query(req, resp, connectionId);
        LOGGER.info("Exit query method. cost time = " + (System.currentTimeMillis() - infterEnterTime));

        return resultRsp;
    }

    /**
     * Rest interface to perform delete IpSec operation. <br>
     * 
     * @param req HttpServletRequest Object
     * @param resp HttpServletResponse Object
     * @param connectionId The UUID of connection
     * @return The object of ResultRsp
     * @throws ServiceException When delete IpSec failed
     * @since SDNO 0.5
     */
    @DELETE
    @Path("/{connectionid}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ResultRsp<String> delete(@Context HttpServletRequest req, @Context HttpServletResponse resp,
            @PathParam("connectionid") String connectionId) throws ServiceException {

        long infterEnterTime = System.currentTimeMillis();

        // update actionState to exception firstly
        IpSecReqDbOper.update(connectionId, ActionStatus.DELETE_EXCEPTION.getName());

        // call the service method to perform delete operation
        ResultRsp<String> resultRsp = ipSecService.delete(req, resp, connectionId);

        // check the response for error code and throw an exception in case of failure
        ExceptionUtil.checkRspThrowException(resultRsp);

        // delete data
        IpSecReqDbOper.delete(connectionId);

        LOGGER.info("Exit delete method. cost time = " + (System.currentTimeMillis() - infterEnterTime));

        return resultRsp;
    }
}
