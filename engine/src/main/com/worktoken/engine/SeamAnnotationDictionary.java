package com.worktoken.engine;

import com.worktoken.annotation.Process;
import com.worktoken.annotation.FlowElement;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.log.Log;

import java.util.Set;

/**
 * @author Alex Pavlov (alex@rushproject.com)
 */
@Name("com.worktoken.engine.SeamAnnotationDictionary")
@Scope(ScopeType.APPLICATION)
@Startup
//@Stateful
public class SeamAnnotationDictionary extends AnnotationDictionary {

    private static SeamAnnotationDictionary instance;
    @Logger
    Log log;

    @In("#{deploymentStrategy.annotatedClasses['com.worktoken.annotation.FlowElement']}")
    private Set<Class<Object>> flowClasses;

    @In("#{hotDeploymentStrategy.annotatedClasses['com.worktoken.annotation.FlowElement']}")
    private Set<Class<Object>> hotFlowClasses;

    @In("#{deploymentStrategy.annotatedClasses['com.worktoken.annotation.Process']}")
    private Set<Class<Object>> processClasses;

    @In("#{hotDeploymentStrategy.annotatedClasses['com.worktoken.annotation.Process']}")
    private Set<Class<Object>> hotProcessClasses;

//    private static Class[] supported = {FlowElement.class, Process.class};

    @Override
    @Create
    public void build() {
        boolean noAnnotations = true;
        if (flowClasses != null) {
            for (Class clazz : flowClasses) {
                noAnnotations = false;
                handleFlowClass(clazz);
            }
        }
        if (hotFlowClasses != null) {
            for (Class clazz : hotFlowClasses) {
                noAnnotations = false;
                handleFlowClass(clazz);
            }
        }
        if (processClasses != null) {
            for (Class clazz : processClasses) {
                noAnnotations = false;
                handleProcessClass(clazz);
            }
        }
        if (hotProcessClasses != null) {
            for (Class clazz : hotProcessClasses) {
                noAnnotations = false;
                handleProcessClass(clazz);
            }
        }
        setScanned(true);
        instance = this;
        if (noAnnotations) {
            log.info("no annotated classes found, make sure /META-INF/seam-deployment.properties is included in war archive");
        }
    }

    public static SeamAnnotationDictionary getInstance() {
        return instance;
    }

    private void handleFlowClass(Class clazz) {
        FlowElement annotation = (FlowElement) clazz.getAnnotation(FlowElement.class);
        AnnotatedClass entry = new AnnotatedClass();
        entry.setClazz(clazz.getName());
        String processId = annotation.processId(); // may not be null because of default value
        if (!processId.isEmpty()) {
            entry.setProcessId(processId);
        }
        switch (annotation.refType()) {
            case Id:
                entry.setId(annotation.nodeRef());
                break;
            case Name:
                entry.setName(annotation.nodeRef());
                break;
            default:
                throw new IllegalArgumentException("Invalid reference type: \"" + annotation.refType().toString() + "\" in annotation for class \"" + clazz.getName() + "\"");
        }
        log.info("Registering FlowEntity Class " + clazz.getName() + " for \"" + annotation.nodeRef() + "\" node");
        getNodeDictionary().add(entry);
    }

    private void handleProcessClass(Class clazz) {
        Process annotation = (Process) clazz.getAnnotation(Process.class);
        AnnotatedClass entry = new AnnotatedClass();
        entry.setClazz(clazz.getName());
        switch (annotation.refType()) {
            case Id:
                entry.setId(annotation.processRef());
                break;
            case Name:
                entry.setName(annotation.processRef());
                break;
            default:
                throw new IllegalArgumentException("Invalid reference type: \"" + annotation.refType().toString() + "\" in annotation for class \"" + clazz.getName() + "\"");
        }
        log.info("Registering Process Class " + clazz.getName() + " for \"" + annotation.processRef() + "\" process");
        getProcessDictionary().add(entry);
    }

//    @Remove
//    @Destroy
//    public void remove() {
//        instance = null;
//        log.info("removing...");
//    }

}
