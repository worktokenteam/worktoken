package com.worktoken.model;


import java.util.HashMap;
import java.util.Map;

/**
 * @author Alex Pavlov (alex@rushproject.com)
 */
public class EventToken {
    private String definitionId;
    private long triggerInstanceId;
    private Map<String, Object> data;

    public String getDefinitionId() {
        return definitionId;
    }

    public void setDefinitionId(String definitionId) {
        this.definitionId = definitionId;
    }

    public Map<String, Object> getData() {
        if (data == null) {
            data = new HashMap<String, Object>();
        }
        return data;
    }

    public long getTriggerInstanceId() {
        return triggerInstanceId;
    }

    public void setTriggerInstanceId(long triggerInstanceId) {
        this.triggerInstanceId = triggerInstanceId;
    }
}
