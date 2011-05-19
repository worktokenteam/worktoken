package com.rushproject.helpdesk;

import com.worktoken.annotation.FlowElement;
import com.worktoken.annotation.RefType;
import com.worktoken.model.BusinessRuleTask;
import com.worktoken.model.Connector;
import com.worktoken.model.WorkToken;

import javax.persistence.Entity;

/**
 * @author Alex Pavlov (alex@rushproject.com)
 */
@Entity
@FlowElement(nodeRef = "Lookup answer", refType = RefType.Name, processId = "process-com_worktoken_helpdesk")
public class LookupAnswer extends BusinessRuleTask {
    @Override
    public void tokenIn(WorkToken token, Connector connector) {
        String answer = lookup((String) token.getData().get("question"));
        final String gatewayName = "Canned answer available?";
        if (answer != null) {
            token.getData().put("answer", answer);
            token.getData().put(gatewayName, "Yes");
        } else {
            token.getData().put(gatewayName, "No");
        }
        tokenOut(token);
    }

    private String lookup(String question) {
        // how are you?
        if (question.matches("(?i)\\bhow\\s+are\\s+you\\?")) {
            return "I am fine, thanks.";
        }
        return null;
    }
}
