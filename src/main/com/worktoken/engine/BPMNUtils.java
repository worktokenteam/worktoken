package com.worktoken.engine;

import org.omg.spec.bpmn._20100524.model.*;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Alex Pavlov (alex@rushproject.com)
 */
public class BPMNUtils {

    // ===================================================================================================== getFlowNode

    static public TFlowNode getFlowNode(final String nodeId, TProcess tProcess) {
        ElementIterator<TFlowNode> iterator = new ElementIterator<TFlowNode>(tProcess, TFlowNode.class);
        while (iterator.hasNext()) {
            TFlowNode node = iterator.next();
            if (node.getId().equals(nodeId)) {
                return node;
            }
        }
        return null;
    }

    // ============================================================================================== findOutgoingByName

    public static TSequenceFlow findOutgoingByName(TFlowNode fromNode, String name, TProcess tProcess) {
        for (QName qName :  fromNode.getOutgoing()) {
            TSequenceFlow link = find(qName.getLocalPart(), tProcess, TSequenceFlow.class);
            String linkName = link.getName();
            if (linkName != null && !linkName.isEmpty() && linkName.equals(name)) {
                return link;
            }
        }
        return null;
    }

    // ============================================================================================================ find

    public static <T> T find(String id, TProcess tProcess, Class<T> clazz) {
        TBaseElement e = findElement(id, tProcess);
        if (e == null) {
            return null;
        }
        if (clazz.isInstance(e)) {
            return clazz.cast(e);
        } else {
            throw new IllegalStateException("Element with id=\"" + id + "\" in process \"" + tProcess.getId() +
                    "\" may not cast to " + clazz.getName());
        }
    }

    // ===================================================================================================== findElement

    public static TBaseElement findElement(String id, TProcess tProcess) {
        for (JAXBElement<? extends TFlowElement> element : tProcess.getFlowElement()) {
            if (element.getValue().getId().equals(id)) {
                return element.getValue();
            }
        }
        return null;
    }

    public static TSequenceFlow findDefaultOutgoing(TExclusiveGateway gateway, TProcess tProcess) {
        Object defaultLink = gateway.getDefault();
        if (defaultLink instanceof TSequenceFlow) {
            return (TSequenceFlow) defaultLink;
        }
        return null;
    }

    // ======================================================================================================== findNext

    public static List<TFlowNode> findNext(TFlowNode fromNodeDef, TProcess tProcess) {
        List<TFlowNode> nodeList = new ArrayList<TFlowNode>();
        for (QName qName : fromNodeDef.getOutgoing()) {
            String qRef = qName.getLocalPart();
            TSequenceFlow link = find(qRef, tProcess, TSequenceFlow.class);
            if (link == null) {
                throw new IllegalStateException("Flow link \"" + qRef + "\" from node \"" + fromNodeDef.getId() + "\" is not defined in process \"" + tProcess.getId() + "\"");
            }
            if (!(link.getTargetRef() instanceof TFlowNode)) {
                throw new IllegalStateException("Target node " + link.getTargetRef().toString() + " is not of TFlowNode type");
            }
            // TODO: do we need this? (see above)
            TFlowNode node = find(((TFlowNode) link.getTargetRef()).getId(), tProcess, TFlowNode.class);
            if (node == null) {
                throw new IllegalStateException("Node node \"" + link.getTargetRef().toString() + "\" is not defined in process \"" + tProcess.getId() + "\"");
            }
            nodeList.add(node);
        }
        return nodeList;
    }


}
