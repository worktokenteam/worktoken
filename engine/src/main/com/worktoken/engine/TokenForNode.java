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

package com.worktoken.engine;

import com.worktoken.model.Connector;
import com.worktoken.model.WorkToken;
import org.omg.spec.bpmn._20100524.model.TFlowNode;

/**
 * @author Alex Pavlov (alex@rushproject.com)
 */
public class TokenForNode extends WorkItem {
    private WorkToken token;
    private TFlowNode nodeDef;
    private Connector connector;

    public WorkToken getToken() {
        return token;
    }

    public void setToken(WorkToken token) {
        this.token = token;
    }

    public TFlowNode getNodeDef() {
        return nodeDef;
    }

    public void setNodeDef(TFlowNode nodeDef) {
        this.nodeDef = nodeDef;
    }

    public Connector getConnector() {
        return connector;
    }

    public void setConnector(Connector connector) {
        this.connector = connector;
    }
}
