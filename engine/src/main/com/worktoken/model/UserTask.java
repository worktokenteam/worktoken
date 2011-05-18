package com.worktoken.model;

import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

/**
 * @author Alex Pavlov (alex@rushproject.com)
 */
@Entity
@NamedQueries({
        @NamedQuery(name = "UserTask.findByProcess",
                    query = "SELECT n FROM UserTask n WHERE n.process = :process"),
        @NamedQuery(name = "UserTask.findByDefIdAndProcess",
                    query = "SELECT n FROM UserTask n WHERE n.nodeId = :defId AND n.process = :process")
})
public class UserTask  extends Node {
    private TaskState taskState;
    private boolean suspended;

    public UserTask() {
        taskState = TaskState.Created;
    }

    public void sendResult(WorkToken token) {
        taskState = TaskState.Completed;
        tokenOut(token == null ? new WorkToken() : token);
    }

    @Override
    public void tokenIn(WorkToken token, Connector connector) {
    }

    public TaskState getTaskState() {
        return taskState;
    }

    protected void setTaskState(TaskState taskState) {
        this.taskState = taskState;
    }

    public boolean isSuspended() {
        return suspended;
    }

    protected void setSuspended(boolean suspended) {
        this.suspended = suspended;
    }

    public String getViewId() {
        return null;
    }

    public String getSubject() {
        return "Task #" + getInstanceId();
    }

    public String getDescription() {
        return "User task";
    }
}
