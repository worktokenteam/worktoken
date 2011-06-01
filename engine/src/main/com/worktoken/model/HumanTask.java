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

/**
 * @author Alex Pavlov (alex@rushproject.com)
 */
public class HumanTask extends UserTask {
    private TaskState taskState;
    private boolean suspended;

    public HumanTask() {
        taskState = TaskState.Created;
    }

    public void sendResult(WorkToken token) {
        taskState = TaskState.Completed;
        tokenOut(token);
    }

    public void sendResult() {
        sendResult(new WorkToken());
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


}
