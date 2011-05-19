package com.worktoken.engine.test.helpdesk;

import com.worktoken.engine.ClassListAnnotationDictionary;
import com.worktoken.engine.PersistentWorkSession;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.omg.spec.bpmn._20100524.model.TDefinitions;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * @author Alex Pavlov (alex@rushproject.com)
 */
public class HelpDesk {

    private static Logger logger = Logger.getLogger(HelpDesk.class.getName());
    private Connection connection;
    private EntityManagerFactory emf;
    private PersistentWorkSession session;

    @Before
    public void setUp() throws Exception {
        logger.info("Starting in-memory HSQL database for unit tests");
        Class.forName("org.hsqldb.jdbcDriver");
        connection = DriverManager.getConnection("jdbc:hsqldb:mem:unit-testing-jpa", "sa", "");
        emf = Persistence.createEntityManagerFactory("testPU");
    }

    @After
    public void tearDown() throws Exception {
        if (emf != null) {
            emf.close();
        }
        logger.info("Stopping in-memory HSQL database.");
        connection.createStatement().execute("SHUTDOWN");
    }

    @Test
    public void testHelpDesk() throws Exception {

        List<Class> annotatedClasses = new ArrayList<Class>();
        annotatedClasses.add(HelpDeskProcess.class);
        ClassListAnnotationDictionary dictionary = new ClassListAnnotationDictionary(annotatedClasses);
        dictionary.build();
        Assert.assertNotNull(dictionary.findProcess(null, "Help desk"));

        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        session = new PersistentWorkSession("com.worktoken.TestSession", emf, dictionary);
        TDefinitions tDefinitions = session.readDefinitions(getClass().getResourceAsStream("helpdesk.bpmn"));
        Assert.assertNotNull(tDefinitions);
        Assert.assertTrue("Definition".equals(tDefinitions.getId()));

        long id = session.createProcess("process-com_worktoken_helpdesk");
        Assert.assertTrue(id > 0);

        HelpDeskProcess process = em.find(HelpDeskProcess.class, id);
        Assert.assertNotNull(process);
        em.detach(process);

        session.close();
        em.getTransaction().commit();
        em.close();
    }
}
