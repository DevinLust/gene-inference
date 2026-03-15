package com.progressengine.geneinference.service;

import com.progressengine.geneinference.dto.CompletedPayload;
import com.progressengine.geneinference.dto.RunEvent;
import com.progressengine.geneinference.model.enums.RunEventType;
import com.progressengine.geneinference.model.enums.RunStage;
import com.progressengine.geneinference.dto.RunStartedPayload;
import com.progressengine.geneinference.dto.StepEventPayload;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class DemoRunService {

    private final Map<String, DemoRunState> runsById = new ConcurrentHashMap<>();
    private final Map<String, String> activeRunByUserId = new ConcurrentHashMap<>();

    public RunEvent startRun(String userId) {
        removeActiveRunForUser(userId);

        String runId = UUID.randomUUID().toString();

        List<DemoStep> steps = List.of(
                new DemoStep(RunStage.MESSAGE_PASSING, "Initialize messages"),
                new DemoStep(RunStage.MESSAGE_PASSING, "Update parent relationship message"),
                new DemoStep(RunStage.MESSAGE_PASSING, "Update partner relationship message"),
                new DemoStep(RunStage.MESSAGE_PASSING, "Convergence reached"),
                new DemoStep(RunStage.BELIEF_UPDATE, "Compute final belief for Sheep A"),
                new DemoStep(RunStage.BELIEF_UPDATE, "Compute final belief for Sheep B"),
                new DemoStep(RunStage.BELIEF_UPDATE, "Compute final belief for Sheep C"),
                new DemoStep(RunStage.BELIEF_UPDATE, "Final belief computation complete")
        );

        DemoRunState run = new DemoRunState(runId, userId, steps);

        runsById.put(runId, run);
        activeRunByUserId.put(userId, runId);

        return new RunEvent(
                RunEventType.RUN_STARTED,
                runId,
                new RunStartedPayload(run.getTotalSteps(), 0, RunStage.MESSAGE_PASSING)
        );
    }

    public RunEvent nextStep(String userId, String runId) {
        DemoRunState run = runsById.get(runId);

        if (run == null) {
            throw new IllegalArgumentException("Run not found: " + runId);
        }

        if (!run.getUserId().equals(userId)) {
            throw new IllegalStateException("User does not own this run");
        }

        String activeRunId = activeRunByUserId.get(userId);
        if (activeRunId == null || !activeRunId.equals(runId)) {
            throw new IllegalStateException("This is not the user's active run");
        }

        if (run.isCompleted()) {
            return new RunEvent(
                    RunEventType.COMPLETED,
                    runId,
                    new CompletedPayload("Run already completed")
            );
        }

        int nextIndex = run.getCurrentStepIndex();

        if (nextIndex >= run.getTotalSteps()) {
            run.setCompleted(true);
            removeRun(runId);

            return new RunEvent(
                    RunEventType.COMPLETED,
                    runId,
                    new CompletedPayload("Demo run completed")
            );
        }

        DemoStep step = run.getSteps().get(nextIndex);
        run.setCurrentStepIndex(nextIndex + 1);
        run.touch();

        if (run.getCurrentStepIndex() >= run.getTotalSteps()) {
            run.setCompleted(true);
            removeRun(runId);

            return new RunEvent(
                    RunEventType.COMPLETED,
                    runId,
                    new CompletedPayload(step.getMessage())
            );
        }

        return new RunEvent(
                RunEventType.STEP_EVENT,
                runId,
                new StepEventPayload(
                        run.getCurrentStepIndex(),
                        run.getTotalSteps(),
                        step.getStage(),
                        step.getMessage()
                )
        );
    }

    public DemoRunState getRun(String runId) {
        return runsById.get(runId);
    }

    public String getActiveRunIdForUser(String userId) {
        return activeRunByUserId.get(userId);
    }

    public void removeActiveRunForUser(String userId) {
        String existingRunId = activeRunByUserId.remove(userId);
        if (existingRunId != null) {
            runsById.remove(existingRunId);
        }
    }

    public void removeRun(String runId) {
        DemoRunState run = runsById.remove(runId);
        if (run != null) {
            activeRunByUserId.remove(run.getUserId(), runId);
        }
    }
}
