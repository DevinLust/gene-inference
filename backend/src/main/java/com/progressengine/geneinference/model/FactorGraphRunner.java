package com.progressengine.geneinference.model;

import com.progressengine.geneinference.model.enums.Category;
import com.progressengine.geneinference.model.enums.Grade;
import com.progressengine.geneinference.model.enums.MessageWaveType;
import com.progressengine.geneinference.model.enums.RunStage;

import java.util.*;

public class FactorGraphRunner {
    private final String userId;
    private final FactorGraph graph;
    private final Deque<MessageCategoryTask> frontier;
    private final Set<MessageCategoryTask> queued;
    private final VisualizationScope scope;
    private final int maxIterations;

    private int iterations;
    private int stepIndex;
    private RunStage stage;
    private boolean completed;
    private Category visibleCategory;
    private final List<Sheep> scopedBeliefSheep;
    private int beliefIndex;

    public FactorGraphRunner(FactorGraph graph, Sheep observedSheep, String userId) {
        this.userId = userId;
        this.graph = graph;
        this.frontier = new ArrayDeque<>();
        this.queued = new HashSet<>();
        this.scope = graph.buildScope(observedSheep);

        for (MessageCategoryTask task : graph.initialFrontierTasks()) {
            frontier.add(task);
            queued.add(task);
        }

        this.maxIterations = graph.estimatedMaxIterations();
        this.iterations = 0;
        this.stepIndex = 0;
        this.stage = RunStage.MESSAGE_PASSING;
        this.completed = false;
        this.visibleCategory = Category.SWIM;

        this.scopedBeliefSheep = new ArrayList<>();
        this.scopedBeliefSheep.add(observedSheep);

        for (Sheep sheep : scope.getScopedSheep()) {
            if (!sheep.equals(observedSheep)) {
                this.scopedBeliefSheep.add(sheep);
            }
        }
        this.beliefIndex = 0;
    }

    public LbpStepResult nextStep() {
        if (completed) {
            return new LbpStepResult(stepIndex, RunStage.COMPLETED, "Run already completed", true, null, null, null, null);
        }

        // Message passing stage
        if (stage == RunStage.MESSAGE_PASSING) {
            List<String> activeFullEdgeIds = new ArrayList<>();
            List<String> activeStubEdgeIds = new ArrayList<>();
            Set<String> seenEdgeIds = new HashSet<>();
            List<MessageCategoryTask> visibleWaveTasks = new ArrayList<>();
            MessageWaveType currentWaveType = null;

            while (!frontier.isEmpty() && iterations < maxIterations) {
                MessageCategoryTask task = frontier.pollFirst();
                queued.remove(task);

                Message message = task.message();
                Category category = task.category();

                boolean visible = isVisible(task);
                MessageWaveType taskWaveType = visible ? waveTypeOf(message) : null;

                if (visible) {
                    if (currentWaveType == null) {
                        currentWaveType = taskWaveType;
                    } else if (taskWaveType != currentWaveType) {
                        frontier.addFirst(task);
                        queued.add(task);

                        stepIndex++;
                        return new LbpStepResult(
                                stepIndex,
                                RunStage.MESSAGE_PASSING,
                                describeWave(currentWaveType, visibleWaveTasks.size()),
                                false,
                                currentWaveType,
                                visibleCategory.name(),
                                activeFullEdgeIds,
                                activeStubEdgeIds
                        );
                    }
                }

                Map<Grade, Double> newDistribution = graph.computeMessageForCategory(message, category);

                if (graph.reachedConvergence(message, category, newDistribution)) {
                    continue;
                }

                message.setDistributionForCategory(category, newDistribution);

                for (MessageCategoryTask dependent : graph.dependentsOf(task)) {
                    if (queued.add(dependent)) {
                        frontier.addLast(dependent);
                    }
                }

                iterations++;

                if (!visible) {
                    continue;
                }

                visibleWaveTasks.add(task);

                String edgeId = graph.visualEdgeIdForMessage(message, scope);
                if (edgeId != null && seenEdgeIds.add(edgeId)) {
                    boolean sourceDisplayed = scope.getScopedNodes().contains(message.getSource());
                    boolean targetDisplayed = scope.getScopedNodes().contains(message.getTarget());

                    if (sourceDisplayed && targetDisplayed) {
                        activeFullEdgeIds.add(edgeId);
                    } else {
                        activeStubEdgeIds.add(edgeId);
                    }
                }
            }
            stage = RunStage.BELIEF_UPDATE;
            if (!visibleWaveTasks.isEmpty()) {
                stepIndex++;
                return new LbpStepResult(
                        stepIndex,
                        RunStage.MESSAGE_PASSING,
                        describeWave(currentWaveType, visibleWaveTasks.size()),
                        false,
                        currentWaveType,
                        visibleCategory.name(),
                        activeFullEdgeIds,
                        activeStubEdgeIds
                );
            }
        }

        if (stage == RunStage.BELIEF_UPDATE) {
            if (beliefIndex < scopedBeliefSheep.size()) {
                Sheep sheep = scopedBeliefSheep.get(beliefIndex);

                Map<Category, Map<String, Double>> belief = graph.computeBeliefForSheep(sheep);

                List<String> activeFullEdgeIds = new ArrayList<>();
                List<String> activeStubEdgeIds = new ArrayList<>();
                Set<String> seenEdgeIds = new HashSet<>();

                for (Message message : graph.incomingMessagesForSheep(sheep)) {
                    String edgeId = graph.visualEdgeIdForMessage(message, scope);
                    if (edgeId == null || !seenEdgeIds.add(edgeId)) {
                        continue;
                    }

                    boolean sourceDisplayed = scope.getScopedNodes().contains(message.getSource());
                    boolean targetDisplayed = scope.getScopedNodes().contains(message.getTarget());

                    if (sourceDisplayed && targetDisplayed) {
                        activeFullEdgeIds.add(edgeId);
                    } else {
                        activeStubEdgeIds.add(edgeId);
                    }
                }

                beliefIndex++;
                stepIndex++;

                return new LbpStepResult(
                        stepIndex,
                        RunStage.BELIEF_UPDATE,
                        "Computed final belief for sheep " + sheep.getId(),
                        false,
                        MessageWaveType.RELATIONSHIP_TO_SHEEP,
                        visibleCategory.name(),
                        activeFullEdgeIds,
                        activeStubEdgeIds
                );
            }

            completed = true;
            stage = RunStage.COMPLETED;

            return new LbpStepResult(
                    stepIndex,
                    RunStage.COMPLETED,
                    "Final belief computation complete",
                    true,
                    null,
                    null,
                    null,
                    null
            );
        }

        return new LbpStepResult(stepIndex, RunStage.COMPLETED, "Run already completed", true, null, null, null, null);
    }

    public String getUserId() {
        return userId;
    }

    public RunStage getStage() {
        return stage;
    }

    public Category getVisibleCategory() {
        return visibleCategory;
    }

    public void setVisibleCategory(Category visibleCategory) {
        if (visibleCategory != null) {
            this.visibleCategory = visibleCategory;
        }
    }

    public boolean isCompleted() {
        return completed;
    }

    public VisualizationScope getScope() {
        return scope;
    }

    public int getEstimatedTotalSteps() {
        int beliefSteps = scopedBeliefSheep.size();
        int estimatedMessageSteps = Math.max(10, maxIterations / 50);

        return estimatedMessageSteps + beliefSteps;
    }

    private MessageWaveType waveTypeOf(Message message) {
        if (message.getSource() instanceof SheepNode) {
            return MessageWaveType.SHEEP_TO_RELATIONSHIP;
        }
        if (message.getSource() instanceof RelationshipNode) {
            return MessageWaveType.RELATIONSHIP_TO_SHEEP;
        }
        throw new IllegalStateException("Unknown message source node type");
    }

    private boolean isVisible(MessageCategoryTask task) {
        return scope.touches(task.message()) && task.category() == visibleCategory;
    }

    private String describeWave(MessageWaveType waveType, int affectedCount) {
        return switch (waveType) {
            case SHEEP_TO_RELATIONSHIP ->
                    "Updated " + affectedCount + " scoped sheep-originating messages";
            case RELATIONSHIP_TO_SHEEP ->
                    "Updated " + affectedCount + " scoped relationship-originating messages";
        };
    }
}
