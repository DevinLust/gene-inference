package com.progressengine.geneinference.service;

import com.progressengine.geneinference.dto.CompletedPayload;
import com.progressengine.geneinference.dto.RunEvent;
import com.progressengine.geneinference.model.*;
import com.progressengine.geneinference.model.enums.Category;
import com.progressengine.geneinference.model.enums.RunEventType;
import com.progressengine.geneinference.model.enums.RunStage;
import com.progressengine.geneinference.dto.RunStartedPayload;
import com.progressengine.geneinference.dto.StepEventPayload;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class FactorGraphRunService {

    private final SheepService sheepService;
    private final RelationshipService relationshipService;
    private final Map<String, String> activeRunByUserId = new ConcurrentHashMap<>();
    private final Map<String, FactorGraphRunner> runners = new ConcurrentHashMap<>();

    public FactorGraphRunService(SheepService sheepService, RelationshipService relationshipService) {
        this.sheepService = sheepService;
        this.relationshipService = relationshipService;
    }

    @Transactional
    public RunEvent startRun(UUID userId, Integer targetSheepId) {
        String userIdStr = userId.toString();
        removeActiveRunForUser(userIdStr);

        String runId = UUID.randomUUID().toString();

        Sheep targetSheep =  sheepService.findByIdAndUserId(targetSheepId, userId);
        FactorGraph graph = new FactorGraph(sheepService.getAllSheep(userId), relationshipService.getAllRelationships(userId)); // sheep + relationships
        FactorGraphRunner runner = new FactorGraphRunner(graph, targetSheep, userIdStr);

        runners.put(runId, runner);
        activeRunByUserId.put(userIdStr, runId);

        return new RunEvent(
                RunEventType.RUN_STARTED,
                runId,
                new RunStartedPayload(
                        runner.getEstimatedTotalSteps(),
                        0,
                        RunStage.MESSAGE_PASSING
                )
        );
    }

    public RunEvent nextStep(String userId, String runId, Category category) {
        FactorGraphRunner runner = runners.get(runId);

        if (runner == null) {
            throw new IllegalArgumentException("Run not found: " + runId);
        }

        String activeRunId = activeRunByUserId.get(userId);
        if (!runId.equals(activeRunId)) {
            throw new IllegalStateException("Not the active run");
        }

        runner.setVisibleCategory(category);

        LbpStepResult step = runner.nextStep();

        if (step.completed()) {
            removeRun(runId);

            return new RunEvent(
                    RunEventType.COMPLETED,
                    runId,
                    new CompletedPayload(step.message())
            );
        }

        return new RunEvent(
                RunEventType.STEP_EVENT,
                runId,
                new StepEventPayload(
                        step.stepIndex(),
                        runner.getEstimatedTotalSteps(),
                        step.stage(),
                        step.message()
                )
        );
    }

    public FactorGraphRunner getRun(String runId) {
        return runners.get(runId);
    }

    public String getActiveRunIdForUser(String userId) {
        return activeRunByUserId.get(userId);
    }

    public void removeActiveRunForUser(String userId) {
        String existingRunId = activeRunByUserId.remove(userId);
        if (existingRunId != null) {
            runners.remove(existingRunId);
        }
    }

    public void removeRun(String runId) {
        FactorGraphRunner run = runners.remove(runId);
        if (run != null) {
            activeRunByUserId.remove(run.getUserId(), runId);
        }
    }
}
