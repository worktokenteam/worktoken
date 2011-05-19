package com.worktoken.engine.test.helpdesk;

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

    public void setComments(String comments) {
        this.comments = comments;
    }

    public void addComment(String comment, boolean byOperator) {
        comments = comments + "\n" + (new Date()).toString() + ", " + (byOperator ? "operator" : "customer") + ": " + comment + "\n";
    }
}
