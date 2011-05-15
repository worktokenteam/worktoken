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
