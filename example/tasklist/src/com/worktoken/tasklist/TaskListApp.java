package com.worktoken.tasklist;

import com.worktoken.engine.ClassListAnnotationDictionary;
import com.worktoken.engine.WorkSessionImpl;
import com.worktoken.model.EventToken;
import com.worktoken.model.UserTask;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.logging.LogManager;

/**
 * @author Alex Pavlov (alex@rushproject.com)
 */
public class TaskListApp {

    private static Scanner scanner;
    private static WorkSessionImpl session;

    public static void main(String[] args) throws SQLException, InterruptedException, IOException {

        scanner = new Scanner(System.in);

        LogManager.getLogManager().readConfiguration(LogManager.class.getResourceAsStream("/logging.properties"));
        Connection connection = DriverManager.getConnection("jdbc:hsqldb:mem:tasklist", "sa", "");
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("taskListPU");

        // Prepare and verify annotation library
        List<Class> annotatedClasses = new ArrayList<Class>();
        annotatedClasses.add(CompleteTask.class);
        annotatedClasses.add(Start.class);
        ClassListAnnotationDictionary dictionary = new ClassListAnnotationDictionary(annotatedClasses);
        dictionary.build();

        // Create work session and load process definition
        session = new WorkSessionImpl("com.worktoken.tasklist", emf, dictionary);
        session.readDefinitions(TaskListApp.class.getResourceAsStream("tasklist.bpmn"));

        do {
            String choice = mainMenu();
            if ("1".equals(choice)) {
                taskList();
            } else if ("2".equals(choice)) {
                addTask(getResponse("Short description: "));
            } else if ("x".equalsIgnoreCase(choice)) {
                break;
            } else {
                System.out.println("\nUnknown command: " + choice);
            }
        } while (true);

//        // Fetch the task
//        CompleteTask CompleteTask = (CompleteTask) session.getUserTasks().get(0);
//
//        // Complete user task
//        CompleteTask.complete();
//
//        // Allow the process to finish
//        Thread.sleep(1000);

        // Clean up: close session, entity manager factory and shutdown database
        session.close();
        if (emf != null) {
            emf.close();
        }
        connection.createStatement().execute("SHUTDOWN");
    }

    private static void taskList() {
        do {
            List<UserTask> tasks = session.getUserTasks();
            if (tasks.size() > 0) {
                System.out.println("\nSelect task\n-----------");
                for (int i = 0; i < tasks.size(); ++i) {
                    System.out.println((i + 1) + " " + tasks.get(i).getSubject());
                }
            } else {
                System.out.println("\nNo tasks found");
            }
            int choice = getIntResponse("0 - Main Menu\n>");
            if (choice == 0) {
                return;
            }
            if (choice >= tasks.size()) {
                System.out.println("\nNo such task: " + choice);
            }
            completeTask(tasks.get(choice - 1));
        } while (true);
    }

    private static void completeTask(UserTask userTask) {
        System.out.println("\nTask completed: " + userTask.getSubject() + "\n");

    }

    private static String mainMenu() {
        return getResponse("\nMain Menu\n=========\n1 - Task List\n2 - New Task\nX - Exit\n>");
    }

    private static String getResponse(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine();
    }

    private static int getIntResponse(String prompt) {
        System.out.print(prompt);
        return scanner.nextInt();
    }

    private static void addTask(String subject) {
        // Create process
        long processId = session.createProcess("taskList");

        // Send message
        EventToken message = new EventToken();
        message.setDefinitionId("newTaskMessage");
        message.getData().put("subject", subject);
        session.sendEventToken(message, processId);
    }
}
