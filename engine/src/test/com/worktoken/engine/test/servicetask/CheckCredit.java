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
import com.worktoken.model.Connector;
import com.worktoken.model.ServiceTask;
import com.worktoken.model.WorkToken;

import javax.persistence.Entity;
import java.util.Random;

/**
 * @author Alex Pavlov (alex@rushproject.com)
 */
@Entity
@FlowElement(nodeRef = "_2_6", processId = "orderProcess")
public class CheckCredit extends ServiceTask {

    private String customerId;

    @Override
    public void tokenIn(WorkToken token, Connector connector) {
        customerId = token.getData().get("customerId").toString();
    }

    @Override
    public String call() throws Exception {
        String result;
        Thread.sleep(3000); // calling remote service, it takes time...
        if ("11111".equals(customerId)) {
            result = "Yes";
        } else {
            result = "No";
        }
        WorkToken token = new WorkToken();
        token.getData().put("Credit OK?", result);
        tokenOut(token);
        return null;
    }
}
