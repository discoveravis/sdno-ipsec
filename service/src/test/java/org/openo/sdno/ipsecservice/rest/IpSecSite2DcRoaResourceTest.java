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

package org.openo.sdno.ipsecservice.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.poi.ss.formula.functions.T;
import org.codehaus.jackson.type.TypeReference;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openo.baseservice.remoteservice.exception.ServiceException;
import org.openo.baseservice.roa.util.restclient.RestfulParametes;
import org.openo.baseservice.roa.util.restclient.RestfulResponse;
import org.openo.sdno.framework.container.resthelper.RestfulProxy;
import org.openo.sdno.framework.container.util.JsonUtil;
import org.openo.sdno.ipsecservice.model.enums.DeployStatus;
import org.openo.sdno.ipsecservice.model.enums.NeRoleType;
import org.openo.sdno.overlayvpn.brs.invdao.LogicalTernminationPointInvDao;
import org.openo.sdno.overlayvpn.brs.invdao.NetworkElementInvDao;
import org.openo.sdno.overlayvpn.brs.model.LogicalTernminationPointMO;
import org.openo.sdno.overlayvpn.brs.model.NetworkElementMO;
import org.openo.sdno.overlayvpn.dao.common.InventoryDao;
import org.openo.sdno.overlayvpn.errorcode.ErrorCode;
import org.openo.sdno.overlayvpn.model.v2.ipsec.NbiActionModel;
import org.openo.sdno.overlayvpn.model.v2.ipsec.NbiIpSec;
import org.openo.sdno.overlayvpn.model.v2.ipsec.SbiIkePolicy;
import org.openo.sdno.overlayvpn.model.v2.ipsec.SbiIp;
import org.openo.sdno.overlayvpn.model.v2.ipsec.SbiIpSecPolicy;
import org.openo.sdno.overlayvpn.model.v2.ipsec.SbiNeIpSec;
import org.openo.sdno.overlayvpn.res.ResourcesUtil;
import org.openo.sdno.overlayvpn.result.ResultRsp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import mockit.Mock;
import mockit.MockUp;
import mockit.Mocked;

/**
 * <br/>
 * <p>
 * </p>
 * 
 * @author
 * @version SDNO 0.5 Feb 9, 2017
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath*:/spring/applicationContext.xml",
                "classpath*:META-INF/spring/service.xml", "classpath*:spring/service.xml"})
public class IpSecSite2DcRoaResourceTest {

    @Mocked
    HttpServletRequest request;

    @Mocked
    HttpServletResponse response;

    @Autowired
    IpSecSite2DcRoaResource ipSecSvc;

    /**
     * <br/>
     * 
     * @throws java.lang.Exception
     * @since SDNO 0.5
     */
    @Before
    public void setUp() throws Exception {
        new MockInventoryDao();
        new MockRestfulProxy();

        new MockUp<ResourcesUtil>() {

            @Mock
            List<Long> requestGloabelValue(String poolname, String label, int reqNumber, Long min, Long max)
                    throws ServiceException {
                Long num = new Long("0");
                return Arrays.asList(num);
            }
        };
    }

    @Test
    public void testCreateSuccess() throws ServiceException {

        new MockNeDao();
        new MockLtpDao();
        NbiIpSec ipsec = JsonUtil.fromJson(
                "{\"id\":\"ipsecconnection1id\",\"tenantId\":\"tenantid\",\"name\":\"nbiipsec1\",\"description\":\"test ipsec\",\"operStatus\":\"none\",\"deployStatus\":\"deploy\",\"srcNeId\":\"Ne01\",\"connectionId\":\"connectionId\",\"srcNeRole\":\"vpc\",\"destNeRole\":\"cloudcpe\",\"type\":\"ipsec\",\"destNeId\":\"Ne02\",\"srcPortName\":\"Port01\",\"destPortName\":\"Port02\",\"workType\":\"work\",\"protectionPolicy\":\"nqa\",\"nqa\":null,\"ikePolicy\":\"{\\\"authAlgorithm\\\":\\\"md5\\\",\\\"psk\\\":\\\"0123456789\\\",\\\"ikeVersion\\\":\\\"v2\\\",\\\"encryptionAlgorithm\\\":\\\"3des\\\"}\",\"ipsecPolicy\":\"{\\\"transformProtocol\\\":\\\"esp\\\",\\\"authAlgorithm\\\":\\\"md5\\\",\\\"encryptionAlgorithm\\\":\\\"3des\\\"}\",\"srcIsTemplateType\":\"false\",\"destIsTemplateType\":\"false\",\"ruleSrcPortName\":\"LoopBack1\",\"ruleDestPortName\":\"LoopBack1\",\"sourceLanCidrs\":\"[{\\\"ipv4\\\":\\\"1.1.1.1\\\",\\\"ipMask\\\":\\\"24\\\"}]\",\"destLanCidrs\":\"[{\\\"ipv4\\\":\\\"1.1.1.2\\\",\\\"ipMask\\\":\\\"24\\\"}]\",\"qosPreClassify\":\"false\",\"regionId\":\"regionId01\"}",
                NbiIpSec.class);
        List<NbiIpSec> ipsecs = new ArrayList<NbiIpSec>();
        ipsecs.add(ipsec);
        List<NbiIpSec> resultRsp = ipSecSvc.create(request, response, ipsecs);
        assertEquals(resultRsp.size(), 1);
    }

    @Test
    public void testBatchQuerySuccess() throws ServiceException {

        new MockNeDao();
        new MockLtpDao();

        List<String> ids = new ArrayList<String>();
        ids.add("id1");
        List<NbiIpSec> resultRsp = ipSecSvc.queryIpsecTunnel(request, ids);
        assertNull(resultRsp);
    }

    @Test
    public void testQuerySuccess() throws ServiceException {
        new MockUp<InventoryDao<T>>() {

            @Mock
            ResultRsp<NbiIpSec> query(Class clazz, String uuid, String tenantId) throws ServiceException {

                NbiIpSec ipsec = new NbiIpSec();
                ipsec.setName("test");
                ResultRsp<NbiIpSec> resp = new ResultRsp<NbiIpSec>(ErrorCode.OVERLAYVPN_SUCCESS, ipsec);
                return resp;

            }

        };
        new MockNeDao();
        new MockLtpDao();

        NbiIpSec resultRsp = ipSecSvc.queryIpsecTunnel(request, "id1");
        assertEquals(resultRsp.getName(), "test");
    }

    @Test
    public void testUpdateSuccess() throws ServiceException {
        new MockUp<InventoryDao<T>>() {

            @Mock
            ResultRsp<List<NbiIpSec>> batchQuery(Class clazz, List<String> uuids) throws ServiceException {

                NbiIpSec ipsec = JsonUtil.fromJson(
                        "{\"id\":\"ipsecconnection1id\",\"tenantId\":\"tenantid\",\"name\":\"nbiipsec1\",\"description\":\"test ipsec\",\"operStatus\":\"none\",\"deployStatus\":\"deploy\",\"srcNeId\":\"Ne01\",\"connectionId\":\"connectionId\",\"srcNeRole\":\"vpc\",\"destNeRole\":\"cloudcpe\",\"type\":\"ipsec\",\"destNeId\":\"Ne02\",\"srcPortName\":\"Port01\",\"destPortName\":\"Port02\",\"workType\":\"work\",\"protectionPolicy\":\"nqa\",\"nqa\":null,\"ikePolicy\":\"{\\\"authAlgorithm\\\":\\\"md5\\\",\\\"psk\\\":\\\"0123456789\\\",\\\"ikeVersion\\\":\\\"v2\\\",\\\"encryptionAlgorithm\\\":\\\"3des\\\"}\",\"ipsecPolicy\":\"{\\\"transformProtocol\\\":\\\"esp\\\",\\\"authAlgorithm\\\":\\\"md5\\\",\\\"encryptionAlgorithm\\\":\\\"3des\\\"}\",\"srcIsTemplateType\":\"false\",\"destIsTemplateType\":\"false\",\"ruleSrcPortName\":\"LoopBack1\",\"ruleDestPortName\":\"LoopBack1\",\"sourceLanCidrs\":\"[{\\\"ipv4\\\":\\\"1.1.1.1\\\",\\\"ipMask\\\":\\\"24\\\"}]\",\"destLanCidrs\":\"[{\\\"ipv4\\\":\\\"1.1.1.2\\\",\\\"ipMask\\\":\\\"24\\\"}]\",\"qosPreClassify\":\"false\",\"regionId\":\"regionId01\"}",
                        NbiIpSec.class);
                List<NbiIpSec> ipsecs = new ArrayList<NbiIpSec>();
                ipsecs.add(ipsec);
                ResultRsp<List<NbiIpSec>> resp = new ResultRsp<List<NbiIpSec>>(ErrorCode.OVERLAYVPN_SUCCESS, ipsecs);
                return resp;

            }

            @Mock
            ResultRsp<List<SbiNeIpSec>> batchQuery(Class clazz, String filter) throws ServiceException {

                SbiNeIpSec ipsec = new SbiNeIpSec();
                ipsec.setLocalNeRole(NeRoleType.VPC.getName());
                ipsec.setConnectionServiceId("ipsecconnection1id");
                ipsec.setNeId("Ne01");
                List<SbiNeIpSec> ipsecs = new ArrayList<SbiNeIpSec>();
                ipsecs.add(ipsec);
                ResultRsp<List<SbiNeIpSec>> resp =
                        new ResultRsp<List<SbiNeIpSec>>(ErrorCode.OVERLAYVPN_SUCCESS, ipsecs);
                return resp;

            }

        };

        new MockNeDao();
        new MockLtpDao();
        NbiIpSec ipsec = JsonUtil.fromJson(
                "{\"id\":\"ipsecconnection1id\",\"tenantId\":\"tenantid\",\"name\":\"nbiipsec1\",\"description\":\"test ipsec\",\"operStatus\":\"none\",\"deployStatus\":\"deploy\",\"srcNeId\":\"Ne01\",\"connectionId\":\"connectionId\",\"srcNeRole\":\"vpc\",\"destNeRole\":\"cloudcpe\",\"type\":\"ipsec\",\"destNeId\":\"Ne02\",\"srcPortName\":\"Port01\",\"destPortName\":\"Port02\",\"workType\":\"work\",\"protectionPolicy\":\"nqa\",\"nqa\":null,\"ikePolicy\":\"{\\\"authAlgorithm\\\":\\\"md5\\\",\\\"psk\\\":\\\"0123456789\\\",\\\"ikeVersion\\\":\\\"v2\\\",\\\"encryptionAlgorithm\\\":\\\"3des\\\"}\",\"ipsecPolicy\":\"{\\\"transformProtocol\\\":\\\"esp\\\",\\\"authAlgorithm\\\":\\\"md5\\\",\\\"encryptionAlgorithm\\\":\\\"3des\\\"}\",\"srcIsTemplateType\":\"false\",\"destIsTemplateType\":\"false\",\"ruleSrcPortName\":\"LoopBack1\",\"ruleDestPortName\":\"LoopBack1\",\"sourceLanCidrs\":\"[{\\\"ipv4\\\":\\\"1.1.1.1\\\",\\\"ipMask\\\":\\\"24\\\"}]\",\"destLanCidrs\":\"[{\\\"ipv4\\\":\\\"1.1.1.2\\\",\\\"ipMask\\\":\\\"24\\\"}]\",\"qosPreClassify\":\"false\",\"regionId\":\"regionId01\"}",
                NbiIpSec.class);
        List<NbiIpSec> ipsecs = new ArrayList<NbiIpSec>();
        ipsecs.add(ipsec);
        List<NbiIpSec> resultRsp = ipSecSvc.update(request, ipsecs);
        assertEquals(resultRsp.size(), 1);
    }

    @Test
    public void testDeploySuccess() throws ServiceException {
        new MockUp<InventoryDao<T>>() {

            @Mock
            public ResultRsp batchQuery(Class clazz, String filter) throws ServiceException {
                if(NbiIpSec.class.equals(clazz)) {
                    NbiIpSec ipsec = JsonUtil.fromJson(
                            "{\"id\":\"ipsecconnection1id\",\"tenantId\":\"tenantid\",\"name\":\"nbiipsec1\",\"description\":\"test ipsec\",\"operStatus\":\"none\",\"deployStatus\":\"deploy\",\"srcNeId\":\"Ne01\",\"connectionId\":\"connectionId\",\"srcNeRole\":\"vpc\",\"destNeRole\":\"cloudcpe\",\"type\":\"ipsec\",\"destNeId\":\"Ne02\",\"srcPortName\":\"Port01\",\"destPortName\":\"Port02\",\"workType\":\"work\",\"protectionPolicy\":\"nqa\",\"nqa\":null,\"ikePolicy\":\"{\\\"authAlgorithm\\\":\\\"md5\\\",\\\"psk\\\":\\\"0123456789\\\",\\\"ikeVersion\\\":\\\"v2\\\",\\\"encryptionAlgorithm\\\":\\\"3des\\\"}\",\"ipsecPolicy\":\"{\\\"transformProtocol\\\":\\\"esp\\\",\\\"authAlgorithm\\\":\\\"md5\\\",\\\"encryptionAlgorithm\\\":\\\"3des\\\"}\",\"srcIsTemplateType\":\"false\",\"destIsTemplateType\":\"false\",\"ruleSrcPortName\":\"LoopBack1\",\"ruleDestPortName\":\"LoopBack1\",\"sourceLanCidrs\":\"[{\\\"ipv4\\\":\\\"1.1.1.1\\\",\\\"ipMask\\\":\\\"24\\\"}]\",\"destLanCidrs\":\"[{\\\"ipv4\\\":\\\"1.1.1.2\\\",\\\"ipMask\\\":\\\"24\\\"}]\",\"qosPreClassify\":\"false\",\"regionId\":\"regionId01\"}",
                            NbiIpSec.class);
                    List<NbiIpSec> ipsecs = new ArrayList<NbiIpSec>();
                    ipsecs.add(ipsec);
                    ResultRsp<List<NbiIpSec>> resp =
                            new ResultRsp<List<NbiIpSec>>(ErrorCode.OVERLAYVPN_SUCCESS, ipsecs);
                    return resp;
                }
                if(SbiNeIpSec.class.equals(clazz)) {

                    SbiNeIpSec ipsec = new SbiNeIpSec();
                    ipsec.setUuid("id1");
                    ipsec.setNeId("Ne01");
                    ipsec.setConnectionServiceId("ipsecconnection1id");
                    ipsec.setLocalNeRole("vpc");
                    ipsec.setExternalIpSecId("ExternalIpSecId");
                    ipsec.setExternalId("ExternalId");
                    ipsec.setIsTemplateType("true");
                    SbiNeIpSec ipsec2 = new SbiNeIpSec();
                    ipsec2.setUuid("id2");
                    ipsec2.setNeId("Ne02");
                    ipsec2.setConnectionServiceId("ipsecconnection1id");
                    ipsec2.setLocalNeRole("vpc");
                    ipsec2.setExternalIpSecId("ExternalIpSecId");
                    ipsec2.setExternalId("ExternalId");
                    ipsec2.setIsTemplateType("true");
                    List<SbiNeIpSec> list = new ArrayList<SbiNeIpSec>();
                    list.add(ipsec);
                    list.add(ipsec2);
                    ResultRsp<List<SbiNeIpSec>> resp =
                            new ResultRsp<List<SbiNeIpSec>>(ErrorCode.OVERLAYVPN_SUCCESS, list);

                    return resp;
                }

                return new ResultRsp(ErrorCode.OVERLAYVPN_SUCCESS);
            }

        };
        new MockNeDao();

        NbiActionModel nbi = new NbiActionModel();
        List<String> ids = new ArrayList<String>();
        ids.add("ipsecconnection1id");
        nbi.setDeploy(ids);
        List<String> resultRsp = ipSecSvc.action(request, nbi);
        assertEquals(resultRsp.size(), 1);
    }

    @Test
    public void testUndeploySuccess() throws ServiceException {
        new MockUp<InventoryDao<T>>() {

            @Mock
            public ResultRsp batchQuery(Class clazz, String filter) throws ServiceException {
                if(NbiIpSec.class.equals(clazz)) {
                    NbiIpSec ipsec = JsonUtil.fromJson(
                            "{\"id\":\"ipsecconnection1id\",\"tenantId\":\"tenantid\",\"name\":\"nbiipsec1\",\"description\":\"test ipsec\",\"operStatus\":\"none\",\"deployStatus\":\"deploy\",\"srcNeId\":\"Ne01\",\"connectionId\":\"connectionId\",\"srcNeRole\":\"vpc\",\"destNeRole\":\"cloudcpe\",\"type\":\"ipsec\",\"destNeId\":\"Ne02\",\"srcPortName\":\"Port01\",\"destPortName\":\"Port02\",\"workType\":\"work\",\"protectionPolicy\":\"nqa\",\"nqa\":null,\"ikePolicy\":\"{\\\"authAlgorithm\\\":\\\"md5\\\",\\\"psk\\\":\\\"0123456789\\\",\\\"ikeVersion\\\":\\\"v2\\\",\\\"encryptionAlgorithm\\\":\\\"3des\\\"}\",\"ipsecPolicy\":\"{\\\"transformProtocol\\\":\\\"esp\\\",\\\"authAlgorithm\\\":\\\"md5\\\",\\\"encryptionAlgorithm\\\":\\\"3des\\\"}\",\"srcIsTemplateType\":\"false\",\"destIsTemplateType\":\"false\",\"ruleSrcPortName\":\"LoopBack1\",\"ruleDestPortName\":\"LoopBack1\",\"sourceLanCidrs\":\"[{\\\"ipv4\\\":\\\"1.1.1.1\\\",\\\"ipMask\\\":\\\"24\\\"}]\",\"destLanCidrs\":\"[{\\\"ipv4\\\":\\\"1.1.1.2\\\",\\\"ipMask\\\":\\\"24\\\"}]\",\"qosPreClassify\":\"false\",\"regionId\":\"regionId01\"}",
                            NbiIpSec.class);
                    List<NbiIpSec> ipsecs = new ArrayList<NbiIpSec>();
                    ipsecs.add(ipsec);
                    ResultRsp<List<NbiIpSec>> resp =
                            new ResultRsp<List<NbiIpSec>>(ErrorCode.OVERLAYVPN_SUCCESS, ipsecs);
                    return resp;
                }
                if(SbiNeIpSec.class.equals(clazz)) {

                    SbiNeIpSec ipsec = new SbiNeIpSec();
                    ipsec.setUuid("id1");
                    ipsec.setNeId("Ne01");
                    ipsec.setConnectionServiceId("ipsecconnection1id");
                    ipsec.setLocalNeRole("vpc");
                    ipsec.setExternalIpSecId("ExternalIpSecId");
                    ipsec.setExternalId("ExternalId");
                    ipsec.setIsTemplateType("true");

                    SbiNeIpSec ipsec2 = new SbiNeIpSec();
                    ipsec2.setUuid("id2");
                    ipsec2.setNeId("Ne02");
                    ipsec2.setConnectionServiceId("ipsecconnection1id");
                    ipsec2.setLocalNeRole("cloudcpe");
                    ipsec2.setExternalIpSecId("ExternalIpSecId");
                    ipsec2.setExternalId("ExternalId");
                    ipsec2.setIsTemplateType("true");
                    ipsec2.setDeployStatus(DeployStatus.DEPLOY.getName());
                    List<SbiNeIpSec> list = new ArrayList<SbiNeIpSec>();
                    list.add(ipsec);
                    list.add(ipsec2);
                    ResultRsp<List<SbiNeIpSec>> resp =
                            new ResultRsp<List<SbiNeIpSec>>(ErrorCode.OVERLAYVPN_SUCCESS, list);

                    return resp;
                }

                if(SbiIkePolicy.class.equals(clazz)) {
                    SbiIkePolicy ike = new SbiIkePolicy();
                    List<SbiIkePolicy> ikes = new ArrayList<SbiIkePolicy>();
                    ikes.add(ike);
                    ResultRsp<List<SbiIkePolicy>> resp =
                            new ResultRsp<List<SbiIkePolicy>>(ErrorCode.OVERLAYVPN_SUCCESS, ikes);
                    return resp;
                }
                if(SbiIpSecPolicy.class.equals(clazz)) {
                    SbiIpSecPolicy policy = new SbiIpSecPolicy();
                    List<SbiIpSecPolicy> policys = new ArrayList<SbiIpSecPolicy>();
                    policys.add(policy);
                    ResultRsp<List<SbiIpSecPolicy>> resp =
                            new ResultRsp<List<SbiIpSecPolicy>>(ErrorCode.OVERLAYVPN_SUCCESS, policys);
                    return resp;
                }
                return new ResultRsp(ErrorCode.OVERLAYVPN_SUCCESS);
            }

        };
        new MockNeDao();

        NbiActionModel nbi = new NbiActionModel();
        List<String> ids = new ArrayList<String>();
        ids.add("ipsecconnection1id");
        nbi.setUndeploy(ids);
        List<String> resultRsp = ipSecSvc.action(request, nbi);
        assertEquals(resultRsp.size(), 1);
    }

    @Test
    public void testDeleteSuccess() throws ServiceException {
        new MockUp<InventoryDao<T>>() {

            @Mock
            ResultRsp<List<NbiIpSec>> batchQuery(Class clazz, List<String> uuids) throws ServiceException {

                NbiIpSec ipsec = new NbiIpSec();
                ipsec.setName("test");
                ipsec.setDeployStatus(DeployStatus.UNDEPLOY.getName());
                List<NbiIpSec> list = new ArrayList<NbiIpSec>();
                list.add(ipsec);
                ResultRsp<List<NbiIpSec>> resp = new ResultRsp<List<NbiIpSec>>(ErrorCode.OVERLAYVPN_SUCCESS, list);
                return resp;

            }

        };
        String resultRsp = ipSecSvc.delete(request, "id1");
        assertEquals(resultRsp, "id1");
    }

    private final class MockInventoryDao<T> extends MockUp<InventoryDao<T>> {

        @Mock
        ResultRsp queryByFilter(Class clazz, String filter, String queryResultFields) throws ServiceException {
            if(NbiIpSec.class.equals(clazz)) {
                NbiIpSec ipsec = null;

                ResultRsp<List<NbiIpSec>> resp =
                        new ResultRsp<List<NbiIpSec>>(ErrorCode.OVERLAYVPN_SUCCESS, Arrays.asList(ipsec));
                resp.setData(null);
                return resp;
            }
            if(SbiNeIpSec.class.equals(clazz)) {
                SbiNeIpSec ipsec = new SbiNeIpSec();
                ipsec.setExternalIpSecId("ExternalIpSecId");
                ipsec.setExternalId("ExternalId");
                ipsec.setIsTemplateType("true");
                ResultRsp<List<SbiNeIpSec>> resp =
                        new ResultRsp<List<SbiNeIpSec>>(ErrorCode.OVERLAYVPN_SUCCESS, Arrays.asList(ipsec));
                resp.setData(null);
                return resp;
            }
            return null;
        }

        @Mock
        ResultRsp<String> batchDelete(Class clazz, List<String> uuids) throws ServiceException {
            return new ResultRsp<String>();
        }

        @Mock
        public ResultRsp update(Class clazz, List oriUpdateList, String updateFieldListStr) {
            return new ResultRsp(ErrorCode.OVERLAYVPN_SUCCESS);
        }

        @Mock
        public ResultRsp<T> insert(T data) throws ServiceException {
            return new ResultRsp(ErrorCode.OVERLAYVPN_SUCCESS);
        }

        @Mock
        public ResultRsp<List<T>> batchInsert(List<T> dataList) throws ServiceException {
            return new ResultRsp(ErrorCode.OVERLAYVPN_SUCCESS);
        }

        @Mock
        public ResultRsp batchQuery(Class clazz, String filter) throws ServiceException {
            if(SbiNeIpSec.class.equals(clazz)) {

                List<SbiNeIpSec> ipsecs = new ArrayList<>();
                ResultRsp<List<SbiNeIpSec>> resp = new ResultRsp<List<SbiNeIpSec>>(ErrorCode.OVERLAYVPN_SUCCESS);
                resp.setData(ipsecs);
                return resp;
            }

            return new ResultRsp(ErrorCode.OVERLAYVPN_SUCCESS);
        }
    }

    private final class MockRestfulProxy extends MockUp<RestfulProxy> {

        @Mock
        RestfulResponse get(String uri, RestfulParametes restParametes) throws ServiceException {
            RestfulResponse response = new RestfulResponse();

            if(uri.contains("ports")) {

                SbiIp ip = new SbiIp();
                ip.setIpv4("192.163.1.1");
                ip.setIpMask("16");
                ResultRsp<SbiIp> sbiRsp = new ResultRsp<SbiIp>(ErrorCode.OVERLAYVPN_SUCCESS, ip);

                response.setStatus(HttpStatus.SC_OK);
                response.setResponseJson(JsonUtil.toJson(sbiRsp));
            }

            return response;
        }

        @Mock
        RestfulResponse post(String uri, RestfulParametes restParametes) throws ServiceException {
            RestfulResponse response = new RestfulResponse();

            if(uri.contains("/dc-gateway/ipsec-connections")) {

                SbiNeIpSec ipsec = new SbiNeIpSec();
                List<SbiNeIpSec> success = new ArrayList<SbiNeIpSec>();
                success.addAll(JsonUtil.fromJson(restParametes.getRawData(), new TypeReference<List<SbiNeIpSec>>() {}));
                ResultRsp<SbiNeIpSec> sbiRsp = new ResultRsp<SbiNeIpSec>(ErrorCode.OVERLAYVPN_SUCCESS, ipsec);
                sbiRsp.setSuccessed(success);
                response.setStatus(HttpStatus.SC_OK);
                response.setResponseJson(JsonUtil.toJson(sbiRsp));
            }
            if(uri.contains("/device/batch-create-ipsecs")) {

                SbiNeIpSec ipsec = new SbiNeIpSec();
                List<SbiNeIpSec> success = new ArrayList<SbiNeIpSec>();
                success.addAll(JsonUtil.fromJson(restParametes.getRawData(), new TypeReference<List<SbiNeIpSec>>() {}));
                ResultRsp<SbiNeIpSec> sbiRsp = new ResultRsp<SbiNeIpSec>(ErrorCode.OVERLAYVPN_SUCCESS, ipsec);
                sbiRsp.setSuccessed(success);
                response.setStatus(HttpStatus.SC_OK);
                response.setResponseJson(JsonUtil.toJson(sbiRsp));
            }

            if(uri.contains("/batch-delete-ipsec")) {
                SbiNeIpSec ipsec = new SbiNeIpSec();
                List<SbiNeIpSec> success = new ArrayList<SbiNeIpSec>();
                success.addAll(JsonUtil.fromJson(restParametes.getRawData(), new TypeReference<List<SbiNeIpSec>>() {}));
                ResultRsp<SbiNeIpSec> sbiRsp = new ResultRsp<SbiNeIpSec>(ErrorCode.OVERLAYVPN_SUCCESS, ipsec);
                sbiRsp.setSuccessed(success);
                response.setStatus(HttpStatus.SC_OK);
                response.setResponseJson(JsonUtil.toJson(sbiRsp));
            }
            return response;
        }

        @Mock
        RestfulResponse put(String uri, RestfulParametes restParametes) throws ServiceException {
            RestfulResponse response = new RestfulResponse();
            if(uri.contains("/dc-gateway/ipsec-connections")) {

                SbiNeIpSec ipsec = new SbiNeIpSec();

                ResultRsp<SbiNeIpSec> sbiRsp = new ResultRsp<SbiNeIpSec>(ErrorCode.OVERLAYVPN_SUCCESS, ipsec);

                response.setStatus(HttpStatus.SC_OK);
                response.setResponseJson(JsonUtil.toJson(sbiRsp));
            }
            return response;
        }

    }

    private class MockNeDao extends MockUp<NetworkElementInvDao> {

        @Mock
        public NetworkElementMO query(String neId) throws ServiceException {
            NetworkElementMO ne = new NetworkElementMO();

            ne.setNativeID(neId + "1");
            ne.setId(neId);

            return ne;
        }

        @Mock
        public List<NetworkElementMO> getAllMO() throws ServiceException {
            NetworkElementMO ne1 = new NetworkElementMO();
            NetworkElementMO ne2 = new NetworkElementMO();
            ne1.setId("Ne01");
            ne2.setId("Ne02");
            List<NetworkElementMO> list = new ArrayList<>();
            list.add(ne1);
            list.add(ne2);
            return list;
        }
    }

    private class MockLtpDao extends MockUp<LogicalTernminationPointInvDao> {

        @Mock
        List<LogicalTernminationPointMO> query(Map<String, String> condition) throws ServiceException {
            LogicalTernminationPointMO ltp = new LogicalTernminationPointMO();
            ltp.setId(condition.get("meID"));
            ltp.setName(condition.get("name"));
            List<LogicalTernminationPointMO> ltps = new ArrayList<LogicalTernminationPointMO>();
            ltps.add(ltp);
            return ltps;
        }
    }
}
