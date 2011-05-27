package com.worktoken.seam2app.action;

import com.worktoken.seam2app.model.PrepareAnswer;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.framework.EntityHome;

import javax.persistence.EntityManager;


import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.framework.EntityHome;

import javax.persistence.EntityManager;

/**
 * @author Alex Pavlov (alex@rushproject.com)
 */
@Name("prepareAnswerHome")
@Scope(ScopeType.EVENT)
public class PrepareAnswerHome extends EntityHome<PrepareAnswer> {

    @In("entityManager")
    private EntityManager em;

    @Factory("prepareAnswer")
    public PrepareAnswer init() {
        return getInstance();
    }

    @Override
    public void setId(Object id) {
        if (id instanceof String) {
            super.setId(Long.valueOf((String)id));
        } else {
            super.setId(id);
        }
    }
}
