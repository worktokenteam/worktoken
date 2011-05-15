package com.worktoken.engine;

import java.io.Serializable;

/**
 * @author Alex Pavlov (alex@rushproject.com)
 */
public class WorkItem implements Serializable {
    private long processInstanceId;
    private String processDefinitionId;

    public long getProcessInstanceId() {
        return processInstanceId;
    }

    public void setProcessInstanceId(long processInstanceId) {
        this.processInstanceId = processInstanceId;
    }

    public String getProcessDefinitionId() {
        return processDefinitionId;
    }

    public void setProcessDefinitionId(String processDefinitionId) {
        this.processDefinitionId = processDefinitionId;
    }
}
