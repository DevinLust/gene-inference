package com.progressengine.geneinference.model;

import com.progressengine.geneinference.model.enums.RunStage;

public record LbpStepResult(
        int stepIndex,
        RunStage stage,
        String message,
        boolean completed
) {
}
