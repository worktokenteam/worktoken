package com.worktoken.engine.test.helpdesk;

import com.worktoken.annotation.FlowElement;
import com.worktoken.annotation.RefType;
import com.worktoken.model.Connector;
import com.worktoken.model.SendTask;
import com.worktoken.model.WorkToken;

import javax.persistence.Entity;
import javax.persistence.Transient;

/**
 * @author Alex Pavlov (alex@rushproject.com)
 */
@FlowElement(nodeRef = "Send answer", refType = RefType.Name, processId = "process-com_worktoken_helpdesk")
@Entity(name = "HD_SendAnswer")
public class SendAnswer extends SendTask {

    @Transient
    private HelpDeskProcess hdp;

    @Override
    public void tokenIn(WorkToken token, Connector connector) {
        hdp = (HelpDeskProcess) getProcess();
        String answer = (String) token.getData().get("answer");
        hdp.addComment(answer, true);
        sendMail(answer);
        tokenOut();
    }

    void sendMail(final String answer) {
        System.out.println("To: " + hdp.getEmail());
        System.out.println("Subject: " + hdp.getSubject());
        System.out.println(answer);
    }
}
