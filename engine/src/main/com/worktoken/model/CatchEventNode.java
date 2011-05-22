package com.worktoken.model;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Alex Pavlov (alex@rushproject.com)
 */
@Entity
@NamedQueries({
        @NamedQuery(name = "CatchEventNode.findByProcess",
                    query = "SELECT n FROM CatchEventNode n WHERE n.process = :process"),
        @NamedQuery(name = "CatchEventNode.findByDefIdAndProcess",
                    query = "SELECT n FROM CatchEventNode n WHERE n.nodeId = :defId AND n.process = :process"),
        @NamedQuery(name = "CatchEventNode.findStartNodesByProcess",
                    query = "SELECT n FROM CatchEventNode n WHERE n.process = :process AND n.startEvent = true"),
        @NamedQuery(name = "CatchEventNode.findAttached",
                    query =  "SELECT n FROM CatchEventNode n WHERE n.ownerId = :nodeId"),
        @NamedQuery(name = "CatchEventNode.deleteAttached",
                    query =  "DELETE FROM CatchEventNode n WHERE n.ownerId = :nodeId"),
        @NamedQuery(name = "CatchEventNode.countAttached",
                    query =  "SELECT COUNT(n) FROM CatchEventNode n WHERE n.ownerId = :nodeId")
})
public class CatchEventNode extends Node {

//    @Enumerated(EnumType.STRING)
//    private EventType eventType;
    private boolean startEvent;
    @OneToMany(mappedBy = "eventNode", cascade = CascadeType.ALL)
    private Set<EventTrigger> triggers;
    private long ownerId;

    public boolean isStartEvent() {
        return startEvent;
    }

    public void setStartEvent(boolean startEvent) {
        this.startEvent = startEvent;
    }

    public void eventIn(EventToken event) {
        tokenOut();
    }

    @Override
    public void tokenIn(WorkToken token, Connector connector) {
        tokenOut(token);
    }

    public Set<EventTrigger> getTriggers() {
        if (triggers == null) {
            triggers = new HashSet<EventTrigger>();
        }
        return triggers;
    }

    public long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(long ownerId) {
        this.ownerId = ownerId;
    }
}
