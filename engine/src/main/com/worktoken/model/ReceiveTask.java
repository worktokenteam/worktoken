package com.worktoken.model;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

/**
 * @author Alex Pavlov (alex@rushproject.com)
 */
@Entity
@NamedQueries({
        @NamedQuery(name = "ReceiveTask.findByProcess",
                    query = "SELECT n FROM ReceiveTask n WHERE n.process = :process"),
        @NamedQuery(name = "ReceiveTask.findByDefIdAndProcess",
                    query = "SELECT n FROM ReceiveTask n WHERE n.nodeId = :defId AND n.process = :process"),
        @NamedQuery(name = "ReceiveTask.findStartNodesByProcess",
                    query = "SELECT n FROM ReceiveTask n WHERE n.process = :process AND n.startEvent = true"),
        @NamedQuery(name = "ReceiveTask.findAttached",
                    query =  "SELECT n FROM ReceiveTask n WHERE n.ownerId = :nodeId"),
        @NamedQuery(name = "ReceiveTask.deleteAttached",
                    query =  "DELETE FROM ReceiveTask n WHERE n.ownerId = :nodeId"),
        @NamedQuery(name = "ReceiveTask.countAttached",
                    query =  "SELECT COUNT(n) FROM ReceiveTask n WHERE n.ownerId = :nodeId")
})
public class ReceiveTask extends Node {

    private boolean startEvent;
//    @OneToMany(mappedBy = "eventNode", cascade = CascadeType.ALL)
//    private Set<EventTrigger> triggers;
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

//    public Set<EventTrigger> getTriggers() {
//        if (triggers == null) {
//            triggers = new HashSet<EventTrigger>();
//        }
//        return triggers;
//    }

    public long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(long ownerId) {
        this.ownerId = ownerId;
    }
}
