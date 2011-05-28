package com.worktoken.seam2app.action;

import com.worktoken.engine.WorkSession;
import com.worktoken.model.EventToken;
import com.worktoken.seam2app.Engine;
import com.worktoken.seam2app.model.ReceiveConfirmation;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.framework.EntityHome;

import javax.persistence.EntityManager;

/**
 * @author Alex Pavlov (alex@rushproject.com)
 */
@Name("receiveConfirmationHome")
@Scope(ScopeType.EVENT)
public class ReceiveConfirmationHome extends EntityHome<ReceiveConfirmation> {

    private String message;
    private Boolean accepted;

    @In("entityManager")
    private EntityManager em;

    @In("com.worktoken.seam2app.Engine")
    Engine engine;


    @Factory("receiveConfirmation")
    public ReceiveConfirmation init() {
        return getInstance();
    }

    @Override
    public void setId(Object id) {
        if (id instanceof String) {
            super.setId(Long.valueOf((String)id));
        } else {
            super.setId(id);
        }
    }

    public void complete() {
        long processId = getInstance().getProcess().getInstanceId();
        em.flush();
        em.clear();
        WorkSession session = engine.getSession();
        EventToken message = new EventToken();
        message.getData().put("message", getMessage());
        message.getData().put("isAccepted", getAccepted());
        message.setDefinitionId("ID_30607364_7317_2206_0052_000400200024");
        session.sendEventToken(message, processId);
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Boolean getAccepted() {
        return accepted;
    }

    public void setAccepted(Boolean accepted) {
        this.accepted = accepted;
    }
}
