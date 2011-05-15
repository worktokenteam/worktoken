package com.worktoken.engine;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Synchronized;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.contexts.Lifecycle;

import javax.persistence.EntityManager;

/**
 * @author Alex Pavlov (alex@rushproject.com)
 */
public class SeamWorkSession extends WorkSessionImpl {

    private AnnotationDictionary dictionary;

    public SeamWorkSession(long id) {
        super(id);
    }

    @Override
    protected AnnotationDictionary getDictionary() {
        if (dictionary == null) {
            dictionary = SeamAnnotationDictionary.getInstance();
//            dictionary = (AnnotationDictionary) Contexts.getApplicationContext().get("seamAnnotationDictionary");
//            dictionary.build();
        }
        return dictionary;
    }

    @Override
    protected EntityManager findEntityManager() {
        return (EntityManager) Component.getInstance("entityManager");
    }

    @Override
    protected void startPersistence() {
        Lifecycle.beginCall();
    }

    @Override
    protected void stopPersistence() {
        Lifecycle.endCall();
    }
}
