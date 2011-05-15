package com.worktoken.engine;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Alex Pavlov (alex@rushproject.com)
 */
public class SessionRegistry {
    private static Map<Long, WorkSession> sessions = new HashMap<Long, WorkSession>();

    public static Map<Long, WorkSession> getSessions() {
        return sessions;
    }

    public static WorkSession getSession(long id) {
        return sessions.get(id);
    }
}
