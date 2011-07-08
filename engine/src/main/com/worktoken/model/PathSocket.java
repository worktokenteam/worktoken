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
public class PathSocket {
    @Id
    @GeneratedValue(strategy = GenerationType.TABLE)
    private long id;
    @Version
    private long version;
    private String defId;
    @ManyToOne()
    private Node node;
    private int tokenCount;

    public PathSocket() {}

    public PathSocket(String defId) {
        this.defId = defId;
    }

    public String getDefId() {
        return defId;
    }

    public Node getNode() {
        return node;
    }

    public void setNode(Node node) {
        this.node = node;
    }

    public int getTokenCount() {
        return tokenCount;
    }

    public void addToken() {
        ++tokenCount;
    }

    public void removeToken() {
        if (tokenCount > 0) {
            --tokenCount;
        }
        // TODO: should we throw exception if no tokens?
    }
}
