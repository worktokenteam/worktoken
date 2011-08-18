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

package com.worktoken.seam2app;

import com.worktoken.engine.WorkSession;
import com.worktoken.engine.WorkSessionImpl;
import com.worktoken.seam2.SeamAnnotationDictionary;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityManagerFactory;
import javax.xml.bind.JAXBException;

/**
 * @author Alex Pavlov (alex@rushproject.com)
 */
@Scope(ScopeType.APPLICATION)
@Startup(depends = "com.worktoken.seam2.seamAnnotationDictionary")
@Name("com.worktoken.seam2app.Engine")
public class Engine {

    private static final String fileName = "helpdesk.bpmn";

    private WorkSession session;

    @Create
    public void initEngine() {
        try {
            initSession();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private void initSession() {
        EntityManagerFactory emf;
        try {
            emf = (EntityManagerFactory) new InitialContext().lookup("java:/seam2appEntityManagerFactory");
        } catch (NamingException e) {
            e.printStackTrace();
            throw new IllegalStateException("Failed to acquire EntityManagerFactory, " + e);
        }
        SeamAnnotationDictionary dictionary = SeamAnnotationDictionary.getInstance();
        if (dictionary == null) {
            throw new IllegalStateException("Missing SeamAnnotationDictionary");
        }
        session = new WorkSessionImpl("seam2app", emf, dictionary);
        session.readDefinitions(getClass().getResourceAsStream(fileName));
        session.start();
    }

    @Destroy
    public void stopEngine() {
        try {
            session.close();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public WorkSession getSession() {
        return session;
    }
}
