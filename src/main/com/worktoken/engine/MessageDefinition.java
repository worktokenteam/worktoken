package com.worktoken.engine;

import org.omg.spec.bpmn._20100524.model.TMessage;

/**
 * @author Alex Pavlov (alex@rushproject.com)
 */
public class MessageDefinition {
    TMessage tMessage;

    public MessageDefinition(TMessage tMessage) {
        this.tMessage = tMessage;
    }

    public TMessage gettMessage() {
        return tMessage;
    }

    public String getId() {
        return tMessage.getId();
    }

    public String getName() {
        return tMessage.getName();
    }

}
