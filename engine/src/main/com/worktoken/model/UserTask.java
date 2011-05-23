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

package com.worktoken.model;

import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

/**
 * @author Alex Pavlov (alex@rushproject.com)
 */
@Entity
@NamedQueries({
        @NamedQuery(name = "UserTask.findAll",
                    query = "SELECT n FROM UserTask n"),
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
