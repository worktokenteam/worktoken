package com.worktoken.model;

import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

/**
 * @author Alex Pavlov (alex@rushproject.com)
 */
@Entity
@NamedQueries({
        @NamedQuery(name = "BusinessRuleTask.findByProcess",
                    query = "SELECT n FROM BusinessRuleTask n WHERE n.process = :process"),
        @NamedQuery(name = "BusinessRuleTask.findByDefIdAndProcess",
                    query = "SELECT n FROM BusinessRuleTask n WHERE n.nodeId = :defId AND n.process = :process")
})
public class BusinessRuleTask extends Node {
    @Override
    public void tokenIn(WorkToken token, Connector connector) {
    }
}
