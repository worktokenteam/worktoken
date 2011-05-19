package com.worktoken.engine;

import com.worktoken.annotation.FlowElement;
import com.worktoken.annotation.Process;
import com.worktoken.model.BusinessProcess;
import com.worktoken.model.Node;

import java.util.List;
import java.util.logging.Logger;

/**
 * @author Alex Pavlov (alex@rushproject.com)
 */
public class ClassListAnnotationDictionary extends AnnotationDictionary {

    private List<Class> classList;
    private static Logger logger = Logger.getLogger(ClassListAnnotationDictionary.class.getName());

    public ClassListAnnotationDictionary(List<Class> classList) {
        super();
        this.classList = classList;
    }

    @Override
    public void build() {
        for (Class clazz : classList) {
            if (Node.class.isAssignableFrom(clazz)) {
                handleFlowClass(clazz);
            } else if (BusinessProcess.class.isAssignableFrom(clazz)) {
                handleProcessClass(clazz);
            } else {
                throw new IllegalArgumentException("Class " + clazz.getName() + " does extend neither Node nor BusinessProcess class");
            }
        }
        setScanned(true);
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
           logger.info("Registering FlowEntity Class " + clazz.getName() + " for \"" + annotation.nodeRef() + "\" node");
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
           logger.info("Registering Process Class " + clazz.getName() + " for \"" + annotation.processRef() + "\" process");
           getProcessDictionary().add(entry);
       }

}
