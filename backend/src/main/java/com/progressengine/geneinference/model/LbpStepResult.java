package com.progressengine.geneinference.model;

import com.progressengine.geneinference.model.enums.RunStage;
import com.progressengine.geneinference.model.enums.MessageWaveType;

import java.util.List;

public record LbpStepResult(
        int stepIndex,
        RunStage stage,
        String message,
        boolean completed,
        MessageWaveType waveType,
        String category,
        List<String> activeFullEdgeIds,
        List<String> activeStubEdgeIds
) {
}
