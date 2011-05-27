package com.worktoken.seam2app;

import com.worktoken.engine.ClassListAnnotationDictionary;
import com.worktoken.engine.PersistentWorkSession;
import com.worktoken.engine.WorkSession;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityManagerFactory;

/**
 * @author Alex Pavlov (alex@rushproject.com)
 */
@Scope(ScopeType.APPLICATION)
@Startup
@Name("com.worktoken.seam2app.Engine")
public class Engine {

    private WorkSession session;
    private EntityManagerFactory emf;
    ClassListAnnotationDictionary dictionary;

    @Create
    public void initEngine() {
        try {
            initSession();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private void initSession() {
        EntityManagerFactory emf = null;
        try {
            emf = (EntityManagerFactory) new InitialContext().lookup("java:/digideskEntityManagerFactory");
        } catch (NamingException e) {
            e.printStackTrace();
            throw
        }

        session = new PersistentWorkSession("seam2app", emf, dictionary);
    }

    @Destroy
    public void stopEngine() {

    }

    public WorkSession getSession() {
        return session;
    }
}
