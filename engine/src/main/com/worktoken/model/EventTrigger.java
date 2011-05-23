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

import javax.persistence.*;

/**
 * @author Alex Pavlov (alex@rushproject.com)
 */
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@NamedQueries({
        @NamedQuery(name = "EventTrigger.findByDefIdAndProcess",
                    query = "SELECT t FROM EventTrigger t WHERE t.definitionId = :defId AND t.eventNode.process = :process")
//        @NamedQuery(name = "EventTrigger.deleteAttached",
//                    query = "DELETE FROM EventTrigger t WHERE t.eventNode.attachedTo = :node"),
})
//@NamedNativeQuery(name = "EventTrigger.deleteAttached",
//                  query = "DELETE t FROM EventTrigger t, CatchEventNode n WHERE t.eventNode_instanceId = n.instanceId AND n.attachedTo = :id")
public class EventTrigger {

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE)
    private long instanceId;
    @Version
    private long version;
    private String definitionId;
    @ManyToOne()
    private CatchEventNode eventNode;

    public long getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(long instanceId) {
        this.instanceId = instanceId;
    }

    public CatchEventNode getEventNode() {
        return eventNode;
    }

    public void setEventNode(CatchEventNode eventNode) {
        this.eventNode = eventNode;
    }

    /**
     * definition id is either event definition id (for simple events like timer) or
     * id of referenced definition (like message id)
     */
    public String getDefinitionId() {
        return definitionId;
    }

    public void setDefinitionId(String definitionId) {
        this.definitionId = definitionId;
    }
}
