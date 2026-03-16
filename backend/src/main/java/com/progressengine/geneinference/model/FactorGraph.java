package com.progressengine.geneinference.model;

import com.progressengine.geneinference.model.enums.Category;
import com.progressengine.geneinference.model.enums.DistributionType;
import com.progressengine.geneinference.model.enums.Grade;
import com.progressengine.geneinference.service.InferenceMath;
import com.progressengine.geneinference.service.SheepService;

import java.util.*;

public class FactorGraph {


    private final Map<Node<?>, List<Node<?>>> adjacencyMatrix;
    private final Map<NodePair, Message> messageMap;
    private final Map<Sheep, Node<Sheep>> sheepToNode;
    private final Map<Relationship, Node<Relationship>> relationshipToNode;

    public FactorGraph(List<Sheep> allSheep, List<Relationship> allRelationships) {
        adjacencyMatrix = new HashMap<>();
        messageMap = new HashMap<>();

        this.sheepToNode = new HashMap<>();
        for (Sheep sheep : allSheep) {
            Node<Sheep> sheepNode = new SheepNode(sheep);
            adjacencyMatrix.put(sheepNode, new ArrayList<>());
            sheepToNode.put(sheep, sheepNode);
        }

        this.relationshipToNode = new HashMap<>();
        for (Relationship relationship : allRelationships) {
            Node<Relationship> relationshipNode = new RelationshipNode(relationship);
            relationshipToNode.put(relationship, relationshipNode);
            adjacencyMatrix.put(relationshipNode, new ArrayList<>());

            Node<Sheep> parent1 = sheepToNode.get(relationship.getParent1());
            Node<Sheep> parent2 = sheepToNode.get(relationship.getParent2());

            adjacencyMatrix.get(relationshipNode).add(parent1);
            messageMap.put(new NodePair(relationshipNode, parent1), new RelationshipMessage(relationshipNode, parent1));

            adjacencyMatrix.get(relationshipNode).add(parent2);
            messageMap.put(new NodePair(relationshipNode, parent2), new RelationshipMessage(relationshipNode, parent2));

            adjacencyMatrix.get(parent1).add(relationshipNode);
            messageMap.put(new NodePair(parent1, relationshipNode), new SheepMessage(parent1, relationshipNode));

            adjacencyMatrix.get(parent2).add(relationshipNode);
            messageMap.put(new NodePair(parent2, relationshipNode), new SheepMessage(parent2, relationshipNode));
        }

        for (Sheep sheep : allSheep) {
            if (sheep.getParentRelationship() == null) continue;

            Node<Sheep> sheepNode = sheepToNode.get(sheep);
            Node<Relationship> relationshipNode = relationshipToNode.get(sheep.getParentRelationship());

            adjacencyMatrix.get(sheepNode).add(relationshipNode);
            messageMap.put(new NodePair(sheepNode, relationshipNode), new SheepMessage(sheepNode, relationshipNode));

            adjacencyMatrix.get(relationshipNode).add(sheepNode);
            messageMap.put(new NodePair(relationshipNode, sheepNode), new ChildMessage(relationshipNode, sheepNode));
        }
    }

    public VisualizationScope buildScope(Sheep targetSheep) {
        Node<Sheep> targetNode = sheepToNode.get(targetSheep);
        if (targetNode == null) {
            throw new IllegalArgumentException("Target sheep is not in factor graph");
        }

        Set<Node<?>> scopedNodes = new HashSet<>();
        Set<Sheep> scopedSheep = new HashSet<>();

        scopedNodes.add(targetNode);
        scopedSheep.add(targetSheep);

        scopedNodes.addAll(adjacencyMatrix.get(targetNode));

        return new VisualizationScope(targetSheep, scopedNodes, scopedSheep);
    }

    public List<MessageCategoryTask> initialFrontierTasks() {
        List<MessageCategoryTask> tasks = new ArrayList<>();

        for (Message message : messageMap.values()) {
            if (message instanceof RelationshipMessage) {
                for (Category category : Category.values()) {
                    tasks.add(new MessageCategoryTask(message, category));
                }
            }
        }

        return tasks;
    }


    public int estimatedMaxIterations() {
        return messageMap.size() * Category.values().length * 20;
    }


    public List<Message> dependentsOf(Message message) {
        List<Message> dependents = new ArrayList<>();
        Node<?> target = message.getTarget();
        Node<?> source = message.getSource();

        List<Node<?>> targetNeighbors = adjacencyMatrix.get(target);
        for (Node<?> neighbor : targetNeighbors) {
            if (neighbor.equals(source)) continue;

            NodePair nodePair = new NodePair(target, neighbor);
            dependents.add(messageMap.get(nodePair));
        }

        return dependents;
    }

    public List<MessageCategoryTask> dependentsOf(MessageCategoryTask messageCategoryTask) {
        return dependentsOf(messageCategoryTask.message(), messageCategoryTask.category());
    }

    public List<MessageCategoryTask> dependentsOf(Message message, Category category) {
        List<MessageCategoryTask> dependents = new ArrayList<>();
        Node<?> target = message.getTarget();
        Node<?> source = message.getSource();

        List<Node<?>> targetNeighbors = adjacencyMatrix.get(target);
        for (Node<?> neighbor : targetNeighbors) {
            if (neighbor.equals(source)) continue;

            NodePair nodePair = new NodePair(target, neighbor);
            Message dependent = messageMap.get(nodePair);
            dependents.add(new MessageCategoryTask(dependent, category));
        }

        return dependents;
    }

    public List<Message> operandsOf(Message message) {
        List<Message> operands = new ArrayList<>();
        Node<?> source = message.getSource();
        Node<?> target = message.getTarget();

        for (Node<?> neighbor : adjacencyMatrix.get(source)) {
            if (neighbor.equals(target)) continue;
            operands.add(messageMap.get(new NodePair(neighbor, source)));
        }

        return operands;
    }

    public Map<Category, Map<Grade, Double>> computeMessage(Message message) {
        return message.computeMessage(operandsOf(message));
    }

    public Map<Grade, Double> computeMessageForCategory(Message message, Category category) {
        return message.computeMessageForCategory(category, operandsOf(message));
    }

    public void recalculateAllMessages() {
        Queue<MessageCategoryTask> frontier = new ArrayDeque<>();
        Set<MessageCategoryTask> queued = new HashSet<>();

        for (Message message : messageMap.values()) {
            if (message instanceof RelationshipMessage) {
                for (Category category : Category.values()) {
                    MessageCategoryTask task = new MessageCategoryTask(message, category);
                    frontier.add(task);
                    queued.add(task);
                }
            }
        }

        int maxIterations = messageMap.size() * Category.values().length * 20;
        int iterations = 0;

        while (!frontier.isEmpty() && iterations < maxIterations) {
            MessageCategoryTask task = frontier.poll();
            queued.remove(task);

            Message message = task.message();
            Category category = task.category();

            Map<Grade, Double> newDistribution = computeMessageForCategory(message, category);

            if (!reachedConvergence(message, category, newDistribution)) {
                message.setDistributionForCategory(category, newDistribution);

                for (MessageCategoryTask dependent : dependentsOf(message, category)) {
                    if (queued.add(dependent)) {
                        frontier.add(dependent);
                    }
                }
                iterations++;
            }
        }
    }

    public List<Map<Category, Map<Grade, Double>>> computeBeliefs() {
        List<Map<Category, Map<Grade, Double>>> beliefs = new ArrayList<>();
        for (Node<?> node : adjacencyMatrix.keySet()) {
            if (node instanceof SheepNode) {
                Sheep sheep = ((SheepNode) node).getValue();
                // changed priors to start with uniform
                Map<Category, Map<Grade, Double>> belief = newBelief();
                for (Node<?> neighbor : adjacencyMatrix.get(node)) {
                    NodePair nodePair = new NodePair(neighbor, node);
                    Message message = messageMap.get(nodePair);
                    for (Category category : Category.values()) {
                        InferenceMath.productOfExperts(belief.get(category), message.getDistribution().get(category));
                    }
                }
                sheep.setDistributionByType(belief, DistributionType.INFERRED);
                beliefs.add(belief);
            }
        }
        return beliefs;
    }

    private boolean reachedConvergence(Message message, Map<Category, Map<Grade, Double>> newMessage) {
        double epsilon = 1e-3;

        Map<Category, Map<Grade, Double>> oldMessage = message.getDistribution();
        boolean converged = true;

        for (Map.Entry<Category, Map<Grade, Double>> dist : oldMessage.entrySet()) {
            Category category = dist.getKey();
            double distance = 0.0;
            for (Map.Entry<Grade, Double> entry : dist.getValue().entrySet()) {
                Grade key = entry.getKey();
                Double value = entry.getValue();
                distance += (value - newMessage.get(category).get(key)) * (value - newMessage.get(category).get(key));
            }
            converged = converged && distance <= (epsilon * epsilon);
        }

        return converged;
    }

    public boolean reachedConvergence(
            Message message,
            Category category,
            Map<Grade, Double> newDistribution
    ) {
        double epsilon = 1e-3;
        double threshold = epsilon * epsilon;

        Map<Grade, Double> oldDistribution = message.getDistribution().get(category);

        double distance = 0.0;
        for (Grade grade : Grade.values()) {
            double oldValue = oldDistribution.get(grade);
            double newValue = newDistribution.get(grade);
            double diff = oldValue - newValue;
            distance += diff * diff;
        }

        return distance <= threshold * threshold;
    }

    private Map<Category, Map<Grade, Double>> newBelief() {
        Map<Category, Map<Grade, Double>> result = new EnumMap<>(Category.class);
        for (Category category : Category.values()) {
            result.put(category, SheepService.createUniformDistribution());
        }
        return result;
    }
}
