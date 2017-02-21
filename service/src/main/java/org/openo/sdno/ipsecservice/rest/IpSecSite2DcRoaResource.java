/*
 * Copyright 2017 Huawei Technologies Co., Ltd.
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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.apache.commons.collections.CollectionUtils;
import org.openo.baseservice.remoteservice.exception.ServiceException;
import org.openo.sdno.exception.InnerErrorServiceException;
import org.openo.sdno.exception.ParameterServiceException;
import org.openo.sdno.framework.container.service.IResource;
import org.openo.sdno.framework.container.util.UuidUtils;
import org.openo.sdno.ipsecservice.model.enums.DeployStatus;
import org.openo.sdno.ipsecservice.service.action.DeployIpsecUtil;
import org.openo.sdno.ipsecservice.service.action.UndeployIpsecUtil;
import org.openo.sdno.ipsecservice.service.create.CreateIpsecConnectionUtil;
import org.openo.sdno.ipsecservice.service.delete.DeleteIpsecUtil;
import org.openo.sdno.ipsecservice.service.query.QueryIpSecService;
import org.openo.sdno.ipsecservice.service.update.UpdateService;
import org.openo.sdno.overlayvpn.consts.HttpCode;
import org.openo.sdno.overlayvpn.model.v2.ipsec.NbiActionModel;
import org.openo.sdno.overlayvpn.model.v2.ipsec.NbiIpSec;
import org.openo.sdno.overlayvpn.model.v2.ipsec.SbiNeIpSec;
import org.openo.sdno.overlayvpn.model.v2.result.ResultRsp;
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
@Path("/sdnoipsec/v1/ipsec-connections")
public class IpSecSite2DcRoaResource extends IResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(IpSecSite2DcRoaResource.class);

    @Override
    public String getResUri() {

        return "/sdnoipsec/v1/ipsec-connections";
    }

    /**
     * Rest interface to perform query IpSec operation. <br>
     * 
     * @param req HttpServletRequest Object
     * @param ipsecConnectionId The UUID of ipsec connection
     * @return The object of NbiIpSec
     * @throws ServiceException When query IpSec failed
     * @since SDNO 0.5
     */
    @GET
    @Path("/{ipsecConnectionId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public NbiIpSec queryIpsecTunnel(@PathParam("ipsecConnectionId") String ipsecConnectionId) throws ServiceException {

        long beginTime = System.currentTimeMillis();

        // call the service method to perform query operation
        ResultRsp<NbiIpSec> result = QueryIpSecService.queryIpsecConnection(ipsecConnectionId);
        LOGGER.info("Exit query method. cost time = " + (System.currentTimeMillis() - beginTime));

        return result.getData();
    }

    /**
     * Rest interface to perform create IpSec operation. <br>
     * 
     * @param req HttpServletRequest Object
     * @param resp HttpServletResponse Object
     * @param ipsecs List of ipsec to be created
     * @return The object of ResultRsp
     * @throws ServiceException When create IpSec failed
     * @since SDNO 0.5
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public List<NbiIpSec> create(@Context HttpServletResponse resp, List<NbiIpSec> ipsecs) throws ServiceException {

        long beginTime = System.currentTimeMillis();
        LOGGER.info("Start ipsec create method. Time = " + beginTime);
        // get create data from input
        List<NbiIpSec> nbiIpsecs = CreateIpsecConnectionUtil.getGreTunnelList(ipsecs);

        ResultRsp<NbiIpSec> nbiRsp = CreateIpsecConnectionUtil.doCreate(nbiIpsecs);

        LOGGER.info("Exit create method. cost time = " + (System.currentTimeMillis() - beginTime));
        if(nbiRsp.getSuccessed().size() == nbiIpsecs.size()) {
            resp.setStatus(HttpCode.CREATE_OK);
            return nbiIpsecs;
        }

        throw new InnerErrorServiceException("create failed!");
    }

    /**
     * Rest interface to delete ipsec.<br/>
     * 
     * @param req HttpServletRequest Object
     * @param ipsecId Uuid of ipsec to be deleted
     * @return Uuid of ipsec
     * @throws ServiceException when delete ipsec failed
     * @since SDNO 0.5
     */
    @DELETE
    @Path("/{ipsecConnectionId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String delete(@PathParam("ipsecConnectionId") String ipsecId) throws ServiceException {
        long beginTime = System.currentTimeMillis();
        LOGGER.info("Start ipsec delete method. Time = " + beginTime);
        NbiIpSec nbiIpsec = DeleteIpsecUtil.getNbiData(ipsecId);
        if(null == nbiIpsec) {
            LOGGER.info("Exit delete method. cost time = " + (System.currentTimeMillis() - beginTime));
            return ipsecId;
        }

        if(!DeployStatus.UNDEPLOY.getName().equals(nbiIpsec.getDeployStatus())) {
            LOGGER.error("delete ipsec fail.DeployStatus is : ", nbiIpsec.getDeployStatus());
            throw new InnerErrorServiceException("delete ipsec fail! DeployStatus is not undeploy.");
        }

        List<SbiNeIpSec> sbiNeIpSecs = DeleteIpsecUtil.getSbiData(nbiIpsec);
        DeleteIpsecUtil.delData(nbiIpsec, sbiNeIpSecs);
        LOGGER.info("Exit delete method. cost time = " + (System.currentTimeMillis() - beginTime));
        return ipsecId;
    }

    /**
     * Rest interface to batch query ipsec.<br/>
     * 
     * @param req HttpServletRequest Object
     * @param ids List of uuids to be queried
     * @return List of NbiIpSec objects
     * @throws ServiceException when batch query failed
     * @since SDNO 0.5
     */
    @POST
    @Path("/batch-query")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public List<NbiIpSec> queryIpsecTunnel(List<String> ids) throws ServiceException {
        long beginTime = System.currentTimeMillis();
        if(CollectionUtils.isEmpty(ids)) {
            return new ArrayList<>();
        }

        Iterator<String> iter = ids.iterator();
        while(iter.hasNext()) {
            String ipsecId = iter.next();

            try {
                UuidUtils.checkUuid(ipsecId);
            } catch(ServiceException e) {
                LOGGER.warn("Invalid ipsec uuid:" + ipsecId, e);
                iter.remove();
            }
        }

        if(CollectionUtils.isEmpty(ids)) {
            return new ArrayList<>();
        }

        ResultRsp<List<NbiIpSec>> result = QueryIpSecService.queryIpsecConnection(ids);
        LOGGER.info("Exit query method. cost time = " + (System.currentTimeMillis() - beginTime));

        return result.getData();
    }

    /**
     * Rest interface to update ipsec.<br/>
     * 
     * @param req HttpServletRequest Object
     * @param nbiIpsecs List of nbi ipsec objects to be updated
     * @return List of NbiIpSec objects
     * @throws ServiceException when update failed
     * @since SDNO 0.5
     */
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public List<NbiIpSec> update(List<NbiIpSec> nbiIpsecs) throws ServiceException {
        LOGGER.info("ipsec start update! ");
        if(CollectionUtils.isEmpty(nbiIpsecs)) {
            LOGGER.info("ipsec update complete! Input is empty.");
            return new ArrayList<>();
        }

        UpdateService.doUpdate(nbiIpsecs);
        LOGGER.info("ipsec update complete! ");
        return nbiIpsecs;
    }

    /**
     * Rest interface to deploy or undeploy ipsec.<br/>
     * 
     * @param req HttpServletRequest Object
     * @param actionModel Object contains uuids of ipsec to be deploy or undeploy
     * @return List of uuids
     * @throws ServiceException when deploy or undeploy failed
     * @since SDNO 0.5
     */
    @POST
    @Path("/action")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public List<String> action(NbiActionModel actionModel) throws ServiceException {
        if(CollectionUtils.isNotEmpty(actionModel.getDeploy())) {
            LOGGER.info("ipsec start deploy! ");
            return DeployIpsecUtil.doDeploy(actionModel.getDeploy());
        } else if(CollectionUtils.isNotEmpty(actionModel.getUndeploy())) {
            LOGGER.info("ipsec start undeploy! ");
            return UndeployIpsecUtil.doUndeploy(actionModel.getUndeploy());
        } else {
            LOGGER.error("Deploy and undeploy list is empty! ");
            throw new ParameterServiceException("Deploy and undeploy list is empty!");
        }
    }

}
