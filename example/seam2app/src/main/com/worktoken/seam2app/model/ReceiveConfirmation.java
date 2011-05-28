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

package com.worktoken.seam2app.model;

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

    public HelpDeskProcess getHelpDeskProcess() {
        return (HelpDeskProcess) getProcess();
    }
}
