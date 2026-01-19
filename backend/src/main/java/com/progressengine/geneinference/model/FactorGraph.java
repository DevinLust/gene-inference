package com.progressengine.geneinference.model;

import com.progressengine.geneinference.model.enums.Category;
import com.progressengine.geneinference.model.enums.DistributionType;
import com.progressengine.geneinference.model.enums.Grade;

import java.util.*;

public class FactorGraph {

    private final int MAX_ITERATIONS = 100;
    private final double epsilon = 0.05;

    private final Map<Node<?>, List<Node<?>>> adjacencyMatrix;
    private final Map<NodePair, Message> messageMap;

    public FactorGraph(List<Sheep> allSheep, List<Relationship> allRelationships) {
        adjacencyMatrix = new HashMap<>();
        messageMap = new HashMap<>();

        Map<Sheep, Node<Sheep>> sheepToNode = new HashMap<>();
        for (Sheep sheep : allSheep) {
            Node<Sheep> sheepNode = new SheepNode(sheep);
            adjacencyMatrix.put(sheepNode, new ArrayList<>());
            sheepToNode.put(sheep, sheepNode);
        }

        for (Relationship relationship : allRelationships) {
            Node<Relationship> relationshipNode = new RelationshipNode(relationship);
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

    public Map<Grade, Double> computeMessage(Message message) {
        List<Message> operands = new ArrayList<>();
        Node<?> source = message.getSource();
        Node<?> target = message.getTarget();

        List<Node<?>> sourceNeighbors = adjacencyMatrix.get(source);
        for (Node<?> neighbor : sourceNeighbors) {
            if (neighbor.equals(target)) continue;

            NodePair nodePair = new NodePair(neighbor, source);
            operands.add(messageMap.get(nodePair));
        }

        return message.computeMessage(operands);
    }

    public void recalculateAllMessages() {
        Queue<Message> frontier = new ArrayDeque<>();
        for (Message message : messageMap.values()) {
            if (message instanceof RelationshipMessage) {
                frontier.add(message);
            }
        }

        int iterations = 0;
        while (!frontier.isEmpty() && iterations < MAX_ITERATIONS) {
            Message message = frontier.poll();
            Map<Grade, Double> newMessage = computeMessage(message);

            if (!reachedConvergence(message, newMessage)) {
                message.setDistribution(newMessage);
                frontier.addAll(dependentsOf(message));
            }
            iterations++;
        }

        // TODO - remove this after initial testing
        System.out.println("Iterations needed: " + iterations + " of " + MAX_ITERATIONS);
    }

    public List<Map<Grade, Double>> computeBeliefs() {
        List<Map<Grade, Double>> beliefs = new ArrayList<>();
        for (Node<?> node : adjacencyMatrix.keySet()) {
            if (node instanceof SheepNode) {
                Sheep sheep = ((SheepNode) node).getValue();
                Map<Grade, Double> belief = sheep.getDistribution(Category.SWIM, DistributionType.PRIOR);
                for (Node<?> neighbor : adjacencyMatrix.get(node)) {
                    NodePair nodePair = new NodePair(neighbor, node);
                    Message message = messageMap.get(nodePair);
                    productOfExperts(belief, message.getDistribution());
                }
                beliefs.add(belief);
            }
        }
        return beliefs;
    }

    private boolean reachedConvergence(Message message, Map<Grade, Double> newMessage) {
        Map<Grade, Double> oldMessage = message.getDistribution();
        double distance = 0.0;

        for (Map.Entry<Grade, Double> entry : oldMessage.entrySet()) {
            Grade key = entry.getKey();
            Double value = entry.getValue();
            distance += (value - newMessage.get(key)) * (value - newMessage.get(key));
        }
        return distance <= (epsilon * epsilon);
    }

    private void productOfExperts(Map<Grade, Double> existingDistribution, Map<Grade, Double> newDistribution) {
        for (Map.Entry<Grade, Double> entry : existingDistribution.entrySet()) {
            double newProbability = newDistribution.get(entry.getKey());
            entry.setValue(entry.getValue() * newProbability);
        }

        normalizeScores(existingDistribution);
    }

    private <T> void normalizeScores(Map<T, Double> scores) {
        double sum = scores.values().stream().mapToDouble(Double::doubleValue).sum();

        if (sum == 0) { return; }

        for (Map.Entry<T, Double> entry : scores.entrySet()) {
            entry.setValue(entry.getValue() / sum);
        }
    }
}
