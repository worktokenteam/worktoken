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

package com.worktoken.engine;

import com.worktoken.model.*;
import org.omg.spec.bpmn._20100524.model.TDefinitions;
import org.omg.spec.bpmn._20100524.model.TProcess;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Alex Pavlov (alex@rushproject.com)
 */
public interface WorkSession {

    public boolean isRunning();

    public String getId();

    /**
     * Send token to outgoing connector.
     *
     * Throws exception if connector is not an outgoing connector for the node
     *
     * @param token
     * @param connector
     */
    public void sendToken(WorkToken token, Node fromNode, Connector connector);

    /**
     * Send token from a node having single output connector.
     *
     * Throws exception if node has multiple output connectors (diverging gateway)
     *
     * @param token
     * @param fromNode
     */
    public void sendToken(WorkToken token, Node fromNode);

    /**
     * Sends multiple tokens
     *
     * @param tokens
     */
    public void sendTokens(Map<Connector, WorkToken> tokens, Node fromNode);

    /**
     * Parse BPMN specification and register process definitions.
     *
     *
     * @param stream
     * @return definitions id
     * @throws javax.xml.bind.JAXBException
     */
    public TDefinitions readDefinitions(InputStream stream);

    public TDefinitions getDefinitions(String defId);

    public Set<String> getDefinitionsIds();

    public void dropDefinitions(final String defId);

    public long createProcess(final String defId);

    public void sendEventToken(EventToken eventToken, long processId);

    public void start();

    public void close() throws InterruptedException;

    public TProcess getProcessDefinition(String defId);

    public List<UserTask> getUserTasks();

    public void persist(final Object o);
    public Object merge(final Object o);

}
