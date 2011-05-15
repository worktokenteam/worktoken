package com.worktoken.model;

import javax.persistence.Entity;

/**
 * @author Alex Pavlov (alex@rushproject.com)
 */
@Entity
public class MessageTrigger extends EventTrigger {

    private String messageName;

    public String getMessageName() {
        return messageName;
    }

    public void setMessageName(String messageName) {
        this.messageName = messageName;
    }
}
