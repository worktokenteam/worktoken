package com.worktoken.model;

import org.omg.spec.bpmn._20100524.model.TEventDefinition;

import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Transient;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Alex Pavlov (alex@rushproject.com)
 */
@Entity
@NamedQueries({
        @NamedQuery(name = "ThrowEventNode.findByProcess",
                    query = "SELECT n FROM ThrowEventNode n WHERE n.process = :process"),
        @NamedQuery(name = "ThrowEventNode.findByDefIdAndProcess",
                    query = "SELECT n FROM ThrowEventNode n WHERE n.nodeId = :defId AND n.process = :process")
})
public class ThrowEventNode extends Node {

    @Transient
    private Set<TEventDefinition> events;

    @Override
    public void tokenIn(WorkToken token, Connector connector) {
    }

    public Set<TEventDefinition> getEvents() {
        if (events == null) {
            events = new HashSet<TEventDefinition>();
        }
        return events;
    }
}
