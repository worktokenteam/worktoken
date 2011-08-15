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
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

/**
 * @author Alex Pavlov (alex@rushproject.com)
 */
@Entity
@NamedQueries({
        @NamedQuery(name = "ReceiveTask.findByProcess",
                    query = "SELECT n FROM ReceiveTask n WHERE n.process = :process"),
        @NamedQuery(name = "ReceiveTask.findByDefIdAndProcess",
                    query = "SELECT n FROM ReceiveTask n WHERE n.defId = :defId AND n.process = :process"),
        @NamedQuery(name = "ReceiveTask.findStartNodesByProcess",
                    query = "SELECT n FROM ReceiveTask n WHERE n.process = :process AND n.startEvent = true"),
        @NamedQuery(name = "ReceiveTask.findAttached",
                    query =  "SELECT n FROM ReceiveTask n WHERE n.ownerId = :nodeId"),
        @NamedQuery(name = "ReceiveTask.deleteAttached",
                    query =  "DELETE FROM ReceiveTask n WHERE n.ownerId = :nodeId"),
        @NamedQuery(name = "ReceiveTask.countAttached",
                    query =  "SELECT COUNT(n) FROM ReceiveTask n WHERE n.ownerId = :nodeId")
})
public class ReceiveTask extends Node {

    private boolean startEvent;
//    @OneToMany(mappedBy = "eventNode", cascade = CascadeType.ALL)
//    private Set<EventTrigger> triggers;
    private long ownerId;

    public boolean isStartEvent() {
        return startEvent;
    }

    public void setStartEvent(boolean startEvent) {
        this.startEvent = startEvent;
    }

    public void eventIn(EventToken event) {
        tokenOut();
    }

    /**
     * Handle incoming token.
     * <p/>
     * Default implementation carries no action. May be overridden to implement application specific token processing.
     * If overridden, does not need to call superclass method.
     * <p/>
     * @param token incoming token
     * @param connector incoming connector, the token arrived through
     */
    @Override
    public void tokenIn(WorkToken token, Connector connector) {
    }

//    public Set<EventTrigger> getTriggers() {
//        if (triggers == null) {
//            triggers = new HashSet<EventTrigger>();
//        }
//        return triggers;
//    }

    public long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(long ownerId) {
        this.ownerId = ownerId;
    }
}
