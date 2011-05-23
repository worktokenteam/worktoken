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

package com.worktoken.engine.test.helloworld;

import com.worktoken.engine.ClassListAnnotationDictionary;
import com.worktoken.engine.PersistentWorkSession;
import com.worktoken.engine.test.helpdesk.HelpDeskProcess;
import com.worktoken.model.BusinessProcess;
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

/**
 * @author Alex Pavlov (alex@rushproject.com)
 */
public class HelloWorld {

    private Connection connection;
    private EntityManagerFactory emf;
    private PersistentWorkSession session;

    @Before
    public void setUp() throws Exception {

        /*
        Start database and create entity manager factory
         */
        System.out.println("Starting in-memory HSQL database");
        Class.forName("org.hsqldb.jdbcDriver");
        connection = DriverManager.getConnection("jdbc:hsqldb:mem:unit-testing-jpa", "sa", "");
        emf = Persistence.createEntityManagerFactory("testPU");

        /*
        Prepare and verify annotation library
         */
        List<Class> annotatedClasses = new ArrayList<Class>();
//        annotatedClasses.add(HelpDeskProcess.class);
        ClassListAnnotationDictionary dictionary = new ClassListAnnotationDictionary(annotatedClasses);
        dictionary.build();
//        Assert.assertNotNull(dictionary.findProcess(null, "Help desk"));

        /*
        Create work session and load process definition
         */
        session = new PersistentWorkSession("com.worktoken.helloworld", emf, dictionary);
        TDefinitions tDefinitions = session.readDefinitions(getClass().getResourceAsStream("helloworld.bpmn"));
        Assert.assertNotNull(tDefinitions);
        Assert.assertTrue("HelloWorld".equals(tDefinitions.getId()));
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

    @Test
    public void testHelloWorld() throws Exception {

        /*
        Create process instance. We retrieve the process entity from database for verification purposes. After
        verification the entity must be detached, otherwise we will have stale version of the object pretty soon.
         */
        long processId = session.createProcess("_1");
        Assert.assertTrue(processId > 0);
        EntityManager em = emf.createEntityManager();
        BusinessProcess process = em.find(HelpDeskProcess.class, processId);
        Assert.assertNotNull(process);
        em.detach(process);
        em.close();


        /*
        Wait a couple of seconds for the process to reach User Task node (Say Hello)
         */
        System.out.println("\n========================== Waiting 2 seconds for the process to reach Prepare Answer node ================\n");
        Thread.sleep(2000);

        /*
        Are we there yet?
         */
        Assert.assertTrue(session.isRunning());
        List<UserTask> userTasks = session.getUserTasks();
        Assert.assertTrue(userTasks.size() == 1);
//        Assert.assertTrue(userTasks.get(0) instanceof PrepareAnswer);
        UserTask userTask = userTasks.get(0);
        /*
        IMPORTANT: do not forget to detach the user task, otherwise we will have stale entity soon.
         */
        Assert.assertTrue(userTask.getTaskState() == TaskState.Created);

//        userTask.complete();


//        System.out.println("\n==================== Verifying process termination =================================\n");
//        Assert.assertTrue(session.isRunning());
//        em = emf.createEntityManager();
//        Assert.assertNull(em.find(HelpDeskProcess.class, processId));
//        em.close();
    }
}
