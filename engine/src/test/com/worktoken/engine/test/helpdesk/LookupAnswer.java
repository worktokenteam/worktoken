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

package com.worktoken.engine.test.helpdesk;

import com.worktoken.annotation.FlowElement;
import com.worktoken.annotation.RefType;
import com.worktoken.model.BusinessRuleTask;
import com.worktoken.model.Connector;
import com.worktoken.model.WorkToken;

import javax.persistence.Entity;
import java.util.logging.Logger;

import org.junit.Assert;

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
        if (question != null && !question.isEmpty()) {
            // how are you?
            if (question.matches("(?i)how\\s+are\\s+you\\?")) {
                return "I am fine, thanks.";
            }
        }
        return null;
    }
}
