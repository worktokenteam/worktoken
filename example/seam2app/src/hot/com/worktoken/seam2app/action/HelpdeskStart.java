package com.worktoken.seam2app.action;

import com.worktoken.engine.WorkSession;
import com.worktoken.model.BusinessProcess;
import com.worktoken.model.EventToken;
import com.worktoken.seam2app.Engine;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

/**
 * @author Alex Pavlov (alex@rushproject.com)
 */
@Name("HelpdeskStart")
@Scope(ScopeType.EVENT)
public class HelpdeskStart {
    @In("com.worktoken.seam2app.Engine")
    Engine engine;
    private String subject;
    private String question;
    private String email;
    private long processId;

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void start() {
        WorkSession session = engine.getSession();
        processId = session.createProcess("process-com_worktoken_helpdesk");
        EventToken message = new EventToken();
        message.getData().put("email", getEmail());
        message.getData().put("subject", getSubject());
        message.getData().put("question", getQuestion());
        message.setDefinitionId("ID_21465726_5737_2200_2400_000000600032");
        session.sendEventToken(message, processId);
    }

    public long getProcessId() {
        return processId;
    }
}
