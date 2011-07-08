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

package com.worktoken.model;

import com.worktoken.engine.BPMNUtils;
import org.omg.spec.bpmn._20100524.model.*;

import javax.persistence.*;
import javax.xml.namespace.QName;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Alex Pavlov (alex@rushproject.com)
 */
@Entity
@NamedQueries({
        @NamedQuery(name = "ParallelGateway.findByProcess",
                    query = "SELECT n FROM ParallelGateway n WHERE n.process = :process"),
        @NamedQuery(name = "ParallelGateway.findByDefIdAndProcess",
                    query = "SELECT n FROM ParallelGateway n WHERE n.defId = :defId AND n.process = :process")
})
public class ParallelGateway extends Node {

    @OneToMany(cascade = CascadeType.ALL,fetch = FetchType.EAGER, mappedBy = "node")
    Set<PathSocket> sockets;

    private TGatewayDirection direction;

    public TGatewayDirection getDirection() {
        return direction;
    }

    public void setDirection(TGatewayDirection direction) {
        this.direction = direction;
    }

    @Override
    public void tokenIn(WorkToken token, Connector connectorIn) {
        PathSocket socket = findSocket(connectorIn.getId());
        if (socket == null) {
            throw new IllegalStateException("Parallel Gateway id:" + getDefId() + " does not have incoming sequence flow with id:" + connectorIn.getId());
        }
        socket.addToken();
        if (isReady()) {
          consumeTokens();
          fireTransition();
        }
    }

    protected void consumeTokens() {
        for (PathSocket socket : getSockets()) {
            socket.removeToken();
        }
    }

    protected void fireTransition() {
        TProcess tProcess = getProcess().getDefinition();
        TParallelGateway tGateway = (TParallelGateway) BPMNUtils.getFlowNode(getDefId(), tProcess);
        Map<Connector, WorkToken> tokenMap = new HashMap<Connector, WorkToken>();
        for (QName qName : tGateway.getOutgoing()) {
            TSequenceFlow link =  BPMNUtils.find(qName.getLocalPart(), tProcess, TSequenceFlow.class);
            if (link == null) {
                throw new IllegalStateException("Sequence flow with id:" + qName.getLocalPart() + " not found");
            }
            tokenMap.put(new Connector(link), new WorkToken());
        }
        tokensOut(tokenMap);
    }

    protected boolean isReady() {
        for (PathSocket socket : getSockets()) {
            int tokenCount = socket.getTokenCount();
            if (tokenCount == 0) {
                return false;
            }
        }
        return true;
    }

    private PathSocket findSocket(String id) {
        for (PathSocket socket : getSockets()) {
            if (socket.getDefId().equals(id)) {
                return socket;
            }
        }
        return null;
    }

    public Set<PathSocket> getSockets() {
        if (sockets == null) {
            sockets = new HashSet<PathSocket>();
        }
        return sockets;
    }
}
