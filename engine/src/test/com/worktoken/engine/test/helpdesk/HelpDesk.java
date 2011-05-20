package com.worktoken.engine.test.helpdesk;

import com.worktoken.engine.ClassListAnnotationDictionary;
import com.worktoken.engine.PersistentWorkSession;
import com.worktoken.model.EventToken;
import com.worktoken.model.EventTrigger;
import com.worktoken.model.TaskState;
import com.worktoken.model.UserTask;
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
        annotatedClasses.add(LookupAnswer.class);
        annotatedClasses.add(ReceiveRequest.class);
        annotatedClasses.add(PrepareAnswer.class);
        ClassListAnnotationDictionary dictionary = new ClassListAnnotationDictionary(annotatedClasses);
        dictionary.build();
        Assert.assertNotNull(dictionary.findProcess(null, "Help desk"));
        Assert.assertNotNull(dictionary.findNodeByName("Lookup answer"));
        Assert.assertNotNull(dictionary.findNodeByName("Prepare answer"));
        Assert.assertNotNull(dictionary.findNodeByName("Receive request"));

        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        session = new PersistentWorkSession("com.worktoken.TestSession", emf, dictionary);
        TDefinitions tDefinitions = session.readDefinitions(getClass().getResourceAsStream("helpdesk.bpmn"));
        Assert.assertNotNull(tDefinitions);
        Assert.assertTrue("Definition".equals(tDefinitions.getId()));

        /*
        Create process instance. We retrieve the process entity from database for verification purposes. After
        verification the entity must be detached, otherwise we will have stale version of the object pretty soon.
         */
        long processId = session.createProcess("process-com_worktoken_helpdesk");
        Assert.assertTrue(processId > 0);
        HelpDeskProcess process = em.find(HelpDeskProcess.class, processId);
        Assert.assertNotNull(process);
        em.detach(process);

        /*
        Sending "Service request" message. Please note that definition it is the one of the message, not the event
        trigger.
         */
        EventToken message = new EventToken();
        String subject = "My question";
        message.getData().put("email", "customer@example.com");
        message.getData().put("subject", subject);
        message.getData().put("question", "What's up?");
        message.setDefinitionId("ID_21465726_5737_2200_2400_000000600032");
        session.sendEventToken(message, processId);

        /*
        Wait a couple of seconds for the process to reach User Task node (Prepare Answer)
         */
        logger.info("Waiting 2 seconds for the process to reach Prepare Answer node");
        Thread.sleep(2000);

        /*
        Are the there yet?
         */
        logger.info("Verifying Prepare Answer node");
        List<UserTask> userTasks = em.createQuery("SELECT task FROM UserTask task WHERE task.process.instanceId = :id").setParameter("id", processId).getResultList();
        Assert.assertTrue(userTasks.size() == 1);
        Assert.assertTrue(userTasks.get(0) instanceof PrepareAnswer);
        PrepareAnswer userTask = (PrepareAnswer) userTasks.get(0);
        /*
        IMPORTANT: do not forget to detach the user task, otherwise we will have stale entity soon.
         */
        em.detach(userTask);
        Assert.assertTrue(subject.equals(userTask.getSubject()));
        Assert.assertTrue(userTask.getTaskState() == TaskState.Created);

        /*
        Post answer and complete user task
         */
        logger.info("Posting answer and completing the Prepare Answer task");
        userTask.setAnswer("I am fine, thanks.");
        userTask.complete();

        /*
        Wait a couple of seconds for the process to reach event based gateway node
         */
        logger.info("Waiting 2 seconds for the process to reach event based gateway node");
        Thread.sleep(2000);

        /*
        Verify gateway triggers
         */
        List<EventTrigger> triggers = em.createQuery("SELECT t FROM EventTrigger t WHERE t.eventNode.process.instanceId = :id").setParameter("id", processId).getResultList();
        Assert.assertTrue(triggers.size() == 2);    // must be 2 triggers - message event and timer event

        for (EventTrigger trigger : triggers) {
            em.detach(trigger);
        }



//        Thread.sleep(2000);
        logger.info("Closing session");
        session.close();
        logger.info("Committing application transaction. Nothing should ever happen...");
        em.getTransaction().commit();
        logger.info("Closing Entity Manager");
        em.close();
    }
}
