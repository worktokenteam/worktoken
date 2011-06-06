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

import org.omg.spec.bpmn._20100524.model.TDocumentation;

import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import java.util.List;

/**
 * @author Alex Pavlov (alex@rushproject.com)
 */
@Entity
@NamedQueries({
        @NamedQuery(name = "UserTask.findAll",
                    query = "SELECT n FROM UserTask n"),
        @NamedQuery(name = "UserTask.findByProcess",
                    query = "SELECT n FROM UserTask n WHERE n.process = :process"),
        @NamedQuery(name = "UserTask.findByDefIdAndProcess",
                    query = "SELECT n FROM UserTask n WHERE n.defId = :defId AND n.process = :process")
})
public class UserTask  extends Node {

    @Override
    public void tokenIn(WorkToken token, Connector connector) {
    }

    public String getViewId() {
        return null;
    }

    public String getSubject() {
        return "Task #" + getId();
    }

    @Override
    public String getDocumentation() {
        return getDocumentation("text/plain");
    }

    /**
     * Retrieve documentation from BPMN specification.
     *
     * @param textFormat - text format: "text/plain", "text/html" etc
     * @return Documentation in specified format
     */
    @Override
    public String getDocumentation(String textFormat) {
        String documentation = super.getDocumentation(textFormat);
        return documentation == null ? "User task" : documentation;
    }
}
