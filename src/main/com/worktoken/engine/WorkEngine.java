package com.worktoken.engine;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;

/**
 * @author Alex Pavlov (alex@rushproject.com)
 */
@Scope(ScopeType.APPLICATION)
@Startup
@Name("com.worktoken.engine.WorkEngine")
public class WorkEngine {

    private WorkSession session;

    @Create
    public void initEngine() {
        try {
            initSession();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private void initSession() {
        WorkSessionFactory factory = new SeamWorkSessionFactory();
        session = factory.openSession(1);
    }

    @Destroy
    public void stopEngine() {

    }

    public WorkSession getSession() {
        return session;
    }
}
