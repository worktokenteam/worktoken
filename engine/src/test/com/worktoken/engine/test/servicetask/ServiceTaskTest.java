/*
 * Copyright (c) 2011. Rush Project Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.worktoken.engine.test.servicetask;

import com.worktoken.engine.ClassListAnnotationDictionary;
import com.worktoken.engine.WorkSessionImpl;
import com.worktoken.engine.test.helpdesk.*;
import com.worktoken.model.*;
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
import java.util.Date;
import java.util.List;

/**
 * @author Alex Pavlov (alex@rushproject.com)
 */
public class ServiceTaskTest {

    private Connection connection;
    private EntityManagerFactory emf;
    private WorkSessionImpl session;

    @Before
    public void setUp() throws Exception {

        /*
        Start database and create entity manager factory
         */
        System.out.println("Starting in-memory HSQL database for unit tests");
        connection = DriverManager.getConnection("jdbc:hsqldb:mem:unit-testing-jpa", "sa", "");
        emf = Persistence.createEntityManagerFactory("testPU");

        /*
        Prepare and verify annotation library
         */
        List<Class> annotatedClasses = new ArrayList<Class>();
        annotatedClasses.add(ReceiveOrder.class);
        annotatedClasses.add(DivergingGateway.class);
        annotatedClasses.add(CheckCredit.class);
        ClassListAnnotationDictionary dictionary = new ClassListAnnotationDictionary(annotatedClasses);
        dictionary.build();
//        Assert.assertNotNull(dictionary.findProcess(null, "Help desk"));
//        Assert.assertNotNull(dictionary.findNodeByName("Lookup answer"));
//        Assert.assertNotNull(dictionary.findNodeByName("Prepare answer"));
//        Assert.assertNotNull(dictionary.findNodeByName("Receive request"));
//        Assert.assertNotNull(dictionary.findNodeByName("Send answer"));
//        Assert.assertNotNull(dictionary.findNodeById("com_worktoken_helpdesk_1_55"));

        /*
        Create work session and load process definition
         */
        session = new WorkSessionImpl("com.worktoken.TestSession", emf, dictionary);
        TDefinitions tDefinitions = session.readDefinitions(getClass().getResourceAsStream("servicetask.bpmn"));
        Assert.assertNotNull(tDefinitions);
        Assert.assertTrue("serviceTask".equals(tDefinitions.getId()));
    }

    @After
    public void tearDown() throws Exception {
        session.close();
        if (emf != null) {
            emf.close();
        }
        System.out.println("Stopping in-memory HSQL database.");
        connection.createStatement().execute("SHUTDOWN");
    }

    /**
     * Test order process, path 1
     * <p/>
     * Path 1: Receive order for good credit customer and in stock item
     *
     * @throws Exception
     */
    @Test
    public void testPath1() throws Exception {


        EntityManager em = emf.createEntityManager();

        em.getTransaction().begin();

        /*
        Create process instance. We retrieve the process entity from database for verification purposes. After
        verification the entity must be detached, otherwise we will have stale version of the object pretty soon.
         */
        long processId = session.createProcess("orderProcess");
        Assert.assertTrue(processId > 0);
        BusinessProcess process = em.find(BusinessProcess.class, processId);
        Assert.assertNotNull(process);
        em.clear();

        /*
        Sending "Service request" message. Please note that definition is the one of the message, not the event
        trigger.
         */
        EventToken message = new EventToken();
        message.getData().put("customerId", "11111");
        message.getData().put("itemId", "MBP-17");
        message.setDefinitionId("newOrder");
        session.sendEventToken(message, processId);
        System.out.println("Waiting 5 seconds for the process to reach user task node");
        Thread.sleep(5000);

        /*
        Are we there yet?
         */
        Assert.assertTrue(session.isRunning());
//        System.out.println("Verifying Prepare Answer node");
//        List<UserTask> userTasks = em.createQuery("SELECT task FROM UserTask task WHERE task.process.id = :id").setParameter("id", processId).getResultList();
//        Assert.assertTrue(userTasks.size() == 1);
//        Assert.assertTrue(userTasks.get(0) instanceof PrepareAnswer);
//        PrepareAnswer userTask = (PrepareAnswer) userTasks.get(0);
//        Assert.assertTrue("Prepare answer".equals(userTask.getDocumentation()));
//        String lineDefId = userTask.getLaneDefId();
//        Assert.assertTrue(lineDefId != null);
//        Assert.assertTrue(lineDefId.equals("com_worktoken_helpdesk_1"));
//        /*
//        IMPORTANT: do not forget to detach the user task, otherwise we will have stale entity soon.
//         */
//        em.clear();
//        Assert.assertTrue(item.equals(userTask.getSubject()));
//
//        System.out.println("Posting answer and completing the Prepare Answer task");
//        userTask.setAnswer("It's alright, Ma");
//        userTask.complete();
//
//        System.out.println("Waiting 2 seconds for the process to reach event based gateway node");
//        Thread.sleep(2000);
//
//        System.out.println("Verifying gateway triggers");
//        Assert.assertTrue(session.isRunning());
//        List<EventTrigger> triggers = em.createQuery("SELECT t FROM EventTrigger t WHERE t.eventNode.process.id = :id").setParameter("id", processId).getResultList();
//        Assert.assertTrue(triggers.size() == 2);    // must be 2 triggers - message event and timer event
//        em.clear();
//
//        System.out.println("Adjusting timer alarm time to ensure it is ready to be fired");
//        TimerTrigger timer = (TimerTrigger) em.createQuery("SELECT t FROM TimerTrigger t WHERE t.eventNode.process.id = :id").setParameter("id", processId).getSingleResult();
//        timer.setNextAlarm(new Date());
//        em.merge(timer);
//        em.flush();
//        em.clear();
//        System.out.println("Committing application transaction.");
//        em.getTransaction().commit();
//        System.out.println("Closing Entity Manager");
//        em.close();
//        System.out.println("\n==================== Waiting 6 seconds for the timer to fire, this should end the process ======================\n");
//        Thread.sleep(6000);
//
//        System.out.println("\n==================== Verifying process termination =================================\n");
//        Assert.assertTrue(session.isRunning());
//        em = emf.createEntityManager();
//        Assert.assertNull(em.find(HelpDeskProcess.class, processId));
        em.close();
    }
}
