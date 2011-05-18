package com.worktoken.model;

import org.omg.spec.bpmn._20100524.model.TProcess;

/**
 * @author Alex Pavlov (alex@rushproject.com)
 */
public class ProcessDefinition {
    private TProcess processDefinition;
    private String definitionsId;

    public ProcessDefinition(TProcess processDefinition) {
        this.processDefinition = processDefinition;
    }

    public TProcess getProcessDefinition() {
        return processDefinition;
    }

    public String getDefinitionsId() {
        return definitionsId;
    }

    public void setDefinitionsId(String definitionsId) {
        this.definitionsId = definitionsId;
    }
}
