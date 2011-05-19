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
