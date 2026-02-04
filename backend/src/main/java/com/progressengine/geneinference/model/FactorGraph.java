package com.progressengine.geneinference.model;

import com.progressengine.geneinference.model.enums.Category;
import com.progressengine.geneinference.model.enums.DistributionType;
import com.progressengine.geneinference.model.enums.Grade;
import com.progressengine.geneinference.service.InferenceMath;
import com.progressengine.geneinference.service.SheepService;
import jakarta.transaction.Transactional;

import java.util.*;

public class FactorGraph {

    private final int MAX_ITERATIONS = 400;
    private final double epsilon = 0.01;

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

        Map<Relationship, Node<Relationship>> relationshipToNode = new HashMap<>();
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

    public Map<Category, Map<Grade, Double>> computeMessage(Message message) {
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
            //System.out.println("Size of queue: " + frontier.size()); // remove after testing
            Message message = frontier.poll();
            Map<Category, Map<Grade, Double>> newMessage = computeMessage(message);

            if (!reachedConvergence(message, newMessage)) {
                message.setDistribution(newMessage);
                frontier.addAll(dependentsOf(message));
                iterations++;
            }
        }

        // TODO - remove this after initial testing
        //System.out.println("Iterations needed: " + iterations + " of " + MAX_ITERATIONS);
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

    private Map<Category, Map<Grade, Double>> newBelief() {
        Map<Category, Map<Grade, Double>> result = new EnumMap<>(Category.class);
        for (Category category : Category.values()) {
            result.put(category, SheepService.createUniformDistribution());
        }
        return result;
    }
}
