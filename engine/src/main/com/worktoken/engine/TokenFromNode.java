package com.worktoken.engine;

import com.worktoken.model.Connector;
import com.worktoken.model.WorkToken;

/**
 * @author Alex Pavlov (alex@rushproject.com)
 */
public class TokenFromNode extends WorkItem {
    private WorkToken token;
    private String nodeId;
    private Connector connector;

    public WorkToken getToken() {
        return token;
    }

    public void setToken(WorkToken token) {
        this.token = token;
    }

    public Connector getConnector() {
        return connector;
    }

    public void setConnector(Connector connector) {
        this.connector = connector;
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }
}
