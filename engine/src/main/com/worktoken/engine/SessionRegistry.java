package com.worktoken.engine;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Alex Pavlov (alex@rushproject.com)
 */
public class SessionRegistry {
    private static Map<String, WorkSession> sessions = new HashMap<String, WorkSession>();

    public static void addSession(WorkSession session) {
        sessions.put(session.getId(), session);
    }

    public static WorkSession getSession(String id) {
        return sessions.get(id);
    }

    public static void removeSession(String id) {
        if (sessions.containsKey(id)) {
            sessions.remove(id);
        }
    }
}
