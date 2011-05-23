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

import org.omg.spec.bpmn._20100524.model.TSequenceFlow;

/**
 * @author Alex Pavlov (alex@rushproject.com)
 */

// TODO: do we really need the wrapper around TSequenceFlow?

public class Connector {
    private final TSequenceFlow definition;

    public Connector(TSequenceFlow definition) {
        this.definition = definition;
    }

    public TSequenceFlow getDefinition() {
        return definition;
    }

    public String getId() {
        return definition.getId();
    }

    public String getName() {
        return definition.getName();
    }
}
