package com.worktoken.model;

import com.worktoken.engine.SessionRegistry;
import com.worktoken.engine.WorkSession;
import org.omg.spec.bpmn._20100524.model.TProcess;

import javax.persistence.*;

/**
 * @author Alex Pavlov (alex@rushproject.com)
 */
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public class BusinessProcess {
    @Id
    @GeneratedValue(strategy = GenerationType.TABLE)
    private long instanceId;
    private String definitionId;
    private String sessionId;
//    @OneToMany(mappedBy = "process", fetch = FetchType.LAZY)
//    private Set<Node> nodes;

    public long getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(long instanceId) {
        this.instanceId = instanceId;
    }

    public String getDefinitionId() {
        return definitionId;
    }

    public void setDefinitionId(String definitionId) {
        this.definitionId = definitionId;
    }

//    public Set<Node> getNodes() {
//        if (nodes == null) {
//            nodes = new HashSet<Node>();
//        }
//        return nodes;
//    }

    public void sendEventToken(EventToken eventToken) {
        getSession().sendEventToken(eventToken, getInstanceId());
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public TProcess getDefinition() {
        return getSession().getProcessDefinition(definitionId);
    }

    public WorkSession getSession() {
        WorkSession session = SessionRegistry.getSession(sessionId);
        if (session == null) {
            throw new IllegalStateException("Session with id=" + sessionId + " is not instantiated");
        }
        return session;
    }
}
