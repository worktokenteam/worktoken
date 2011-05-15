package com.worktoken.engine;

/**
 * @author Alex Pavlov (alex@rushproject.com)
 */
public interface WorkSessionFactory {
    public WorkSession openSession(long id);
    public void closeSession(WorkSession session);
}
