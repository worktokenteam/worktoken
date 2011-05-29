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

import org.omg.spec.bpmn._20100524.model.TEventDefinition;

import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Transient;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Alex Pavlov (alex@rushproject.com)
 */
@Entity
@NamedQueries({
        @NamedQuery(name = "ThrowEventNode.findByProcess",
                    query = "SELECT n FROM ThrowEventNode n WHERE n.process = :process"),
        @NamedQuery(name = "ThrowEventNode.findByDefIdAndProcess",
                    query = "SELECT n FROM ThrowEventNode n WHERE n.defId = :defId AND n.process = :process")
})
public class ThrowEventNode extends Node {

    @Transient
    private Set<TEventDefinition> events;

    @Override
    public void tokenIn(WorkToken token, Connector connector) {
    }

    public Set<TEventDefinition> getEvents() {
        if (events == null) {
            events = new HashSet<TEventDefinition>();
        }
        return events;
    }
}
