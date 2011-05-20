package com.worktoken.engine.test.helpdesk;

import com.worktoken.annotation.FlowElement;
import com.worktoken.annotation.RefType;
import com.worktoken.engine.test.helpdesk.HelpDeskProcess;
import com.worktoken.model.Connector;
import com.worktoken.model.UserTask;
import com.worktoken.model.WorkToken;

import javax.persistence.Entity;

/**
 * @author Alex Pavlov (alex@rushproject.com)
 */
@FlowElement(nodeRef = "Prepare answer", refType = RefType.Name, processId = "process-com_worktoken_helpdesk")
@Entity(name = "HD_PrepareAnswer")
public class PrepareAnswer extends UserTask {

    private String answer;
    private static final String viewId = "/helpdesk/prepare-answer.xhtml";

    @Override
    public void tokenIn(WorkToken token, Connector connector) {
    }

    @Override
    public String getSubject() {
        return ((HelpDeskProcess) getProcess()).getSubject();
    }

    @Override
    public String getDescription() {
        return "Prepare answer";
    }

    @Override
    public String getViewId() {
        return viewId;
    }


    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public void complete() {
        WorkToken token = new WorkToken();
        token.getData().put("answer", answer);
        sendResult(token);
    }

    public HelpDeskProcess getHelpdeskProcess() {
        return (HelpDeskProcess) getProcess();
    }
}
