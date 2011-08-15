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

package com.worktoken.model;

import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import java.util.concurrent.Callable;

/**
 * <p><b>Service Task</b> implementation</p>
 *
 * <p>A Service Task is a Task that uses some sort of service, which could be a Web service or an automated application.
 * It is anticipated that execution of Service Task may take considerable amount of time, this is why Service Task runs
 * in a separate thread. The <em>tokenIn()</em> method, if overridden in subclass may not do any actions affecting
 * workflow (e.g. calling <em>tokenOut()</em> method), as actual execution of the Service Task is triggered by call to
 * <em>call()</em> method.</p>
 *
 * @author Alex Pavlov (alex@rushproject.com)
 */
@Entity
@NamedQueries({
        @NamedQuery(name = "ServiceTask.findAll",
                    query = "SELECT n FROM ServiceTask n"),
        @NamedQuery(name = "ServiceTask.findByProcess",
                    query = "SELECT n FROM ServiceTask n WHERE n.process = :process"),
        @NamedQuery(name = "ServiceTask.findByDefIdAndProcess",
                    query = "SELECT n FROM ServiceTask n WHERE n.defId = :defId AND n.process = :process")
})
public class ServiceTask extends Node implements Callable<String> {

    /**
     * <p>Handles incoming token</p>
     *
     * <p>The <em>tokenIn()</em> method, if overridden in subclass may not do any actions affecting workflow (e.g.
     * calling <em>tokenOut()</em> method), as actual execution of the Service Task is triggered by call to
     * <em>call()</em> method. The <em>tokenIn()</em> method is intended for preparing an object of ServiceTask type for
     * execution (initializing variables, etc). The default implementation of the method carries no action and does not
     * need to be called from overriding methods.</p>
     *
     * @param token incoming token
     * @param connector incoming connector the token arrived through
     */
    @Override
    public void tokenIn(WorkToken token, Connector connector) {
    }

    /**
     * <p>Executes the Service Task</p>
     *
     * <p>The method implements Callable interface. Must call <em>tokenOut()</em> upon completion. Default
     * implementation immediately calls the <em>tokenOut()</em> and does not need to be called from overriding
     * method</p>
     *
     * @return string, which value is currently ignored by engine
     * @throws Exception
     */
    @Override
    public String call() throws Exception {
        tokenOut();
        return null;
    }
}
