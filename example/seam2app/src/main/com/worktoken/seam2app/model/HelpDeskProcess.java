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

package com.worktoken.seam2app.model;

import com.worktoken.annotation.*;
import com.worktoken.annotation.Process;
import com.worktoken.model.BusinessProcess;

import javax.persistence.Column;
import javax.persistence.Entity;
import java.util.Date;

/**
 * @author Alex Pavlov (alex@rushproject.com)
 */
@Entity
@Process(processRef = "Help desk", refType = RefType.Name)
public class HelpDeskProcess extends BusinessProcess {
    private String email;
    private String subject;
    @Column(length = 8192)
    private String comments;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getComments() {
        return comments;
    }

    public String getHtmlComments() {
        return comments.replaceAll("\n", "<br/>");
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public void addComment(String comment, boolean byOperator) {
        comments = comments + "\n" + (new Date()).toString() + ", " + (byOperator ? "operator" : "customer") + ": " + comment + "\n";
    }
}
