/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel.dashboard.rule.apollo;

import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.FlowRuleEntity;
import com.alibaba.csp.sentinel.dashboard.rule.DynamicRulePublisher;
import com.alibaba.csp.sentinel.datasource.Converter;
import com.alibaba.csp.sentinel.util.AssertUtil;
import com.ctrip.framework.apollo.openapi.client.ApolloOpenApiClient;
import com.ctrip.framework.apollo.openapi.dto.NamespaceReleaseDTO;
import com.ctrip.framework.apollo.openapi.dto.OpenItemDTO;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author hantianwei@gmail.com
 * @since 1.5.0
 */
@Component("authorityRuleApolloPublisher")
public class AuthorityRuleApolloPublisher implements DynamicRulePublisher<List<FlowRuleEntity>> {

    @Autowired
    private ApolloOpenApiClient apolloOpenApiClient;
    @Autowired
    private Converter<List<FlowRuleEntity>, String> converter;

    @Autowired
    private ApolloConfig apolloConfig;

    @Override
    public void publish(String app, List<FlowRuleEntity> rules) throws Exception {
        AssertUtil.notEmpty(app, "app name cannot be empty");
        if (rules == null) {
            return;
        }

        // Increase the configuration
        String appId = apolloConfig.getSentinelApolloProject();
        String flowDataId = ApolloConfigUtil.getAuthorityDataId(app);
        OpenItemDTO openItemDTO = new OpenItemDTO();
        openItemDTO.setKey(flowDataId);
        openItemDTO.setValue(converter.convert(rules));
        openItemDTO.setComment("modify by sentinel-dashboard");
        openItemDTO.setDataChangeCreatedBy("apollo");
        apolloOpenApiClient.createOrUpdateItem(appId, apolloConfig.getEnv(), "default", apolloConfig.getNamespace(), openItemDTO);

        // Release configuration
        NamespaceReleaseDTO namespaceReleaseDTO = new NamespaceReleaseDTO();
        namespaceReleaseDTO.setEmergencyPublish(true);
        namespaceReleaseDTO.setReleaseComment("release by sentinel-dashboard");
        namespaceReleaseDTO.setReleasedBy("apollo");
        namespaceReleaseDTO.setReleaseTitle("release by sentinel-dashboard");
        apolloOpenApiClient.publishNamespace(appId, apolloConfig.getEnv(), "default", apolloConfig.getNamespace(), namespaceReleaseDTO);
    }
}
