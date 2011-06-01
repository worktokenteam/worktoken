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

import com.worktoken.model.*;
import org.omg.spec.bpmn._20100524.di.BPMNDiagram;
import org.omg.spec.bpmn._20100524.model.*;
import org.omg.spec.dd._20100524.dc.Bounds;
import org.omg.spec.dd._20100524.di.Diagram;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.NoResultException;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.*;


/**
 * @author Alex Pavlov (alex@rushproject.com)
 */

public class PersistentWorkSession implements WorkSession, Runnable {

    private String sessionId;

    // persistence stuff
    private EntityManagerFactory emf;
    private ThreadLocal<EntityManager> em;
    private ThreadLocal<Integer> acquireCounter;

    // runner thread stuff
    private ExecutorService executor;
    Future<?> future;
    private volatile boolean cancelled;
    private LinkedBlockingQueue<WorkItem> workItems;
    private static long TriggerPollCycle = 60000L;
    private long lastTriggerPollTime = 0L;

    // BPMN stuff
    private ThreadLocal<Boolean> tokenOut;
    private AnnotationDictionary dictionary;
    private HashMap<String, TDefinitions> definitionsMap;
    private HashMap<String, ProcessDefinition> processDefinitions;
    private HashMap<String, MessageDefinition> messageDefinitions;
    private long threadId;

    // =========================================================================================== PersistentWorkSession

    public PersistentWorkSession(String id, EntityManagerFactory emf, AnnotationDictionary dictionary) {
        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("Null or missing session id in PersistentWorkSession constructor");
        }
        if (SessionRegistry.getSession(id) != null) {
            throw new IllegalArgumentException("Duplicate session id in PersistentWorkSession constructor");
        }
        if (emf == null) {
            throw new IllegalArgumentException("Null EntityManagerFactory in PersistentWorkSession constructor");
        }
        this.emf = emf;
        this.sessionId = id;
        if (dictionary != null) {
            this.dictionary = dictionary;
        } else {
            dictionary = new AnnotationDictionary() {
                @Override
                public void build() {
                }
            };
            dictionary.setScanned(true);
        }
        SessionRegistry.addSession(this);
        workItems = new LinkedBlockingQueue<WorkItem>();
        executor = Executors.newFixedThreadPool(10);
        em = new ThreadLocal<EntityManager>();
        acquireCounter = new ThreadLocal<Integer>();
        tokenOut = new ThreadLocal<Boolean>();
        future = executor.submit((Runnable) this);
    }

    // =========================================================================================================== getId

    @Override
    public String getId() {
        return sessionId;
    }

    // ============================================================================================================= run

    @Override
    public void run() {
        boolean interrupted = false;
        threadId = Thread.currentThread().getId();
        while (!cancelled) {
            WorkItem workItem = null;
            try {
                workItem = workItems.poll(500, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                cancelled = true;
                interrupted = true;
            }
            if (workItem != null) {
                acquireEntityManager();

                BusinessProcess process = em.get().find(BusinessProcess.class, workItem.getProcessInstanceId());
                if (process == null) {
                    releaseEntityManager();
                    throw new IllegalStateException("Process does not exist (id=" + workItem.getProcessInstanceId() + ")");
                }

                beginTransaction();

                if (workItem instanceof TokenForNode) {
                    TokenForNode t4n = (TokenForNode) workItem;
                    Node node;
                    try {
                        node = findOrCreateNode(t4n.getNodeDef(), process);
                        node.tokenIn(t4n.getToken(), t4n.getConnector());
                    } catch (Exception e) {
                        markRollbackTransaction();
                        commitTransaction();
                        releaseEntityManager();
                        e.printStackTrace();
                        throw new IllegalStateException("Failed to process target node, " + e);
                    }
                    boolean endProcess = false;
                    if (isEndEvent(t4n.getNodeDef())) {
                        endProcess = processEndEvent(node, process);
                    }
                    if (endProcess) {
                        em.get().remove(process);
                    }
                } else if (workItem instanceof TokenFromNode) {
                    try {
                        handleTokenFromNode((TokenFromNode) workItem);
                    } catch (Exception e) {
                        markRollbackTransaction();
                        commitTransaction();
                        releaseEntityManager();
                        e.printStackTrace();
                        throw new IllegalStateException("Failed to process outgoing token from node id=" + ((TokenFromNode) workItem).getNodeId() + ", " + e);
                    }
                } else if (workItem instanceof EventIn) {
                    try {
                        handleEventToken((EventIn) workItem);
                    } catch (Exception e) {
                        markRollbackTransaction();
                        commitTransaction();
                        releaseEntityManager();
                        e.printStackTrace();
                        throw new IllegalStateException("Failed to process event id=" + ((EventIn) workItem).getEventToken().getDefinitionId() + ", " + e);
                    }
                }
                commitTransaction();
                releaseEntityManager();
            }
            fireTimers();
        }
        if (interrupted) {
            Thread.currentThread().interrupt();
        }
    }

    // ======================================================================================================= isRunning

    public boolean isRunning() {
        return !future.isDone();
    }

    // ====================================================================================================== fireTimers

    private void fireTimers() {
        if (System.currentTimeMillis() - lastTriggerPollTime > TriggerPollCycle) {
            acquireEntityManager();
            beginTransaction();
            @SuppressWarnings("unchecked")
            List<TimerTrigger> triggers = em.get().createNamedQuery("TimerTrigger.findAlerts").setParameter("date", new Date()).getResultList();
            try {
                for (TimerTrigger trigger : triggers) {
                    /*
                   we do not care for repeat (cycle) events for now, each timer trigger fired once or never.
                   First thing we should do is to disarm trigger, to avoid false alarms
                    */
                    trigger.disarm();
                    EventIn eventIn = new EventIn();
                    BusinessProcess process = trigger.getEventNode().getProcess();
                    eventIn.setProcessInstanceId(process.getInstanceId());
                    eventIn.setProcessDefinitionId(process.getDefinitionId());
                    EventToken token = new EventToken();
                    token.setTriggerInstanceId(trigger.getInstanceId());
                    eventIn.setEventToken(token);
                    workItems.add(eventIn);
                }
            } finally {
                commitTransaction();
                releaseEntityManager();
                lastTriggerPollTime = System.currentTimeMillis();
            }
        }
    }

    // ======================================================================================================== findNode

    protected Node findNode(TFlowNode nodeDef, BusinessProcess process) {
        acquireEntityManager();
        String query;
        if (nodeDef instanceof TUserTask) {
            query = "UserTask";
        } else if (nodeDef instanceof TSendTask) {
            query = "SendTask";
        } else if (nodeDef instanceof TCatchEvent) {
            query = "CatchEventNode";
        } else if (nodeDef instanceof TThrowEvent) {
            query = "ThrowEventNode";
        } else if (nodeDef instanceof TBusinessRuleTask) {
            query = "BusinessRuleTask";
        } else if (nodeDef instanceof TExclusiveGateway) {
            query = "ExclusiveGateway";
        } else if (nodeDef instanceof TEventBasedGateway) {
            query = "EventBasedGateway";
        } else {
            throw new IllegalStateException("Unsupported node type: " + nodeDef.getClass().getName());
        }
        try {
            return (Node) em.get().createNamedQuery(
                    new StringBuilder(query).append(".findByDefIdAndProcess").toString()).setParameter("defId", nodeDef.getId()).setParameter("process", process).getSingleResult();
        } catch (NoResultException e) {
            return null;
        } finally {
            releaseEntityManager();
        }
    }

    // ================================================================================================ findOrCreateNode

    protected Node findOrCreateNode(TFlowNode nodeDef, BusinessProcess process) {
        Node node = findNode(nodeDef, process);
        if (node != null) {
            return node;
        }
        return createNode(nodeDef, process);
    }

    // ================================================================================================= processEndEvent

    private boolean processEndEvent(Node node, BusinessProcess process) {
        assert isRunner();
        if (isTerminateEvent(node)) {
            acquireEntityManager();
            // TODO: FIX! absolutely wrong, just run delete query
            for (Node n : findNodes(process)) {
                if (em.get().contains(n)) {
                    em.get().remove(n);
                }
            }
            releaseEntityManager();
            return true;
        } else {
            cleanUpStartNodes(process);
        }
        Long count = (Long) em.get().createNamedQuery("Node.countByProcess").setParameter("process", process).getSingleResult();
        return count == 0;
    }

    // ================================================================================================ isTerminateEvent

    private boolean isTerminateEvent(Node node) {
        if (node instanceof ThrowEventNode) {
            for (TEventDefinition eventDefinition : ((ThrowEventNode) node).getEvents()) {
                if (eventDefinition instanceof TTerminateEventDefinition) {
                    return true;
                }
            }
        }
        return false;
    }

    // ====================================================================================================== isEndEvent

    private boolean isEndEvent(TFlowNode flowNode) {
        return flowNode.getOutgoing().isEmpty();
    }

    // =========================================================================== sendToken(token, fromNode, connector)

    @Override
    public void sendToken(WorkToken token, Node fromNode, Connector connector) {
        tokenOut.set(true);
        if (!isRunner()) {
            fromNode = (Node) merge(fromNode);
        }
        TokenFromNode tokenFromNode = new TokenFromNode();
        tokenFromNode.setToken(token);
        BusinessProcess process = fromNode.getProcess();
        tokenFromNode.setProcessInstanceId(process.getInstanceId());
        tokenFromNode.setProcessDefinitionId(process.getDefinitionId());
        tokenFromNode.setNodeId(fromNode.getDefId());
        tokenFromNode.setConnector(connector);
        workItems.add(tokenFromNode);
    }

    // ============================================================================================= handleTokenFromNode

    private void handleTokenFromNode(TokenFromNode tokenFromNode) {
        TFlowNode target;
        if (tokenFromNode.getConnector() == null) {
            List<TFlowNode> toNodeDefs = findNext(tokenFromNode.getNodeId(), tokenFromNode.getProcessDefinitionId());
            if (toNodeDefs.size() == 0) {
                throw new IllegalStateException("No paths from \"" + tokenFromNode.getNodeId() + "\"");
            }
            if (toNodeDefs.size() > 1) {
                throw new IllegalStateException("Multiple paths from \"" + tokenFromNode.getNodeId() + "\", please specify Connector");
            }
            target = toNodeDefs.get(0);
        } else {
            Connector connector = tokenFromNode.getConnector();
            if (!(connector.getDefinition().getTargetRef() instanceof TFlowNode)) {
                throw new IllegalStateException("Target node " + connector.getDefinition().getTargetRef().toString() + " is not of TFlowNode type");
            }
            target = (TFlowNode) connector.getDefinition().getTargetRef();
        }
        TokenForNode t4n = new TokenForNode();
        t4n.setToken(tokenFromNode.getToken());
        t4n.setNodeDef(target);
        t4n.setConnector(createConnector(tokenFromNode.getNodeId(), target.getId()));
        acquireEntityManager();
        TFlowNode tSource = BPMNUtils.getFlowNode(tokenFromNode.getNodeId(), getProcessDefinition(tokenFromNode.getProcessDefinitionId()));
        BusinessProcess process = em.get().find(BusinessProcess.class, tokenFromNode.getProcessInstanceId());
        t4n.setProcessInstanceId(process.getInstanceId());
        t4n.setProcessDefinitionId(process.getDefinitionId());
        Node source = findNode(tSource, process);
        if (source != null) {
            if (source instanceof CatchEventNode) {
                CatchEventNode eventNode = (CatchEventNode) source;
                if (eventNode.isStartEvent()) {
                    cleanUpStartNodes(source.getProcess());
                } else if (eventNode.getOwnerId() != 0) {
                    Node owner = getNode(eventNode.getOwnerId());
                    if (owner != null) {
                        if (owner instanceof EventBasedGateway) {
                            handleGatewayEvent((EventBasedGateway) owner, eventNode);
                        } else {
                            // TODO: implement processing of boundary events
                            throw new IllegalStateException("Not implemented yet");
                        }
                    } else {
                        throw new IllegalStateException("Owner node for catch event " + eventNode.getDefId() + " does not exist");
                    }
                }
            } else {
                em.get().remove(source);
            }
            source.setProcess(null);
        }
        workItems.add(t4n);
        releaseEntityManager();
    }

    // ============================================================================================== handleGatewayEvent

    /**
     * Handles event caught by one of the target nodes of event based gateway
     *
     * @param gateway   - event based gateway entity
     * @param eventNode - target node entity (either catch event or receive task)
     */
    private void handleGatewayEvent(EventBasedGateway gateway, Node eventNode) {
        assert isRunner();
        // delete event node (token is already out)
        if (em.get().contains(eventNode)) {
            em.get().remove(eventNode);
        }
        TEventBasedGateway tGateway = BPMNUtils.find(gateway.getDefId(), getProcessDefinition(gateway.getProcess().getDefinitionId()), TEventBasedGateway.class);
        if (tGateway.getEventGatewayType() == TEventBasedGatewayType.EXCLUSIVE) {
            /*
            if exclusive gateway, delete all other target nodes
             */
            for (CatchEventNode catchEventNode : (List<CatchEventNode>) em.get().createNamedQuery("CatchEventNode.findAttached").setParameter("nodeId", gateway.getId()).getResultList()) {
                em.get().remove(catchEventNode);
            }
            for (ReceiveTask receiveTask : (List<ReceiveTask>) em.get().createNamedQuery("ReceiveTask.findAttached").setParameter("nodeId", gateway.getId()).getResultList()) {
                em.get().remove(receiveTask);
            }
            /*
            finally, delete the gateway
             */
            if (em.get().contains(gateway)) {
                em.get().remove(gateway);
            }
        } else {
            /*
            if parallel gateway, delete it only if no targets remaining
             */
            Long targetCount = (Long) em.get().createNamedQuery("CatchEventNode.countAttached").setParameter("nodeId", gateway.getId()).getSingleResult();
            targetCount += (Long) em.get().createNamedQuery("ReceiveTask.countAttached").setParameter("nodeId", gateway.getId()).getSingleResult();
            if (targetCount == 0) {
                if (em.get().contains(gateway)) {
                    em.get().remove(gateway);
                }
            }
        }
    }

    // ====================================================================================== sendToken(token, fromNode)

    @Override
    public void sendToken(WorkToken token, Node fromNode) {
        tokenOut.set(true);
        if (!isRunner()) {
            fromNode = (Node) merge(fromNode);
        }
        TokenFromNode tokenFromNode = new TokenFromNode();
        tokenFromNode.setToken(token);
        BusinessProcess process = fromNode.getProcess();
        tokenFromNode.setProcessInstanceId(process.getInstanceId());
        tokenFromNode.setProcessDefinitionId(process.getDefinitionId());
        tokenFromNode.setNodeId(fromNode.getDefId());
        workItems.add(tokenFromNode);
    }

    // =========================================================================================================== close

    @Override
    public void close() {
        cancelled = true;
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            SessionRegistry.removeSession(getId());
        }
    }

    // =============================================================================================== cleanUpStartNodes

    @SuppressWarnings("unchecked")
    private void cleanUpStartNodes(BusinessProcess process) {
        acquireEntityManager();
        List<CatchEventNode> nodes = em.get().createNamedQuery("CatchEventNode.findStartNodesByProcess").setParameter("process", process).getResultList();
        for (CatchEventNode node : nodes) {
            em.get().remove(node);
        }
        releaseEntityManager();
    }

    // ================================================================================================= createConnector

    private Connector createConnector(String from, String to) {
        // TODO: implement it! If we need it, though...
        return null;
    }

    // ======================================================================================================== findNext

    private List<TFlowNode> findNext(final String nodeId, final String processId) {
        TProcess tProcess = getProcessDefinitions().get(processId).getProcessDefinition();
        TFlowNode fromNodeDef = BPMNUtils.find(nodeId, tProcess, TFlowNode.class);
        if (fromNodeDef == null) {
            throw new IllegalStateException("From node \"" + nodeId + "\" is not defined in process \"" + processId + "\"");
        }
        return BPMNUtils.findNext(fromNodeDef, tProcess);
    }

    @Override
    public void sendTokens(Map<Connector, WorkToken> tokens) {
        tokenOut.set(true);
        // TODO: implement
        throw new IllegalStateException("Not implemented");
    }

    // ================================================================================================ handleEventToken

    private void handleEventToken(EventIn eventIn) {
        assert isRunner();  // may not be called from application thread
        BusinessProcess process = em.get().find(BusinessProcess.class, eventIn.getProcessInstanceId());
        EventToken token = eventIn.getEventToken();
        /*
        We must find the trigger (it is a previously persisted entity). For timer triggers event token already keeps
        the entity id, because timer events are fired by polling the triggers. Message events are different,
        they provide definition id and we have to run query against the database.
         */
        EventTrigger trigger;
        if (token.getTriggerInstanceId() != 0) {
            trigger = em.get().find(EventTrigger.class, token.getTriggerInstanceId());
        } else {
            trigger = (EventTrigger) em.get().createNamedQuery("EventTrigger.findByDefIdAndProcess").setParameter("defId", eventIn.getEventToken().getDefinitionId()).setParameter("process", process).getSingleResult();
        }
        if (trigger != null) {
            /*
                We do not handle start event case (do not delete other start events) here, because catch event node
                is supposed to call tokenOut() immediately and tokenOut (through sendToken()) will take care of
                multiple start event nodes.
             */
            CatchEventNode node = trigger.getEventNode();
            node.eventIn(token);
            /*
            Catch event object may change after eventIn(), so we do merge. Since we may safely assume that we are in the
            engine thread, we use Entity Manager directly.
             */
            if (em.get().contains(node)) {
                trigger.setEventNode(em.get().merge(node));
            }
        }
    }

    // ================================================================================================== sendEventToken

    @Override
    public void sendEventToken(EventToken eventToken, long processId) {
        EventIn eventIn = new EventIn();
        eventIn.setProcessInstanceId(processId);
        eventIn.setEventToken(eventToken);
        workItems.add(eventIn);
    }

    // ================================================================================================= readDefinitions

    /**
     * Reads BPMN definitions from InputStream
     *
     * @param stream - BPMN definition input stream
     * @return initialized TDefinitions data structure
     */
    @Override
    public TDefinitions readDefinitions(InputStream stream) throws JAXBException {
        String packageName1 = TDefinitions.class.getPackage().getName();
        String packageName2 = BPMNDiagram.class.getPackage().getName();
        String packageName3 = Bounds.class.getPackage().getName();
        String packageName4 = Diagram.class.getPackage().getName();
        JAXBContext jc = JAXBContext.newInstance(packageName1 + ":" + packageName2 + ":" + packageName3 + ":" + packageName4);
        Unmarshaller u = jc.createUnmarshaller();
        u.setEventHandler(new javax.xml.bind.helpers.DefaultValidationEventHandler());
        @SuppressWarnings("unchecked")
        JAXBElement<TDefinitions> doc = (JAXBElement<TDefinitions>) u.unmarshal(stream);
        TDefinitions tDefinitions = doc.getValue();
        String id = tDefinitions.getId();
        if (id == null || id.isEmpty()) {
            if (getDefinitionsMap().isEmpty()) {
                id = "com.worktoken.definitions.defaultId";
            } else {
                throw new IllegalArgumentException("Definitions set does not have ID");
            }
        }
        if (getDefinitionsMap().containsKey(id)) {
            throw new IllegalArgumentException("Duplicate ID (\"" + id + "\") for definitions");
        }
        getDefinitionsMap().put(id, tDefinitions);
        scanForProcesses(tDefinitions);
        scanForMessages(tDefinitions);
        return tDefinitions;
    }

    // ================================================================================================== getDefinitions

    @Override
    public TDefinitions getDefinitions(String id) {
        if (getDefinitionsMap().containsKey(id)) {
            return getDefinitionsMap().get(id);
        }
        return null;
    }

    // =============================================================================================== getDefinitionsIds

    @Override
    public Set<String> getDefinitionsIds() {
        return getDefinitionsMap().keySet();
    }

    // ================================================================================================= dropDefinitions

    @Override
    public void dropDefinitions(String id) {
        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("Null or empty definitions ID");
        }
        if (!getDefinitionsMap().containsKey(id)) {
            throw new IllegalArgumentException("No such definitions (ID=\"" + id + "\")");
        }
        // TODO: make sure no running processes from this definitions exist.
        getDefinitionsMap().remove(id);
    }

    // ======================================================================================================== isRunner

    private boolean isRunner() {
        return Thread.currentThread().getId() == threadId;
    }

    // =================================================================================================== createProcess

    @Override
    public long createProcess(String id) {
        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("Null or empty process id in call to createProcess");
        }
        if (!getProcessDefinitions().containsKey(id)) {
            throw new IllegalArgumentException("No process with id \"" + id + "\"");
        }
        TProcess tProcess = getProcessDefinitions().get(id).getProcessDefinition();
        BusinessProcess process;
        AnnotatedClass ac = dictionary.findProcess(id, tProcess.getName());
        if (ac == null) {
            process = new BusinessProcess();
        } else {
            Object entity;
            try {
                entity = Class.forName(ac.getClazz()).newInstance();
            } catch (Exception e) {
                throw new IllegalStateException("Failed to instantiate object of class " + ac.getClazz() + " due to " + e);
            }
            if (!(entity instanceof BusinessProcess)) {
                throw new IllegalStateException("Annotated business process class " + ac.getClazz() + " does not extend BusinessProcess class");
            }
            process = (BusinessProcess) entity;
        }
        acquireEntityManager();
        process.setDefinitionId(id);
        process.setSessionId(getId());
        persist(process);
        final List<TActivity> startActivities = new ArrayList<TActivity>();
        final List<TCatchEvent> noneStartEvents = new ArrayList<TCatchEvent>();
        final List<TCatchEvent> startEvents = new ArrayList<TCatchEvent>();
        forEachStartNode(tProcess,
                new EachNodeHandler() {
                    public boolean node(TFlowNode tNode) {
                        if (tNode instanceof TCatchEvent) {
                            if (((TCatchEvent) tNode).getEventDefinition().isEmpty()) {
                                noneStartEvents.add((TCatchEvent) tNode);
                            } else {
                                startEvents.add((TCatchEvent) tNode);
                            }
                        } else if (tNode instanceof TActivity) {
                            startActivities.add((TActivity) tNode);
                        }
                        return true;
                    }
                });
        /*
            All start activities (no incoming links) must be instantiated
         */
        for (TActivity activity : startActivities) {
            Node node = createNode(activity, process);
            tokenOut.set(false);
            node.tokenIn(new WorkToken(), null);
            if (!isRunner() && !tokenOut.get()) {
                merge(node);
            }
        }
        /*
            Only one start event will be triggered, if there is at least one None Start Event, it will be triggered,
            other start events will be ignored.
         */
        if (noneStartEvents.isEmpty()) {
            for (TCatchEvent start : startEvents) {
                CatchEventNode node = (CatchEventNode) createNode(start, process);
                node.setStartEvent(true);
                if (!isRunner()) {
                    merge(node);
                }
            }
        } else {
            CatchEventNode node = (CatchEventNode) createNode(noneStartEvents.get(0), process);
            tokenOut.set(false);
            node.eventIn(new EventToken());
            node.setStartEvent(true);
            if (!isRunner() && !tokenOut.get()) {
                merge(node);
            }
        }
        if (isRunner()) {
            em.get().flush();   // we need to flush here to ensure the process instance id is not empty
        }
        releaseEntityManager();
        return process.getInstanceId();
    }

    // ====================================================================================================== createNode

    private Node createNode(TFlowNode tNode, BusinessProcess process) {
        if (tNode instanceof TCatchEvent) {
            CatchEventNode node = createCatchEventNode((TCatchEvent) tNode, process, null);
            node.setSession(this);
            persist(node);
            return node;
        }
        if (tNode instanceof TUserTask) {
            UserTask node = instantiateNode(tNode, UserTask.class, process);
            TLane lane = BPMNUtils.findLaneForNode(tNode, process.getDefinition());
            if (lane != null) {
                node.setLaneDefId(lane.getId());
            }
            node.setSession(this);
            persist(node);
            return node;
        }
        if (tNode instanceof TBusinessRuleTask) {
            BusinessRuleTask node = instantiateNode(tNode, BusinessRuleTask.class, process);
            node.setSession(this);
            persist(node);
            return node;
        }
        if (tNode instanceof TSendTask) {
            SendTask node = instantiateNode(tNode, SendTask.class, process);
            node.setSession(this);
            persist(node);
            return node;
        }
        if (tNode instanceof TEventBasedGateway) {
            EventBasedGateway node = createEventBasedGateway((TEventBasedGateway) tNode, process);
            node.setSession(this);
            persist(node);
            for (Node target : node.getTargets()) {
                if (target instanceof CatchEventNode) {
                    ((CatchEventNode) target).setOwnerId(node.getId());
                } else if (target instanceof ReceiveTask) {
                    ((ReceiveTask) target).setOwnerId(node.getId());
                }
                persist(target);
            }
            return node;
        }
        if (tNode instanceof TExclusiveGateway) {
            ExclusiveGateway node = instantiateNode(tNode, ExclusiveGateway.class, process);
            node.setSession(this);
            // do not persist
            return node;
        }
        if (tNode instanceof TThrowEvent) {
            ThrowEventNode node = createThrowEventNode((TThrowEvent) tNode, process);
            node.setSession(this);
            // do not persist
            return node;
        }
        throw new IllegalArgumentException("Unknown or unsupported node type: process=\"" + process.getDefinitionId() +
                "\", node(id=\"" + tNode.getId() + "\", name=\"" + tNode.getName() + "\")");
    }

    // ================================================================================================= instantiateNode

    private <T extends Node> T instantiateNode(TFlowNode tNode, Class<T> nClass, BusinessProcess process) {
        Node node;
        AnnotatedClass ac = dictionary.findNode(tNode.getId(), tNode.getName(), process.getDefinitionId());
        if (ac == null) {
            try {
                node = nClass.newInstance();
            } catch (Exception e) {
                e.printStackTrace();
                throw new IllegalStateException("Failed to instantiate object of type  " + nClass.getName() + ", due to " + e);
            }
            node.setClassName(nClass.getName());
        } else {
            Object entity;
            try {
                entity = Class.forName(ac.getClazz()).newInstance();
            } catch (Exception e) {
                throw new IllegalStateException("Failed to instantiate object of class " + ac.getClazz() + " due to " + e);
            }
            if (!nClass.isInstance(entity)) {
                throw new IllegalStateException("Annotated class " + ac.getClazz() + " does not extend " + nClass.getName() + " class");
            }
            node = (Node) entity;
            node.setClassName(ac.getClazz());
        }
        node.setDefId(tNode.getId());
        node.setProcess(process);
        return (T) node;
    }

    // ================================================================================================== EventValidator

    private interface EventValidator {
        public boolean validate(TEventDefinition tEventDefinition);
    }

    // ============================================================================================ createCatchEventNode

    /**
     * Creates an instance of Catch Event Node
     * <p/>
     * Instantiates annotated class. If no annotated class found, instantiates CatchEventNode object. Does not persist
     * the node.
     *
     * @param tNode
     * @param process
     * @param validator
     * @return CatchEventNode
     */
    private CatchEventNode createCatchEventNode(TCatchEvent tNode, BusinessProcess process, EventValidator validator) {
        CatchEventNode node = instantiateNode(tNode, CatchEventNode.class, process);
        /*
         * Setup triggers
         */
        for (JAXBElement<? extends TEventDefinition> element : tNode.getEventDefinition()) {
            TEventDefinition eventDefinition = element.getValue();
            if (validator != null && !validator.validate(eventDefinition)) {
                continue;
            }
            try {
                EventTrigger trigger = createEventTrigger(eventDefinition);
                node.getTriggers().add(trigger);
                trigger.setEventNode(node);
            } catch (Exception e) {
                throw new IllegalStateException("Failed to create event trigger for catch event node: id=\"" +
                        tNode.getId() + "\", name=\"" + tNode.getName() + "\", " + e);
            }
        }
        return node;
    }

    // ========================================================================================= createEventBasedGateway

    /**
     * Creates an instance of Event Based Gateway
     * <p/>
     * Instantiates annotated class. If no annotated class found, instantiates EventBasedGateway object. Does not persist
     * the node.
     *
     * @param tNode
     * @param process
     * @return EventBasedGateway
     */
    private EventBasedGateway createEventBasedGateway(TEventBasedGateway tNode, BusinessProcess process) {
        EventBasedGateway node = instantiateNode(tNode, EventBasedGateway.class, process);
        /*
         * Setup targets
         */
        TProcess tProcess = getProcessDefinition(process.getDefinitionId());
        final boolean[] hasMessageEvents = {false};
        boolean hasReceiveTasks = false;
        EventValidator validator = new EventValidator() {
            @Override
            public boolean validate(TEventDefinition e) {
                if (e instanceof TErrorEventDefinition ||
                        e instanceof TCancelEventDefinition ||
                        e instanceof TCompensateEventDefinition ||
                        e instanceof TLinkEventDefinition) {
                    throw new IllegalStateException("Event based gateway may not have target events of this type: " + e.getClass().getName());
                }
                if (e instanceof TMessageEventDefinition) {
                    hasMessageEvents[0] = true;
                }
                return true;
            }
        };
        for (TFlowNode tFlowNode : BPMNUtils.findNext(tNode, tProcess)) {
            if (tFlowNode instanceof TReceiveTask) {
                hasReceiveTasks = true;
                // TODO: create createReceiveTask()
            } else if (tFlowNode instanceof TIntermediateCatchEvent) {
                CatchEventNode catchEventNode = createCatchEventNode((TCatchEvent) tFlowNode, process, validator);
                node.getTargets().add(catchEventNode);
            }
        }
        if (hasMessageEvents[0] && hasReceiveTasks) {
            throw new IllegalStateException("Event based gateway may not have both Receive Tasks and Message Events target nodes, gateway id:" + tNode.getId());
        }
        return node;
    }

    // ============================================================================================ createThrowEventNode

    /**
     * Creates an instance of Throw Event Node
     * <p/>
     * Instantiates annotated class. If no annotated class found, instantiates ThrowEventNode object. Does not persist
     * the node.
     *
     * @param tNode
     * @param process
     * @return ThrowEventNode
     */
    private ThrowEventNode createThrowEventNode(TThrowEvent tNode, BusinessProcess process) {
        ThrowEventNode node = instantiateNode(tNode, ThrowEventNode.class, process);
        /*
         * Process event definitions
         */
        for (JAXBElement<? extends TEventDefinition> element : tNode.getEventDefinition()) {
            TEventDefinition eventDefinition = element.getValue();
            node.getEvents().add(eventDefinition);
        }
        return node;
    }
    // ============================================================================================== createEventTrigger

    private EventTrigger createEventTrigger(TEventDefinition eventDefinition) {
        if (eventDefinition instanceof TMessageEventDefinition) {
            return createMessageEventTrigger((TMessageEventDefinition) eventDefinition);
        } else if (eventDefinition instanceof TTimerEventDefinition) {
            TimerTrigger trigger = createTimerTrigger((TTimerEventDefinition) eventDefinition);
            trigger.arm();
            return trigger;
        } else {
            throw new IllegalStateException("Unknown event definition type " + eventDefinition.getClass().getName());
        }
    }

    // ============================================================================================== createTimerTrigger

    private TimerTrigger createTimerTrigger(TTimerEventDefinition eventDefinition) {
        TimerTrigger trigger = new TimerTrigger();
        trigger.setDefinitionId(eventDefinition.getId());
        TExpression timeExpression;
        if (eventDefinition.getTimeCycle() != null) {
            timeExpression = eventDefinition.getTimeCycle();
            trigger.setTriggerType(TimerTriggerType.Cycle);
        } else if (eventDefinition.getTimeDate() != null) {
            timeExpression = eventDefinition.getTimeDate();
            trigger.setTriggerType(TimerTriggerType.Date);
        } else if (eventDefinition.getTimeDuration() != null) {
            timeExpression = eventDefinition.getTimeDuration();
            trigger.setTriggerType(TimerTriggerType.Duration);
        } else {
            throw new IllegalStateException("Invalid timer event definition, " + eventDefinition.getId() + ", must have one of timeCycle, timeDate, timeDuration");
        }
        if (timeExpression == null || timeExpression.getContent().isEmpty()) {
            throw new IllegalStateException("Empty " + trigger.getTriggerType().toString() + "element in timer event definition " + eventDefinition.getId());
        }
        trigger.setExpression(timeExpression.getContent().get(0).toString());
        return trigger;
    }

    // ======================================================================================= createMessageEventTrigger

    private MessageTrigger createMessageEventTrigger(TMessageEventDefinition eventDefinition) {
        MessageTrigger trigger = new MessageTrigger();
        String messageId = eventDefinition.getMessageRef().getLocalPart();
        if (messageId == null || messageId.isEmpty()) {
            throw new IllegalStateException("Missing or empty message id in message event definition \"" + eventDefinition.getId() + "\"");
        }
        if (!getMessageDefinitions().containsKey(messageId)) {
            throw new IllegalStateException("Message is not defined, id=\"" + messageId + "\"");
        }
        MessageDefinition md = getMessageDefinitions().get(messageId);
        trigger.setDefinitionId(md.getId());
        trigger.setMessageName(md.getName());
        return trigger;
    }

    private Map<String, ProcessDefinition> getProcessDefinitions() {
        if (processDefinitions == null) {
            processDefinitions = new HashMap<String, ProcessDefinition>();
        }
        return processDefinitions;
    }

    public Map<String, MessageDefinition> getMessageDefinitions() {
        if (messageDefinitions == null) {
            messageDefinitions = new HashMap<String, MessageDefinition>();
        }
        return messageDefinitions;
    }

    // ================================================================================================= EachNodeHandler

    protected interface EachNodeHandler {
        public boolean node(TFlowNode tNode);
    }

    // ===================================================================================================== forEachNode

    protected static void forEachNode(TProcess process, EachNodeHandler handler) {
        for (JAXBElement<? extends TFlowElement> element : process.getFlowElement()) {
            if (element.getValue() instanceof TFlowNode) {
                if (!handler.node((TFlowNode) element.getValue())) {
                    break;
                }
            }
        }
    }

    // ================================================================================================ forEachStartNode

    public static void forEachStartNode(TProcess process, EachNodeHandler handler) {
        for (JAXBElement<? extends TFlowElement> element : process.getFlowElement()) {
            if (element.getValue() instanceof TFlowNode) {
                TFlowNode tFlowNode = (TFlowNode) element.getValue();
                if (tFlowNode.getIncoming().isEmpty() && !((tFlowNode instanceof TTask) && ((TTask) tFlowNode).isIsForCompensation())) {
                    if (tFlowNode instanceof TCatchEvent || tFlowNode instanceof TTask || tFlowNode instanceof TGateway) {
                        handler.node(tFlowNode);
                    }
                }
            }
        }
    }

    // ============================================================================================== EachProcessHandler

    protected interface EachProcessHandler {
        public void process(TProcess tProcess);
    }

    // ================================================================================================== forEachProcess

    protected static void forEachProcess(TDefinitions tDefinitions, EachProcessHandler handler) {
        for (JAXBElement<? extends TRootElement> element : tDefinitions.getRootElement()) {
            if (element.getValue() instanceof TProcess) {
                handler.process((TProcess) element.getValue());
            }
        }
    }

    // ============================================================================================== EachMessageHandler

    protected interface EachMessageHandler {
        public void message(TMessage tProcess);
    }

    // ================================================================================================== forEachMessage

    protected static void forEachMessage(TDefinitions tDefinitions, EachMessageHandler handler) {
        for (JAXBElement<? extends TRootElement> element : tDefinitions.getRootElement()) {
            if (element.getValue() instanceof TMessage) {
                handler.message((TMessage) element.getValue());
            }
        }
    }


    // ================================================================================================ scanForProcesses

    private void scanForProcesses(final TDefinitions definitions) {

        forEachProcess(definitions, new EachProcessHandler() {

            public void process(TProcess tProcess) {
                String id = tProcess.getId();
                if (id == null || id.isEmpty()) {
                    throw new IllegalArgumentException("Process with with no or empty name attribute");
                }
                if (getProcessDefinitions().containsKey(id)) {
                    throw new IllegalArgumentException("Duplicate process id: " + id);
                }
                ProcessDefinition processDefinition = new ProcessDefinition(tProcess);
                processDefinition.setDefinitionsId(definitions.getId());
                getProcessDefinitions().put(id, processDefinition);
            }
        });
    }

    // ================================================================================================= scanForMessages

    private void scanForMessages(final TDefinitions definitions) {

        forEachMessage(definitions, new EachMessageHandler() {

            public void message(TMessage tMessage) {
                String id = tMessage.getId();
                if (id == null || id.isEmpty()) {
                    throw new IllegalArgumentException("Message with with no or empty name attribute");
                }
                if (getMessageDefinitions().containsKey(id)) {
                    throw new IllegalArgumentException("Duplicate message id: " + id);
                }
                MessageDefinition messageDefinition = new MessageDefinition(tMessage);
                getMessageDefinitions().put(id, messageDefinition);
            }
        });
    }

    // =============================================================================================== getDefinitionsMap

    private Map<String, TDefinitions> getDefinitionsMap() {
        if (definitionsMap == null) {
            definitionsMap = new HashMap<String, TDefinitions>();
        }
        return definitionsMap;
    }

    protected List<Node> findNodes(BusinessProcess process) {
        acquireEntityManager();
        List<Node> nodes = em.get().createNamedQuery("Node.findByProcess").setParameter("process", process).getResultList();
        releaseEntityManager();
        return nodes;
    }

    @Override
    public TProcess getProcessDefinition(String id) {
        if (getProcessDefinitions().containsKey(id)) {
            return getProcessDefinitions().get(id).getProcessDefinition();
        }
        return null;
    }

    public static long getTriggerPollCycle() {
        return TriggerPollCycle;
    }

    public static void setTriggerPollCycle(long triggerPollCycle) {
        TriggerPollCycle = triggerPollCycle;
    }

    /*
    ===================================== Persistence related methods ==================================================

    To avoid interference with application transactions, all persistence operations are executed outside of application
    Thread - either in queue runner or in a dedicated thread.

     */
    // ========================================================================================================= persist

    private void persist(final Object o) {
        if (isRunner()) {
            em.get().persist(o);
        } else {
            final LinkedBlockingQueue<String> kicker = new LinkedBlockingQueue<String>();
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    acquireEntityManager();
                    beginTransaction();
                    em.get().persist(o);
                    commitTransaction();
                    releaseEntityManager();
                    kicker.add("done");
                }
            });
            try {
                String result = null;
                int retries = 40;
                while (result == null && retries > 0) {
                    --retries;
                    result = kicker.poll(500, TimeUnit.MILLISECONDS);
                }
                /*
                TODO: and what now? no way to stop the Runnable. Rising exception here?
                 */
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    // =========================================================================================================== merge

    private Object merge(final Object o) {
        if (isRunner()) {
            return em.get().merge(o);
        } else {
            final LinkedBlockingQueue<Object> kicker = new LinkedBlockingQueue<Object>();
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    acquireEntityManager();
                    beginTransaction();
                    Object mergedEntity = em.get().merge(o);
                    commitTransaction();
                    releaseEntityManager();
                    kicker.add(mergedEntity);
                }
            });
            try {
                Object entity = null;
                int retries = 40;
                while (entity == null && retries > 0) {
                    --retries;
                    entity = kicker.poll(500, TimeUnit.MILLISECONDS);
                }
                /*
                TODO: and what now? no way to stop the Runnable. Rising exception here?
                 */
                return entity;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        return null;
    }

    // ==================================================================================================== getUserTasks

    private List<UserTask> getUserTasks(EntityManager em) {
        return em.createNamedQuery("UserTask.findAll").getResultList();
    }

    // ==================================================================================================== getUserTasks

    public List<UserTask> getUserTasks() {
        if (isRunner()) {
            return getUserTasks(em.get());
        } else {
            final LinkedBlockingQueue<List<UserTask>> kicker = new LinkedBlockingQueue<List<UserTask>>();
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    acquireEntityManager();
                    List<UserTask> tasks = getUserTasks(em.get());
                    releaseEntityManager();
                    kicker.add(tasks);
                }
            });
            try {
                List<UserTask> tasks = null;
                int retries = 40;
                while (tasks == null && retries > 0) {
                    --retries;
                    tasks = kicker.poll(500, TimeUnit.MILLISECONDS);
                }
                /*
                TODO: log error
                 */
                return tasks;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        return Collections.emptyList();
    }

    // ============================================================================================ acquireEntityManager

    protected EntityManager acquireEntityManager() {
        if (em.get() == null) {
            em.set(emf.createEntityManager());
            acquireCounter.set(1);
        } else {
            acquireCounter.set(acquireCounter.get() + 1);
        }
        return em.get();
    }

    // ============================================================================================ releaseEntityManager

    protected void releaseEntityManager() {
        if (acquireCounter.get() <= 0) {
            throw new IllegalStateException("Mismatched call to releaseEntityManager()");
        }
        acquireCounter.set(acquireCounter.get() - 1);
        if (acquireCounter.get() == 0) {
            em.remove();
        }
    }

    // ================================================================================================ beginTransaction

    protected void beginTransaction() {
        em.get().getTransaction().begin();
    }

    // =============================================================================================== commitTransaction

    protected void commitTransaction() {
        EntityTransaction transaction = em.get().getTransaction();
        if (transaction.getRollbackOnly()) {
            transaction.rollback();
        } else {
            transaction.commit();
        }
    }

    // ========================================================================================= markRollbackTransaction

    protected void markRollbackTransaction() {
        em.get().getTransaction().setRollbackOnly();
    }

    // ========================================================================================================= getNode

    private Node getNode(long id) {
        String className = (String) em.get().createNamedQuery("Node.className").setParameter("id", id).getSingleResult();
        if (className != null && !className.isEmpty()) {
            try {
                Class clazz = Class.forName(className);
                @SuppressWarnings({"unchecked"})
                Object entity = em.get().find(clazz, id);
                if (entity == null) {
                    throw new IllegalStateException("Node id:" + id + " is not an instance of " + className);
                }
                if (entity instanceof Node) {
                    return (Node) entity;
                }
                throw new IllegalStateException("Object of class " + className + " with id:" + id + " is not a subclass of " + Node.class.getName());
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                throw new IllegalStateException("Can't instantiate Class " + className);
            }
        } else {
            throw new IllegalStateException("No class name for node or node id:" + id + " not found");
        }
    }
}
