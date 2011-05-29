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
import com.worktoken.engine.SessionRegistry;
import com.worktoken.engine.WorkSession;
import org.omg.spec.bpmn._20100524.model.TDocumentation;
import org.omg.spec.bpmn._20100524.model.TFlowNode;

import javax.persistence.*;
import java.util.List;

/**
 * @author Alex Pavlov (alex@rushproject.com)
 */
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@NamedQueries({
        @NamedQuery(name = "Node.findByProcess",
                    query = "SELECT n FROM Node n WHERE n.process = :process"),
        @NamedQuery(name = "Node.findByDefIdAndProcess",
                    query = "SELECT n FROM Node n WHERE n.defId = :defId AND n.process = :process"),
        @NamedQuery(name = "Node.countByProcess",
                    query =  "SELECT COUNT(n) FROM Node n WHERE n.process = :process"),
        @NamedQuery(name = "Node.className",
                    query = "SELECT className FROM Node n WHERE n.id = :id")
})
public abstract class Node {
    @Id @GeneratedValue(strategy = GenerationType.TABLE)
    private long id;
    @Version
    private long version;
    private String defId;
    private String className;
    @ManyToOne(fetch = FetchType.EAGER)
    private BusinessProcess process;
    @Transient
    private WorkSession session;

    public long getId() {
        return id;
    }

    public String getDefId() {
        return defId;
    }

    public void setDefId(String nodeId) {
        this.defId = nodeId;
    }

    public BusinessProcess getProcess() {
        return process;
    }

    public void setProcess(BusinessProcess process) {
        this.process = process;
    }

    public WorkSession getSession() {
        if (session == null) {
            session = SessionRegistry.getSession(process.getSessionId());
        }
        return session;
    }

    public void setSession(WorkSession session) {
        this.session = session;
    }

    public abstract void tokenIn(WorkToken token, Connector connector);

    protected void tokenOut(WorkToken token) {
        getSession().sendToken(token, this);
    }

    protected void tokenOut() {
        getSession().sendToken(new WorkToken(), this);
    }

    protected void tokenOut(WorkToken token, Connector connector) {
        getSession().sendToken(token, this, connector);
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public TFlowNode getDefinition() {
        return BPMNUtils.getFlowNode(defId, getProcess().getDefinition());
    }

    public List<TDocumentation> getDocumentation() {
        TFlowNode definition = getDefinition();
        if (definition == null) {
            throw new IllegalStateException("Failed to find definition for node \"" + defId + "\"");
        }
        return definition.getDocumentation();
    }
}
