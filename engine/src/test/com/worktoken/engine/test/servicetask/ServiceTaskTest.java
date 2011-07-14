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
        annotatedClasses.add(ConvergingGateway.class);
        annotatedClasses.add(CheckCredit.class);
        annotatedClasses.add(CheckStock.class);
        annotatedClasses.add(ProcessOrder.class);
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
        String customerId = "11111";
        String itemId = "MBP-17";
        message.getData().put("customerId", customerId);
        message.getData().put("itemId", itemId);
        message.setDefinitionId("newOrder");
        session.sendEventToken(message, processId);
        System.out.println("Waiting 5 seconds for the process to reach user task node");
        Thread.sleep(5000);

        /*
        Are we there yet?
         */
        Assert.assertTrue(session.isRunning());
        List<UserTask> taskList = session.getUserTasks();
        Assert.assertTrue(taskList.size() == 1);  // we have only one process running and it should arrive to "Process order" node
        ProcessOrder processOrder = (ProcessOrder) taskList.get(0);
        Assert.assertTrue(processOrder.getCustomerId().equals(customerId));
        Assert.assertTrue(processOrder.getItemId().equals(itemId));
        processOrder.complete();
        Thread.sleep(1000);
        Assert.assertTrue(session.isRunning());
        em = emf.createEntityManager();
        List<Node> nodes = em.createQuery("SELECT n FROM Node n").getResultList();
        Assert.assertTrue(nodes.isEmpty());
        Assert.assertNull(em.find(BusinessProcess.class, processId));
        em.close();
    }
}
