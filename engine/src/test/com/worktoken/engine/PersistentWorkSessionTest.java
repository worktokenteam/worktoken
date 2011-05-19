package com.worktoken.engine;

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
import java.util.logging.Logger;

/**
 * @author Alex Pavlov (alex@rushproject.com)
 */
public class PersistentWorkSessionTest {

    private static Logger logger = Logger.getLogger(PersistentWorkSessionTest.class.getName());
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
    public void testResourceLocalPersistenceManager() throws Exception {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        session = new PersistentWorkSession("com.worktoken.TestSession", emf);
        TDefinitions tDefinitions = session.readDefinitions(getClass().getResourceAsStream("helpdesk.bpmn"));
        Assert.assertNotNull(tDefinitions);
        Assert.assertTrue("Definition".equals(tDefinitions.getId()));
        session.close();
        em.getTransaction().commit();
        em.close();
    }
}
