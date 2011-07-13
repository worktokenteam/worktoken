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

package com.worktoken.engine.test.servicetask;

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
@FlowElement(nodeRef = "Receive order", refType = RefType.Name, processId = "orderProcess")
public class ReceiveOrder extends CatchEventNode {

    @Override
    public void eventIn(EventToken event) {
        WorkToken token = new WorkToken();
        token.getData().put("customerId", event.getData().get("customerId"));
        token.getData().put("itemId", event.getData().get("itemId"));
        tokenOut(token);
    }
}
