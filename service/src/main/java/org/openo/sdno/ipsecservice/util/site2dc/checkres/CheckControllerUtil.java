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

package org.openo.sdno.ipsecservice.util.site2dc.checkres;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.openo.sdno.overlayvpn.brs.model.NetworkElementMO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

/**
 * Class of check controller util.<br/>
 * 
 * @author
 * @version SDNO 0.5 Jan 9, 2017
 */
public class CheckControllerUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(CheckControllerUtil.class);

    private CheckControllerUtil() {
    }

    /**
     * Build a map of ne id and controller id.<br/>
     * 
     * @param neMoList List of nes
     * @return map of ne id and controller id
     * @since SDNO 0.5
     */
    public static Map<String, String> testCtrlConnection(List<NetworkElementMO> neMoList) {
        Map<String, List<NetworkElementMO>> ctrlIdToNeMosMap = buildCtrlIdToNeMosMap(neMoList);

        List<String> ctrlIds = new ArrayList<String>(ctrlIdToNeMosMap.keySet());

        return buildNeIdToControllerMoMap(ctrlIdToNeMosMap, ctrlIds);
    }

    private static Map<String, List<NetworkElementMO>> buildCtrlIdToNeMosMap(List<NetworkElementMO> neMoList) {
        Map<String, List<NetworkElementMO>> ctrlIdToNeMosMap = new HashMap<String, List<NetworkElementMO>>();
        for(NetworkElementMO tempNeMo : neMoList) {
            if(CollectionUtils.isEmpty(tempNeMo.getControllerID())) {
                LOGGER.warn("ControllerID is empty. ne id: " + tempNeMo.getId());
                continue;
            }

            for(String ctrlId : tempNeMo.getControllerID()) {
                if(null == ctrlIdToNeMosMap.get(ctrlId)) {
                    ctrlIdToNeMosMap.put(ctrlId, new ArrayList<NetworkElementMO>());
                }
                ctrlIdToNeMosMap.get(ctrlId).add(tempNeMo);
            }
        }
        return ctrlIdToNeMosMap;
    }

    private static Map<String, String> buildNeIdToControllerMoMap(Map<String, List<NetworkElementMO>> ctrlIdToNeMosMap,
            List<String> ctrlIds) {
        Map<String, String> neIdToCtrlMoMap = new HashMap<String, String>();
        for(Entry<String, List<NetworkElementMO>> tempEntry : ctrlIdToNeMosMap.entrySet()) {

            List<NetworkElementMO> tempNeMOs = tempEntry.getValue();
            for(NetworkElementMO tempNeMO : tempNeMOs) {

                neIdToCtrlMoMap.put(tempNeMO.getId(), tempEntry.getKey());
            }
        }
        return neIdToCtrlMoMap;
    }

}
