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

import com.worktoken.model.Connector;
import com.worktoken.model.EventToken;
import com.worktoken.model.Node;
import com.worktoken.model.WorkToken;
import org.omg.spec.bpmn._20100524.model.TDefinitions;
import org.omg.spec.bpmn._20100524.model.TProcess;

import javax.xml.bind.JAXBException;
import java.io.InputStream;
import java.util.Map;
import java.util.Set;

/**
 * @author Alex Pavlov (alex@rushproject.com)
 */
public interface WorkSession {

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
    public void sendTokens(Map<Connector, WorkToken> tokens);


    /**
     * Parse BPMN specification and register processes.
     *
     *
     * @param stream
     * @return definitions id
     * @throws javax.xml.bind.JAXBException
     */
    public TDefinitions readDefinitions(InputStream stream) throws JAXBException;

    public TDefinitions getDefinitions(String id);

    public Set<String> getDefinitionsIds();

    public void dropDefinitions(final String id);

    public long createProcess(final String id);

    public void sendEventToken(EventToken eventToken, long processId);

    public void close() throws InterruptedException;

    public TProcess getProcessDefinition(String id);

}
