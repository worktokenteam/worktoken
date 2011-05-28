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

package com.worktoken.seam2;

import com.worktoken.annotation.Process;
import com.worktoken.annotation.FlowElement;
import com.worktoken.engine.AnnotatedClass;
import com.worktoken.engine.AnnotationDictionary;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.log.Log;

import java.util.Set;

/**
 * @author Alex Pavlov (alex@rushproject.com)
 */
@Name("com.worktoken.seam2.seamAnnotationDictionary")
@Scope(ScopeType.APPLICATION)
@Startup
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
            log.warn("no annotated classes found, make sure /META-INF/seam-deployment.properties is included in war archive");
        }
    }

    private void handleFlowClass(Class clazz) {
        @SuppressWarnings({"unchecked"})
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
        @SuppressWarnings({"unchecked"})
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

    public static SeamAnnotationDictionary getInstance() {
        return instance;
    }
}
