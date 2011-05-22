package com.worktoken.engine.test.helpdesk;

import com.worktoken.engine.ClassListAnnotationDictionary;
import com.worktoken.engine.PersistentWorkSession;
import com.worktoken.model.*;
import org.junit.*;
import org.omg.spec.bpmn._20100524.model.TDefinitions;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

/**
 * @author Alex Pavlov (alex@rushproject.com)
 */
public class HelpDesk {

    private Logger logger = Logger.getLogger(HelpDesk.class.getName());
    private Connection connection;
    private EntityManagerFactory emf;
    private PersistentWorkSession session;

    @Before
    public void setUp() throws Exception {

        /*
        Start database and create entity manager factory
         */
        logger.info("Starting in-memory HSQL database for unit tests");
        Class.forName("org.hsqldb.jdbcDriver");
        connection = DriverManager.getConnection("jdbc:hsqldb:mem:unit-testing-jpa", "sa", "");
        emf = Persistence.createEntityManagerFactory("testPU");

        /*
        Prepare and verify annotation library
         */
        List<Class> annotatedClasses = new ArrayList<Class>();
        annotatedClasses.add(HelpDeskProcess.class);
        annotatedClasses.add(LookupAnswer.class);
        annotatedClasses.add(ReceiveRequest.class);
        annotatedClasses.add(PrepareAnswer.class);
        annotatedClasses.add(SendAnswer.class);
        ClassListAnnotationDictionary dictionary = new ClassListAnnotationDictionary(annotatedClasses);
        dictionary.build();
        Assert.assertNotNull(dictionary.findProcess(null, "Help desk"));
        Assert.assertNotNull(dictionary.findNodeByName("Lookup answer"));
        Assert.assertNotNull(dictionary.findNodeByName("Prepare answer"));
        Assert.assertNotNull(dictionary.findNodeByName("Receive request"));
        Assert.assertNotNull(dictionary.findNodeByName("Send answer"));

        /*
        Create work session and load process definition
         */
        session = new PersistentWorkSession("com.worktoken.TestSession", emf, dictionary);
        TDefinitions tDefinitions = session.readDefinitions(getClass().getResourceAsStream("helpdesk.bpmn"));
        Assert.assertNotNull(tDefinitions);
        Assert.assertTrue("Definition".equals(tDefinitions.getId()));

    }

    @After
    public void tearDown() throws Exception {
        session.close();
        if (emf != null) {
            emf.close();
        }
        logger.info("Stopping in-memory HSQL database.");
        connection.createStatement().execute("SHUTDOWN");
    }

    /**
     * Test help desk process, path 1
     *
     * Path 1: Receive question - Canned answer lookup fails - Operator prepares answer - Process ends on time out while
     * waiting for customer response.
     *
     * @throws Exception
     */
    @Test
    public void testHelpDeskPath1() throws Exception {


        EntityManager em = emf.createEntityManager();

        em.getTransaction().begin();
        long originalPollCycle = PersistentWorkSession.getTriggerPollCycle();
        PersistentWorkSession.setTriggerPollCycle(5000L); // poll time triggers every 5 seconds

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
        Sending "Service request" message. Please note that definition is the one of the message, not the event
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
        Are we there yet?
         */
        Assert.assertTrue(session.isRunning());
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

        logger.info("Posting answer and completing the Prepare Answer task");
        userTask.setAnswer("It's alright, Ma");
        userTask.complete();

        logger.info("Waiting 2 seconds for the process to reach event based gateway node");
        Thread.sleep(2000);

        logger.info("Verifying gateway triggers");
        Assert.assertTrue(session.isRunning());
        List<EventTrigger> triggers = em.createQuery("SELECT t FROM EventTrigger t WHERE t.eventNode.process.instanceId = :id").setParameter("id", processId).getResultList();
        Assert.assertTrue(triggers.size() == 2);    // must be 2 triggers - message event and timer event

        for (EventTrigger trigger : triggers) {
            em.detach(trigger);
        }

        logger.info("Adjusting timer alarm time to ensure it is ready to be fired");
        TimerTrigger timer = (TimerTrigger) em.createQuery("SELECT t FROM TimerTrigger t WHERE t.eventNode.process.instanceId = :id").setParameter("id", processId).getSingleResult();
        timer.setNextAlarm(new Date());
        em.merge(timer);
        em.flush();
        em.detach(timer);
        logger.info("Committing application transaction.");
        em.getTransaction().commit();
        logger.info("Closing Entity Manager");
        em.close();
        logger.info("Waiting 6 seconds for the timer to fire, this should end the process");
        Thread.sleep(6000);

        logger.info("Verifying process termination");
        Assert.assertTrue(session.isRunning());
        em = emf.createEntityManager();
        Assert.assertNull(em.find(HelpDeskProcess.class, processId));
        em.close();
        PersistentWorkSession.setTriggerPollCycle(originalPollCycle);
    }

    /**
     * Test help desk process, path 2
     *
     * Path 1: Receive question - Canned answer lookup succeeds - Process ends on receiving positive confirmation
     * from customer.
     *
     * @throws Exception
     */
    @Test
    public void testHelpDeskPath2() throws Exception {

        Assert.assertTrue(session.isRunning());
        EntityManager em = emf.createEntityManager();

        /*
        Create process instance. We retrieve the process entity from database for verification purposes. After
        verification the entity must be detached, otherwise we will have stale version of the object pretty soon.
         */
        em.getTransaction().begin();
        long processId = session.createProcess("process-com_worktoken_helpdesk");
        Assert.assertTrue(processId > 0);
        HelpDeskProcess process = em.find(HelpDeskProcess.class, processId);
        Assert.assertNotNull(process);
        em.detach(process);

        /*
        Sending "Service request" message. Please note that definition is the one of the message, not the event
        trigger.
         */
        EventToken message = new EventToken();
        String subject = "My question";
        message.getData().put("email", "customer@example.com");
        message.getData().put("subject", subject);
        message.getData().put("question", "How are you?");
        message.setDefinitionId("ID_21465726_5737_2200_2400_000000600032");
        session.sendEventToken(message, processId);
        em.getTransaction().commit();
        em.close();

        logger.info("Waiting 2 seconds for the process to reach event based gateway node");
        Thread.sleep(2000);

        System.out.println("\n====================== Verifying gateway triggers =========================\n");
        Assert.assertTrue(session.isRunning());
        em = emf.createEntityManager();
        List<EventTrigger> triggers = em.createQuery("SELECT t FROM EventTrigger t WHERE t.eventNode.process.instanceId = :id").setParameter("id", processId).getResultList();
        Assert.assertTrue(triggers.size() == 2);    // must be 2 triggers - message event and timer event
        em.close();

// TODO: receive confirmation message

        System.out.println("\n====================== Verifying process termination =========================\n");
        Assert.assertTrue(session.isRunning());
        em = emf.createEntityManager();
        Assert.assertNull(em.find(HelpDeskProcess.class, processId));
        em.close();
    }

}
