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

package com.worktoken.engine;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Alex Pavlov (alex@rushproject.com)
 */
public abstract class AnnotationDictionary {

    private final Set<AnnotatedClass> nodeDictionary;
    private final Set<AnnotatedClass> processDictionary;
    private boolean scanned;

    protected AnnotationDictionary() {
        this.nodeDictionary = new HashSet<AnnotatedClass>();
        this.processDictionary  = new HashSet<AnnotatedClass>();
    }

    public Set<AnnotatedClass> getNodeDictionary() {
        return nodeDictionary;
    }

    public boolean isScanned() {
        return scanned;
    }

    protected void setScanned(boolean scanned) {
        this.scanned = scanned;
    }

    public Set<AnnotatedClass> getProcessDictionary() {
        return processDictionary;
    }

    public abstract void build();


    public AnnotatedClass findNode(String id, String name, String processDefId) {
        if (processDefId == null || processDefId.isEmpty()) {
            throw new IllegalArgumentException("process definition id may not be empty in call to AnnotationDictionary.findNode");
        }
        if (!scanned) {
            build();
        }
        List<AnnotatedClass> results = new ArrayList<AnnotatedClass>();
        for (AnnotatedClass ac : nodeDictionary) {
            if (ac.getProcessId() != null && !ac.getProcessId().isEmpty() && !ac.getProcessId().equals(processDefId)) {
                continue;
            }
            if (id != null && !id.isEmpty()) {
                if (ac.getId() != null && ac.getId().equals(id)) {
                    results.add(ac);
                }
            }
            if (name != null && !name.isEmpty()) {
                if (ac.getName() != null && ac.getName().equals(name)) {
                    results.add(ac);
                }
            }
        }
        if (results.size() > 1) {
            if (id == null) {
                id = "<null>";
            }
            if (name == null) {
                name = "<null>";
            }
            StringBuilder sb = new StringBuilder();
            for (AnnotatedClass ac : results) {
                if (sb.length() > 0) {
                    sb.append(", ");
                }
                sb.append(ac.getClazz());
            }
            throw new IllegalStateException("Multiple annotated classes " + sb.toString() + " for node with id=\"" + id + "\", name=\"" + name + "\", process=\"" + processDefId + "\"");
        }
        if (results.isEmpty()) {
            return null;
        }
        return results.get(0);
    }

    public AnnotatedClass findNodeById(String id, String processId) {
        for (AnnotatedClass ac : getNodeDictionary()) {
            if (processId != null && !processId.isEmpty()) {
                if (ac.getProcessId() == null || !ac.getProcessId().equals(processId)) {
                    continue;
                }
            }
            if (ac.getId() != null && ac.getId().equals(id)) {
                return ac;
            }
        }
        return null;
    }

    public AnnotatedClass findNodeById(String id) {
        return findNodeById(id, null);
    }

    public AnnotatedClass findNodeByName(String name, String processId) {
        for (AnnotatedClass ac : getNodeDictionary()) {
            if (processId != null && !processId.isEmpty()) {
                if (ac.getProcessId() == null || !ac.getProcessId().equals(processId)) {
                    continue;
                }
            }
            if (ac.getName() != null && ac.getName().equals(name)) {
                return ac;
            }
        }
        return null;
    }

    public AnnotatedClass findNodeByName(String name) {
        return findNodeByName(name, null);
    }


    public AnnotatedClass findProcess(String id, String name) {
        if (!scanned) {
            build();
        }
        List<AnnotatedClass> results = new ArrayList<AnnotatedClass>();
        for (AnnotatedClass ac : processDictionary) {
            if (id != null && !id.isEmpty()) {
                if (ac.getId() != null && ac.getId().equals(id)) {
                    results.add(ac);
                }
            }
            if (name != null && !name.isEmpty()) {
                if (ac.getName() != null && ac.getName().equals(name)) {
                    results.add(ac);
                }
            }
        }
        if (results.size() > 1) {
            if (id == null) {
                id = "<null>";
            }
            if (name == null) {
                name = "<null>";
            }
            StringBuilder sb = new StringBuilder();
            for (AnnotatedClass ac : results) {
                if (sb.length() > 0) {
                    sb.append(", ");
                }
                sb.append(ac.getClazz());
            }
            throw new IllegalStateException("Multiple annotated classes " + sb.toString() + " for process with id=\"" + id + "\", name=\"" + name + "\"");
        }
        if (results.isEmpty()) {
            return null;
        }
        return results.get(0);
    }
}
