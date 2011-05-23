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

import org.omg.spec.bpmn._20100524.model.TFlowElement;
import org.omg.spec.bpmn._20100524.model.TProcess;

import javax.xml.bind.JAXBElement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: sure
 * Date: 5/5/11
 * Time: 10:33 PM
 * To change this template use File | Settings | File Templates.
 */
public class ElementIterator<T> implements Iterator<T> {

    private List<T> elements;
    private int index;

    @SuppressWarnings("unchecked")
    public ElementIterator(TProcess tProcess, Class<T> clazz) {
        index = 0;
        elements = new ArrayList<T>();
        for (JAXBElement<? extends TFlowElement> element : tProcess.getFlowElement()) {
            if (clazz.isInstance(element.getValue())) {
                elements.add((T) element.getValue());
            }
        }
    }

    @Override
    public boolean hasNext() {
        return (elements.size() > index);
    }

    @Override
    public T next() {
        return elements.get(index++);
    }

    @Override
    public void remove() {}
}
