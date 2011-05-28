package com.worktoken.seam2app;

import com.worktoken.engine.ClassListAnnotationDictionary;
import com.worktoken.engine.PersistentWorkSession;
import com.worktoken.engine.WorkSession;
import com.worktoken.seam2.SeamAnnotationDictionary;
import com.worktoken.seam2app.model.*;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityManagerFactory;
import javax.xml.bind.JAXBException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Alex Pavlov (alex@rushproject.com)
 */
@Scope(ScopeType.APPLICATION)
@Startup
@Name("com.worktoken.seam2app.Engine")
public class Engine {

    private static final String fileName = "helpdesk.bpmn";

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
        EntityManagerFactory emf;
        try {
            emf = (EntityManagerFactory) new InitialContext().lookup("java:/seam2appEntityManagerFactory");
        } catch (NamingException e) {
            e.printStackTrace();
            throw new IllegalStateException("Failed to acquire EntityManagerFactory, " + e);
        }
        session = new PersistentWorkSession("seam2app", emf, SeamAnnotationDictionary.getInstance());
        // TODO: move loader into separate class (requires Seam annotation dictionary)
        try {
            session.readDefinitions(getClass().getResourceAsStream(fileName));
        } catch (JAXBException e) {
            throw new IllegalStateException("Failed to parse BPMN file " + fileName + ", " + e);
        }
    }

    @Destroy
    public void stopEngine() {
        try {
            session.close();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public WorkSession getSession() {
        return session;
    }
}
