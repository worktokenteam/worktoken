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
import java.util.ArrayList;
import java.util.List;

/**
 * <p><b>Event-Based gateway</b> implementation</p>
 *
 */
@Entity
@NamedQueries({
        @NamedQuery(name = "EventBasedGateway.findByProcess",
                    query = "SELECT n FROM EventBasedGateway n WHERE n.process = :process"),
        @NamedQuery(name = "EventBasedGateway.findByDefIdAndProcess",
                    query = "SELECT n FROM EventBasedGateway n WHERE n.defId = :defId AND n.process = :process")
})

public class EventBasedGateway extends Node {
    @Transient
    List<Node> targets;
    @Override
    public void tokenIn(WorkToken token, Connector connector) {
    }

    /*
    TODO: get rid of it, very misleading
     */
    public List<Node> getTargets() {
        if (targets == null) {
            targets = new ArrayList<Node>();
        }
        return targets;
    }
}
