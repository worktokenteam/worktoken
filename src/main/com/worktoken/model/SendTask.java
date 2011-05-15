package com.worktoken.model;

import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

/**
 * @author Alex Pavlov (alex@rushproject.com)
 */
// TODO: There are no send task specific behavior so far. Consider deleting the class to simplify class hierarchy
@Entity
@NamedQueries({
        @NamedQuery(name = "SendTask.findByProcess",
                    query = "SELECT n FROM SendTask n WHERE n.process = :process"),
        @NamedQuery(name = "SendTask.findByDefIdAndProcess",
                    query = "SELECT n FROM SendTask n WHERE n.nodeId = :defId AND n.process = :process")
})
public class SendTask extends Node {

    @Override
    public void tokenIn(WorkToken token, Connector connector) {
        tokenOut(token);
    }

}
