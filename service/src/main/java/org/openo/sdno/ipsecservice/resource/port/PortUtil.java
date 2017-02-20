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

package org.openo.sdno.ipsecservice.resource.port;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.type.TypeReference;
import org.openo.baseservice.remoteservice.exception.ServiceException;
import org.openo.baseservice.roa.util.restclient.RestfulParametes;
import org.openo.baseservice.roa.util.restclient.RestfulResponse;
import org.openo.sdno.exception.InnerErrorServiceException;
import org.openo.sdno.framework.container.resthelper.RestfulProxy;
import org.openo.sdno.framework.container.util.JsonUtil;
import org.openo.sdno.ipsecservice.model.consts.AdapterUrlConst;
import org.openo.sdno.ipsecservice.util.site2dc.RestParameterUtil;
import org.openo.sdno.overlayvpn.brs.invdao.LogicalTernminationPointInvDao;
import org.openo.sdno.overlayvpn.brs.model.LogicalTernminationPointMO;
import org.openo.sdno.overlayvpn.brs.model.NetworkElementMO;
import org.openo.sdno.overlayvpn.model.v2.ipsec.NbiIpSec;
import org.openo.sdno.overlayvpn.model.v2.ipsec.SbiIp;
import org.openo.sdno.overlayvpn.model.v2.result.ResultRsp;
import org.openo.sdno.rest.ResponseUtils;
import org.openo.sdno.util.ip.IpUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class of port utils.<br/>
 * 
 * @author
 * @version SDNO 0.5 Jan 9, 2017
 */
public class PortUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(PortUtil.class);

    private static final String WAN_DEFAULT_IP = "0.0.0.0";

    private static final String LOOP_BACK_PORT = "LoopBack";

    private PortUtil() {
    }

    /**
     * Query port's Ip in db and sbi adapter.<br/>
     * 
     * @param deviceIdToNeMap Map of device id and ne
     * @param deviceIdPortNameToPortNameMap Map of device id and port name
     * @param nbiIpsecs List of Nbi ipsecs
     * @param deviceIdToCtrollMap Map of device id and controller id
     * @return Map of device id, port name and Ip.
     * @throws ServiceException when query Ip failed
     * @since SDNO 0.5
     */
    public static Map<String, SbiIp> getPortIpMap(Map<String, NetworkElementMO> deviceIdToNeMap,
            Map<String, String> deviceIdPortNameToPortNameMap, List<NbiIpSec> nbiIpsecs,
            Map<String, String> deviceIdToCtrollMap) throws ServiceException {
        Map<String, SbiIp> deviceIdPortNameToIpMap = new ConcurrentHashMap<>();
        LogicalTernminationPointInvDao ltpInvDao = new LogicalTernminationPointInvDao();

        for(Entry<String, String> tempEntry : deviceIdPortNameToPortNameMap.entrySet()) {
            String tempPortName = tempEntry.getValue();
            String tempDeviceId = tempEntry.getKey().replace(tempPortName, "");
            NetworkElementMO tempNeMo = deviceIdToNeMap.get(tempDeviceId);

            if(null == tempNeMo) {
                LOGGER.warn("NeMO not found, deviceId: ", tempDeviceId);
                continue;

            }

            Map<String, String> filter = new HashMap<>();
            filter.put("meID", tempNeMo.getId());
            filter.put("name", tempPortName);
            List<LogicalTernminationPointMO> tempLtpMos = ltpInvDao.query(filter);

            boolean isFindLtp = false;
            for(LogicalTernminationPointMO tempLtpMo : tempLtpMos) {
                if(!tempPortName.equals(tempLtpMo.getName())) {
                    continue;
                }

                String ipAddress = tempLtpMo.getIpAddress();
                if(StringUtils.isEmpty(ipAddress) || WAN_DEFAULT_IP.equals(ipAddress)) {
                    LOGGER.warn("ipAddress of port:{0} is empty, neName:{1}: from brs", tempPortName,
                            tempNeMo.getName());

                    if(tempPortName.contains(LOOP_BACK_PORT)) {
                        LOGGER.error("ipAddress is empty! Port name: ", tempPortName);
                        throw new InnerErrorServiceException("ipAddress is empty! Port name:" + tempPortName);
                    } else {
                        getPortIpAc(deviceIdToCtrollMap, deviceIdPortNameToIpMap, nbiIpsecs, tempNeMo, tempPortName);
                    }
                } else {
                    String ipMaskStr = tempLtpMo.getIpMask();
                    int ipMask = (!StringUtils.isEmpty(ipMaskStr)) ? IpUtils.maskToPrefix(ipMaskStr) : (32);
                    deviceIdPortNameToIpMap.put(tempDeviceId + tempPortName,
                            new SbiIp(ipAddress, String.valueOf(ipMask)));
                }
                isFindLtp = true;
                break;
            }
            if(!isFindLtp) {
                LOGGER.warn("did not find Ltp.  port:{0} , neName:{1}: from brs", tempPortName, tempNeMo.getName());

                if(tempPortName.contains(LOOP_BACK_PORT)) {
                    LOGGER.error("ipAddress is empty! Port name: ", tempPortName);
                    throw new InnerErrorServiceException("ipAddress is empty! Port name:" + tempPortName);
                } else {
                    getPortIpAc(deviceIdToCtrollMap, deviceIdPortNameToIpMap, nbiIpsecs, tempNeMo, tempPortName);
                }
            }
        }

        return deviceIdPortNameToIpMap;
    }

    private static SbiIp getPortIpAc(Map<String, String> deviceIdToCtrollMap,
            Map<String, SbiIp> deviceIdPortNameToIpMap, List<NbiIpSec> nbiIpsecs, NetworkElementMO neMo,
            String portName) throws ServiceException {
        String deviceId = neMo.getNativeID();

        String ctrlId = deviceIdToCtrollMap.get(deviceId);

        RestfulParametes restfulPara = RestParameterUtil.getQueryPortIpParam(portName, ctrlId);
        String serviceUrl =
                AdapterUrlConst.ADAPTER_BASE_URL + AdapterUrlConst.QUERY_NE_PORT_URL.replace("{0}", deviceId);

        try {
            RestfulResponse response = RestfulProxy.get(serviceUrl, restfulPara);
            LOGGER.info("query port ip. response status: " + response.getStatus() + ",body:"
                    + response.getResponseContent());

            String rspContent = ResponseUtils.transferResponse(response);
            ResultRsp<SbiIp> queryPortIpRsp = JsonUtil.fromJson(rspContent, new TypeReference<ResultRsp<SbiIp>>() {});

            if(null == queryPortIpRsp.getData()) {
                LOGGER.error("query port ip failed. portName: " + portName);
                throw new InnerErrorServiceException("query port ip failed!");
            }

            if(WAN_DEFAULT_IP.equals(queryPortIpRsp.getData().getIpv4())) {
                LOGGER.error("port ip not configed. query result is : ", WAN_DEFAULT_IP);
                throw new InnerErrorServiceException("port dose not have ip!");
            }

            deviceIdPortNameToIpMap.put(deviceId + portName, queryPortIpRsp.getData());
            return queryPortIpRsp.getData();
        } catch(ServiceException e) {
            LOGGER.error("query port ip exception. e: ", e);
            throw new InnerErrorServiceException("query ne failed!");
        }

    }
}
