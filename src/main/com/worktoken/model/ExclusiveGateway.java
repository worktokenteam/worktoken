package com.worktoken.model;

import com.worktoken.engine.BPMNUtils;
import org.omg.spec.bpmn._20100524.model.TExclusiveGateway;
import org.omg.spec.bpmn._20100524.model.TProcess;
import org.omg.spec.bpmn._20100524.model.TSequenceFlow;

import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

/**
 * @author Alex Pavlov (alex@rushproject.com)
 */
@Entity
@NamedQueries({
        @NamedQuery(name = "ExclusiveGateway.findByProcess",
                    query = "SELECT n FROM ExclusiveGateway n WHERE n.process = :process"),
        @NamedQuery(name = "ExclusiveGateway.findByDefIdAndProcess",
                    query = "SELECT n FROM ExclusiveGateway n WHERE n.nodeId = :defId AND n.process = :process")
})
public class ExclusiveGateway extends Node {
    @Override
    public void tokenIn(WorkToken token, Connector connectorIn) {
        TProcess tProcess = getProcess().getDefinition();
        TExclusiveGateway gateway = (TExclusiveGateway) BPMNUtils.getFlowNode(getNodeId(), tProcess);
        final String myName = gateway.getName();
        TSequenceFlow tSequenceFlow = null;
        if (myName != null && !myName.isEmpty() && token.getData().containsKey(myName)) {
            final String connectorName = token.getData().get(myName).toString();
            if (connectorName != null && !connectorName.isEmpty()) {
                tSequenceFlow = BPMNUtils.findOutgoingByName(gateway, connectorName, tProcess);
            }
        }
        if (tSequenceFlow == null) {
            tSequenceFlow = BPMNUtils.findDefaultOutgoing(gateway, tProcess);
        }
        if (tSequenceFlow != null) {
            tokenOut(token, new Connector(tSequenceFlow));
        } else {
            // TODO: add a link to error description
            throw new IllegalStateException("Can't route token out of exclusive gateway \"" + gateway.getId() + "\"");
        }
    }
}
