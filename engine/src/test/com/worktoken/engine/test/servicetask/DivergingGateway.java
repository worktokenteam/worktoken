/*
 * Copyright (c) 2011. Rush Project Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.worktoken.engine.test.servicetask;

import com.worktoken.annotation.FlowElement;
import com.worktoken.model.Connector;
import com.worktoken.model.ParallelGateway;
import com.worktoken.model.WorkToken;

import javax.persistence.Entity;

/**
 * @author Alex Pavlov (alex@rushproject.com)
 */
@Entity
@FlowElement(nodeRef = "_2_2", processId = "orderProcess")
public class DivergingGateway extends ParallelGateway {
    private String customerId;
    private String itemId;

    @Override
    public void registerToken(WorkToken token, Connector connectorIn) {
        customerId = token.getData().get("customerId").toString();
        itemId = token.getData().get("itemId").toString();
    }

    @Override
    protected WorkToken newToken(Connector connector) {
        WorkToken token = new WorkToken();
        if ("_2_7".equals(connector.getId())) {
            token.getData().put("customerId", customerId);
        } else if ("_2_9".equals(connector.getId())) {
            token.getData().put("itemId", itemId);
        }
        return token;
    }
}
