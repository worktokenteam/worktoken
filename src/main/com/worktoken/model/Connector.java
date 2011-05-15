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
