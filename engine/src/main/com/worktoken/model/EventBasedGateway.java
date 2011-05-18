package com.worktoken.model;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: sure
 * Date: 5/8/11
 * Time: 9:05 PM
 * To change this template use File | Settings | File Templates.
 */
@Entity
@NamedQueries({
        @NamedQuery(name = "EventBasedGateway.findByProcess",
                    query = "SELECT n FROM EventBasedGateway n WHERE n.process = :process"),
        @NamedQuery(name = "EventBasedGateway.findByDefIdAndProcess",
                    query = "SELECT n FROM EventBasedGateway n WHERE n.nodeId = :defId AND n.process = :process")
})

public class EventBasedGateway extends Node {
    @Transient
    List<Node> targets;
    @Override
    public void tokenIn(WorkToken token, Connector connector) {
    }

    /*
    TODO: get rid of it, very misleading
     */
    public List<Node> getTargets() {
        if (targets == null) {
            targets = new ArrayList<Node>();
        }
        return targets;
    }
}
