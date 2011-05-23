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
