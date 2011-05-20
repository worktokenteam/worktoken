package com.worktoken.engine.test.helpdesk;

import com.worktoken.annotation.FlowElement;
import com.worktoken.annotation.RefType;
import com.worktoken.engine.test.helpdesk.HelpDeskProcess;
import com.worktoken.model.CatchEventNode;
import com.worktoken.model.EventToken;
import com.worktoken.model.WorkToken;

import javax.persistence.Entity;

/**
 * @author Alex Pavlov (alex@rushproject.com)
 */
@Entity
@FlowElement(nodeRef = "Receive request", refType = RefType.Name, processId = "process-com_worktoken_helpdesk")
public class ReceiveRequest extends CatchEventNode {

    @Override
    public void eventIn(EventToken event) {
        HelpDeskProcess process = (HelpDeskProcess) getProcess();
        process.setEmail((String) event.getData().get("email"));
        process.setSubject((String) event.getData().get("subject"));
        process.setComments("Question: " + event.getData().get("question") + "\n\n");
        WorkToken token = new WorkToken();
        token.getData().put("question", event.getData().get("question"));
        tokenOut(token);
    }
}
