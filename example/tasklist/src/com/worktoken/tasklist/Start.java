package com.worktoken.tasklist;

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
@FlowElement(nodeRef = "start", processId = "taskList")
public class Start extends CatchEventNode {
    @Override
    public void eventIn(EventToken event) {
        WorkToken token = new WorkToken();
        token.getData().put("subject", event.getData().get("subject"));
        tokenOut(token);
    }
}
