package com.worktoken.tasklist;

import com.worktoken.engine.ClassListAnnotationDictionary;
import com.worktoken.engine.WorkSessionImpl;
import com.worktoken.model.EventToken;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.LogManager;

/**
 * @author Alex Pavlov (alex@rushproject.com)
 */
public class TaskListApp {

    public static void main(String[] args) throws SQLException, InterruptedException, IOException {

        LogManager.getLogManager().readConfiguration(LogManager.class.getResourceAsStream("/logging.properties"));
        Connection connection = DriverManager.getConnection("jdbc:hsqldb:mem:helloworld", "sa", "");
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("helloWorldPU");

        // Prepare and verify annotation library
        List<Class> annotatedClasses = new ArrayList<Class>();
        annotatedClasses.add(completeTask.class);
        ClassListAnnotationDictionary dictionary = new ClassListAnnotationDictionary(annotatedClasses);
        dictionary.build();

        // Create work session and load process definition
        WorkSessionImpl session = new WorkSessionImpl("com.worktoken.tasklist", emf, dictionary);
        session.readDefinitions(TaskListApp.class.getResourceAsStream("tasklist.bpmn"));

        // Create process
        long processId = session.createProcess("taskList");

        // Send message
        EventToken message = new EventToken();
        message.setDefinitionId("newTaskMessage");
        session.sendEventToken(message, processId);


        // Allow the process to reach User Task node (Say Hello)
        Thread.sleep(1000);

        // Fetch the task
        completeTask completeTask = (completeTask) session.getUserTasks().get(0);

        // Complete user task
        completeTask.complete();

        // Allow the process to finish
        Thread.sleep(1000);

        // Clean up: close session, entity manager factory and shutdown database
        session.close();
        if (emf != null) {
            emf.close();
        }
        connection.createStatement().execute("SHUTDOWN");
    }
}
