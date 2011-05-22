package com.worktoken.engine.test.helpdesk;

import com.worktoken.annotation.FlowElement;
import com.worktoken.annotation.RefType;
import com.worktoken.model.CatchEventNode;
import com.worktoken.model.EventToken;
import com.worktoken.model.WorkToken;

import javax.persistence.Entity;

/**
 * @author Alex Pavlov (alex@rushproject.com)
 */
@Entity
@FlowElement(nodeRef = "com_worktoken_helpdesk_1_55", refType = RefType.Id, processId = "process-com_worktoken_helpdesk")
public class ReceiveConfirmation extends CatchEventNode {

    @Override
    public void eventIn(EventToken event) {
        WorkToken token = new WorkToken();
        HelpDeskProcess process = (HelpDeskProcess) getProcess();
        process.addComment((String) event.getData().get("message"), false);
        final String gatewayName = "Answer accepted?";
        if ((Boolean) event.getData().get("isAccepted")) {
            token.getData().put(gatewayName, "Yes");
        } else {
            token.getData().put(gatewayName, "No");
        }
        tokenOut(token);
    }
}
