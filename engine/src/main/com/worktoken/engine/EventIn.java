package com.worktoken.engine;

import com.worktoken.model.EventToken;

public class EventIn extends WorkItem {
    private EventToken eventToken;

    public EventToken getEventToken() {
        return eventToken;
    }

    public void setEventToken(EventToken eventToken) {
        this.eventToken = eventToken;
    }
}
