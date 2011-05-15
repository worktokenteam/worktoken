package com.worktoken.engine;

import javax.persistence.EntityManagerFactory;

/**
 * @author Alex Pavlov (alex@rushproject.com)
 */
public class SeamWorkSessionFactory implements WorkSessionFactory {

    public WorkSession openSession(long id) {
        return new SeamWorkSession(id);
    }

    public void closeSession(WorkSession session) {
    }
}
