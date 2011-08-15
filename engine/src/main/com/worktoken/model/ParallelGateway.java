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
 * <p><b>Parallel Gateway</b> implementation.</p>
 *
 *  <p>A Parallel Gateway is used to synchronize (combine) parallel flows and to create parallel flows.</p>
 *
 *  <table cellpadding="5px"><tr><td><img src="doc-files/pargwydiv.png"/></td>
 *  <td><img src="doc-files/pargwycnv.png"/></td></tr>
 *  <tr><td colspan="2"><em>Diverging (left) and converging or synchronizing (right) parallel gateways.</em></td></tr></table>
 *
 *  <p>A Parallel Gateway creates parallel paths without checking any conditions; each outgoing Sequence Flow receives
 *  a token upon execution of this Gateway. For incoming flows, the Parallel Gateway will wait for all incoming flows
 *  before triggering the flow through its outgoing Sequence Flows.</p>
 *
 *  <p>ParallelGateway class finalizes tokenIn method and introduces several new overridable methods for life cycle
 *  management. Every time the gateway receives a token (via <em>tokenIn()</em>), it calls
 *  <em><a href="#registerToken(com.worktoken.model.WorkToken, com.worktoken.model.Connector)">registerToken()</a></em>,
 *  to allow application specific token processing within derived classes. Upon execution, the gateway calls
 *  <em><a href="#newToken(com.worktoken.model.Connector)">newToken()</a></em> method for each outgoing sequence flow to
 *  obtain a token from derived class.</p>
 *
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
    private Set<PathSocket> sockets;

    private TGatewayDirection direction;

    public TGatewayDirection getDirection() {
        return direction;
    }

    public void setDirection(TGatewayDirection direction) {
        this.direction = direction;
    }

    @Override
    public final void tokenIn(WorkToken token, Connector connectorIn) {
        registerToken(token, connectorIn);
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

    /**
     * <p>Handles incoming token.</p>
     *
     * <p>This method is called every time Parallel Gateway receives a new token. Default implementation does not carry
     * any actions. The method may be overwritten by derived classes to provide application specific token handling.
     * The overriding method is not required to call parent method. The overriding method must not do any actions
     * affecting workflow (e.g. calling <em>tokenOut()</em> method), as ParallelGateway manages complete life cycle
     * of the gateway.</p>
     *
     * @param token incoming token
     * @param connectorIn incoming connector, the token arrived through
     */
    protected void registerToken(WorkToken token, Connector connectorIn) {
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
            Connector connector = new Connector(link);
            tokenMap.put(connector, newToken(connector));
        }
        tokensOut(tokenMap);
    }

    /**
     * <p>Creates a token for outgoing connector</p>
     *
     * <p>This method is called for each outgoing connector during Parallel Gateway execution. Default implementation
     * generates new WorkToken object carrying no data. The method may be overwritten by derived classes to provide
     * application specific tokens. The overriding method is not required to call parent method. The overriding method
     * must not do any actions affecting workflow (e.g. calling <em>tokenOut()</em> method), as ParallelGateway manages
     * complete life cycle of the gateway.</p>
     *
     * @param connector outgoing connector to generate a WorkToken object for
     * @return token to be sent via the specified outgoing connector
     */
    protected WorkToken newToken(Connector connector) {
        return new WorkToken();
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
