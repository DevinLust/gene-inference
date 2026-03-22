package com.progressengine.geneinference.model;

import com.progressengine.geneinference.dto.VisualEdgeDTO;
import com.progressengine.geneinference.dto.VisualGraphSnapshot;
import com.progressengine.geneinference.dto.VisualNodeDTO;
import com.progressengine.geneinference.model.enums.Category;
import com.progressengine.geneinference.model.enums.DistributionType;
import com.progressengine.geneinference.model.enums.Grade;
import com.progressengine.geneinference.model.enums.RelationshipEdgeRole;
import com.progressengine.geneinference.service.InferenceMath;
import com.progressengine.geneinference.service.SheepService;

import java.util.*;

public class FactorGraph {

    private final Map<Node<?>, List<Node<?>>> adjacencyMatrix;
    private final Map<NodePair, Message> messageMap;
    private final Map<NodePair, RelationshipEdgeRole> relationshipEdgeRoles;
    private final Map<Sheep, Node<Sheep>> sheepToNode;
    private final Map<Relationship, Node<Relationship>> relationshipToNode;

    public FactorGraph(List<Sheep> allSheep, List<Relationship> allRelationships) {
        adjacencyMatrix = new HashMap<>();
        messageMap = new HashMap<>();
        relationshipEdgeRoles = new HashMap<>();

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

            if (parent1 != null) {
                adjacencyMatrix.get(relationshipNode).add(parent1);
                messageMap.put(new NodePair(relationshipNode, parent1), new RelationshipMessage(relationshipNode, parent1));
                relationshipEdgeRoles.put(new NodePair(relationshipNode, parent1), RelationshipEdgeRole.PARENT);

                adjacencyMatrix.get(parent1).add(relationshipNode);
                messageMap.put(new NodePair(parent1, relationshipNode), new SheepMessage(parent1, relationshipNode));
                relationshipEdgeRoles.put(new NodePair(parent1, relationshipNode), RelationshipEdgeRole.PARENT);
            }

            if (parent2 != null) {
                adjacencyMatrix.get(relationshipNode).add(parent2);
                messageMap.put(new NodePair(relationshipNode, parent2), new RelationshipMessage(relationshipNode, parent2));
                relationshipEdgeRoles.put(new NodePair(relationshipNode, parent2), RelationshipEdgeRole.PARENT);

                adjacencyMatrix.get(parent2).add(relationshipNode);
                messageMap.put(new NodePair(parent2, relationshipNode), new SheepMessage(parent2, relationshipNode));
                relationshipEdgeRoles.put(new NodePair(parent2, relationshipNode), RelationshipEdgeRole.PARENT);
            }
        }

        for (Sheep sheep : allSheep) {
            if (sheep.getParentRelationship() == null) continue;

            Node<Sheep> sheepNode = sheepToNode.get(sheep);
            Node<Relationship> relationshipNode = relationshipToNode.get(sheep.getParentRelationship());

            if (sheepNode == null || relationshipNode == null) {
                continue;
            }

            adjacencyMatrix.get(sheepNode).add(relationshipNode);
            messageMap.put(new NodePair(sheepNode, relationshipNode), new SheepMessage(sheepNode, relationshipNode));
            relationshipEdgeRoles.put(new NodePair(sheepNode, relationshipNode), RelationshipEdgeRole.CHILD);

            adjacencyMatrix.get(relationshipNode).add(sheepNode);
            messageMap.put(new NodePair(relationshipNode, sheepNode), new ChildMessage(relationshipNode, sheepNode));
            relationshipEdgeRoles.put(new NodePair(relationshipNode, sheepNode), RelationshipEdgeRole.CHILD);
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

        List<Node<?>> relationshipNeighbors = adjacencyMatrix.getOrDefault(targetNode, List.of());
        for (Node<?> relationshipNode : relationshipNeighbors) {
            scopedNodes.add(relationshipNode);

            for (Node<?> relativeNode : adjacencyMatrix.getOrDefault(relationshipNode, List.of())) {
                scopedNodes.add(relativeNode);

                if (relativeNode instanceof SheepNode sheepNode) {
                    scopedSheep.add(sheepNode.getValue());
                }
            }
        }

        return new VisualizationScope(targetSheep, scopedNodes, scopedSheep);
    }

    public VisualGraphSnapshot buildVisualGraph(VisualizationScope scope) {
        List<VisualNodeDTO> nodes = new ArrayList<>();
        List<VisualEdgeDTO> edges = new ArrayList<>();

        Node<Sheep> centerNode = sheepToNode.get(scope.getCenterSheep());
        if (centerNode == null) {
            throw new IllegalArgumentException("Center sheep is not in graph");
        }

        String centerNodeId = nodeId(centerNode);

        Set<Node<?>> displayedNodes = new HashSet<>(scope.getScopedNodes());
        Map<Node<?>, double[]> positions = new HashMap<>();
        Set<String> addedNodeIds = new HashSet<>();
        Set<String> addedEdgeIds = new HashSet<>();

        positions.put(centerNode, new double[]{0.0, 0.0});
        nodes.add(new VisualNodeDTO(
                centerNodeId,
                "sheep",
                "Sheep " + scope.getCenterSheep().getId(),
                0.0,
                0.0,
                true
        ));
        addedNodeIds.add(centerNodeId);

        List<RelationshipNode> relationshipNodes = adjacencyMatrix.getOrDefault(centerNode, List.of())
                .stream()
                .filter(n -> n instanceof RelationshipNode)
                .map(n -> (RelationshipNode) n)
                .toList();

        double relationshipRadius = 240.0;
        Map<RelationshipNode, Double> relationshipAngles = new HashMap<>();

        for (int i = 0; i < relationshipNodes.size(); i++) {
            RelationshipNode relationshipNode = relationshipNodes.get(i);

            double angle = (2 * Math.PI * i) / Math.max(1, relationshipNodes.size());
            double x = relationshipRadius * Math.cos(angle);
            double y = relationshipRadius * Math.sin(angle);

            relationshipAngles.put(relationshipNode, angle);
            positions.put(relationshipNode, new double[]{x, y});

            String relationshipNodeId = nodeId(relationshipNode);
            if (addedNodeIds.add(relationshipNodeId)) {
                nodes.add(new VisualNodeDTO(
                        relationshipNodeId,
                        "relationship",
                        "Relationship " + relationshipNode.getValue().getId(),
                        x,
                        y,
                        false
                ));
            }

            String edgeId = structuralEdgeId(centerNode, relationshipNode);
            if (addedEdgeIds.add(edgeId)) {
                edges.add(new VisualEdgeDTO(
                        edgeId,
                        centerNodeId,
                        relationshipNodeId,
                        "full",
                        true,
                        relationshipRole(relationshipNode, (SheepNode) centerNode),
                        null,
                        null,
                        null
                ));
            }
        }

        double sheepRadius = 460.0;

        for (int i = 0; i < relationshipNodes.size(); i++) {
            RelationshipNode relationshipNode = relationshipNodes.get(i);
            String relationshipNodeId = nodeId(relationshipNode);

            double centerAngle = relationshipAngles.get(relationshipNode);

            double maxSpreadFromNeighbors;

            if (relationshipNodes.size() == 1) {
                maxSpreadFromNeighbors = Math.PI;
            } else {
                double prevAngle = relationshipAngles.get(
                        relationshipNodes.get((i - 1 + relationshipNodes.size()) % relationshipNodes.size())
                );
                double nextAngle = relationshipAngles.get(
                        relationshipNodes.get((i + 1) % relationshipNodes.size())
                );

                double leftGap = angularDistance(centerAngle, prevAngle);
                double rightGap = angularDistance(nextAngle, centerAngle);
                double minNeighborGap = Math.min(leftGap, rightGap);

                maxSpreadFromNeighbors = Math.max(0.0, minNeighborGap * 0.82);
            }

            List<SheepNode> visibleSheepNeighbors = new ArrayList<>();
            List<Node<?>> hiddenNeighbors = new ArrayList<>();

            for (Node<?> relative : adjacencyMatrix.getOrDefault(relationshipNode, List.of())) {
                if (relative.equals(centerNode)) {
                    continue;
                }

                if (displayedNodes.contains(relative) && relative instanceof SheepNode sheepNode) {
                    visibleSheepNeighbors.add(sheepNode);
                } else if (!displayedNodes.contains(relative)) {
                    hiddenNeighbors.add(relative);
                }
            }

            double desiredSpread = relationshipSectorSpread(visibleSheepNeighbors.size());
            double actualSpread = Math.min(desiredSpread, maxSpreadFromNeighbors);

            List<Double> sheepAngles = evenlySpacedAngles(centerAngle, actualSpread, visibleSheepNeighbors.size());

            for (int j = 0; j < visibleSheepNeighbors.size(); j++) {
                SheepNode sheepNode = visibleSheepNeighbors.get(j);
                double sheepAngle = sheepAngles.get(j);

                double sheepX = sheepRadius * Math.cos(sheepAngle);
                double sheepY = sheepRadius * Math.sin(sheepAngle);

                positions.put(sheepNode, new double[]{sheepX, sheepY});

                String sheepNodeId = nodeId(sheepNode);
                if (addedNodeIds.add(sheepNodeId)) {
                    nodes.add(new VisualNodeDTO(
                            sheepNodeId,
                            "sheep",
                            "Sheep " + sheepNode.getValue().getId(),
                            sheepX,
                            sheepY,
                            false
                    ));
                }

                String edgeId = structuralEdgeId(relationshipNode, sheepNode);
                if (addedEdgeIds.add(edgeId)) {
                    edges.add(new VisualEdgeDTO(
                            edgeId,
                            relationshipNodeId,
                            sheepNodeId,
                            "full",
                            true,
                            relationshipRole(relationshipNode, sheepNode),
                            null,
                            j,
                            visibleSheepNeighbors.size()
                    ));
                }
            }

            double stubSpread = Math.min(
                    relationshipSectorSpread(hiddenNeighbors.size()),
                    maxSpreadFromNeighbors
            );
            List<Double> stubAngles = evenlySpacedAngles(centerAngle, stubSpread, hiddenNeighbors.size());

            for (int j = 0; j < hiddenNeighbors.size(); j++) {
                Node<?> hiddenNeighbor = hiddenNeighbors.get(j);
                String hiddenNeighborId = nodeId(hiddenNeighbor);
                double stubAngle = stubAngles.get(j);

                RelationshipEdgeRole role = null;
                if (hiddenNeighbor instanceof SheepNode hiddenSheepNode) {
                    role = relationshipRole(relationshipNode, hiddenSheepNode);
                }

                String edgeId = relationshipNodeId + "--" + hiddenNeighborId;
                if (addedEdgeIds.add(edgeId)) {
                    edges.add(new VisualEdgeDTO(
                            edgeId,
                            relationshipNodeId,
                            hiddenNeighborId,
                            "stub",
                            false,
                            role,
                            stubAngle,
                            j,
                            hiddenNeighbors.size()
                    ));
                }
            }
        }

        for (Node<?> displayedNode : displayedNodes) {
            if (!(displayedNode instanceof SheepNode sheepNode)) {
                continue;
            }
            if (displayedNode.equals(centerNode)) {
                continue;
            }

            String displayedNodeId = nodeId(sheepNode);
            double[] sourcePos = positions.get(sheepNode);
            if (sourcePos == null) {
                continue;
            }

            List<Node<?>> hiddenNeighbors = new ArrayList<>();
            for (Node<?> neighbor : adjacencyMatrix.getOrDefault(displayedNode, List.of())) {
                if (!displayedNodes.contains(neighbor)) {
                    hiddenNeighbors.add(neighbor);
                }
            }

            if (hiddenNeighbors.isEmpty()) {
                continue;
            }

            double baseAngle = outwardAngle(sourcePos[0], sourcePos[1]);
            double spread = Math.min(
                    relationshipSectorSpread(hiddenNeighbors.size()),
                    Math.PI
            );
            List<Double> stubAngles = evenlySpacedAngles(baseAngle, spread, hiddenNeighbors.size());

            for (int j = 0; j < hiddenNeighbors.size(); j++) {
                Node<?> hiddenNeighbor = hiddenNeighbors.get(j);
                String hiddenNeighborId = nodeId(hiddenNeighbor);
                double stubAngle = stubAngles.get(j);

                RelationshipEdgeRole role = null;
                if (hiddenNeighbor instanceof RelationshipNode hiddenRelationshipNode) {
                    role = relationshipRole(hiddenRelationshipNode, sheepNode);
                }

                String edgeId = displayedNodeId + "--" + hiddenNeighborId;
                if (addedEdgeIds.add(edgeId)) {
                    edges.add(new VisualEdgeDTO(
                            edgeId,
                            displayedNodeId,
                            hiddenNeighborId,
                            "stub",
                            false,
                            role,
                            stubAngle,
                            j,
                            hiddenNeighbors.size()
                    ));
                }
            }
        }

        return new VisualGraphSnapshot(centerNodeId, nodes, edges);
    }

    private double normalizeAngle(double angle) {
        while (angle <= -Math.PI) angle += 2 * Math.PI;
        while (angle > Math.PI) angle -= 2 * Math.PI;
        return angle;
    }

    private double angularDistance(double a, double b) {
        return Math.abs(normalizeAngle(a - b));
    }

    private double relationshipSectorSpread(int relativeCount) {
        if (relativeCount <= 1) {
            return Math.toRadians(55);
        }

        double spread = Math.toRadians(55 + 22 * (relativeCount - 1));
        return Math.min(spread, Math.PI);
    }

    private List<Double> evenlySpacedAngles(double centerAngle, double spread, int count) {
        List<Double> angles = new ArrayList<>();
        if (count <= 0) {
            return angles;
        }

        if (count == 1) {
            angles.add(centerAngle);
            return angles;
        }

        double start = centerAngle - spread / 2.0;
        double step = spread / (count - 1);

        for (int i = 0; i < count; i++) {
            angles.add(start + i * step);
        }

        return angles;
    }

    private double nodeX(Node<?> node, Node<Sheep> centerNode, Map<Node<?>, double[]> positions) {
        if (node.equals(centerNode)) return 0.0;
        double[] pos = positions.get(node);
        return pos == null ? 0.0 : pos[0];
    }

    private double nodeY(Node<?> node, Node<Sheep> centerNode, Map<Node<?>, double[]> positions) {
        if (node.equals(centerNode)) return 0.0;
        double[] pos = positions.get(node);
        return pos == null ? 0.0 : pos[1];
    }

    private String structuralEdgeId(Node<?> a, Node<?> b) {
        String aId = nodeId(a);
        String bId = nodeId(b);

        if (aId.compareTo(bId) < 0) {
            return aId + "--" + bId;
        }
        return bId + "--" + aId;
    }

    public String visualEdgeIdForMessage(Message message, VisualizationScope scope) {
        Node<?> source = message.getSource();
        Node<?> target = message.getTarget();

        boolean sourceDisplayed = scope.getScopedNodes().contains(source);
        boolean targetDisplayed = scope.getScopedNodes().contains(target);

        if (sourceDisplayed && targetDisplayed) {
            return structuralEdgeId(source, target);
        }

        if (sourceDisplayed && !targetDisplayed) {
            return nodeId(source) + "--" + nodeId(target);
        }

        if (!sourceDisplayed && targetDisplayed) {
            return nodeId(target) + "--" + nodeId(source);
        }

        return null;
    }

    private double outwardAngle(double x, double y) {
        return Math.atan2(y, x);
    }

    private double fanoutAngle(double baseAngle, int index, int count) {
        if (count <= 1) {
            return baseAngle;
        }

        double spread = Math.PI / 3.0;
        double step = spread / (count - 1);
        double start = baseAngle - spread / 2.0;

        return start + index * step;
    }

    private String nodeId(Node<?> node) {
        if (node instanceof SheepNode sheepNode) {
            return "sheep-" + sheepNode.getValue().getId();
        }
        if (node instanceof RelationshipNode relationshipNode) {
            return "relationship-" + relationshipNode.getValue().getId();
        }
        throw new IllegalStateException("Unknown node type");
    }

    private RelationshipEdgeRole relationshipRole(RelationshipNode relationshipNode, SheepNode sheepNode) {
        RelationshipEdgeRole role = relationshipEdgeRoles.get(new NodePair(relationshipNode, sheepNode));
        if (role != null) {
            return role;
        }

        return relationshipEdgeRoles.get(new NodePair(sheepNode, relationshipNode));
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

    public Map<Category, Map<Grade, Double>> computeBeliefForSheep(Sheep sheep) {
        Node<Sheep> node = sheepToNode.get(sheep);
        if (node == null) {
            throw new IllegalArgumentException("Sheep is not in factor graph");
        }

        Map<Category, Map<Grade, Double>> belief = newBelief();

        for (Node<?> neighbor : adjacencyMatrix.get(node)) {
            NodePair nodePair = new NodePair(neighbor, node);
            Message message = messageMap.get(nodePair);

            for (Category category : Category.values()) {
                InferenceMath.productOfExperts(
                        belief.get(category),
                        message.getDistribution().get(category)
                );
            }
        }

        sheep.setDistributionByType(belief, DistributionType.INFERRED);
        return belief;
    }

    public List<Message> incomingMessagesForSheep(Sheep sheep) {
        Node<Sheep> sheepNode = sheepToNode.get(sheep);
        if (sheepNode == null) {
            throw new IllegalArgumentException("Sheep is not in factor graph");
        }

        List<Message> incoming = new ArrayList<>();

        for (Node<?> neighbor : adjacencyMatrix.getOrDefault(sheepNode, List.of())) {
            Message message = messageMap.get(new NodePair(neighbor, sheepNode));
            if (message != null) {
                incoming.add(message);
            }
        }

        return incoming;
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

        return distance <= threshold;
    }

    private Map<Category, Map<Grade, Double>> newBelief() {
        Map<Category, Map<Grade, Double>> result = new EnumMap<>(Category.class);
        for (Category category : Category.values()) {
            result.put(category, SheepService.createUniformDistribution());
        }
        return result;
    }
}
