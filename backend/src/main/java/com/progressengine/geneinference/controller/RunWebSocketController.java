package com.progressengine.geneinference.controller;

import com.progressengine.geneinference.dto.*;
import com.progressengine.geneinference.service.FactorGraphRunService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.UUID;

@Controller
public class RunWebSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final FactorGraphRunService factorGraphRunService;

    public RunWebSocketController(
            SimpMessagingTemplate messagingTemplate,
            FactorGraphRunService factorGraphRunService
    ) {
        this.messagingTemplate = messagingTemplate;
        this.factorGraphRunService = factorGraphRunService;
    }

    @MessageMapping("/run.start")
    public void startRun(StartRunRequest request, Principal principal) {
        String userIdStr = principal.getName();
        UUID userId = UUID.fromString(userIdStr);

        RunEvent event = factorGraphRunService.startRun(userId, request.sheepId());

        messagingTemplate.convertAndSendToUser(
                userIdStr,
                "/queue/run-events",
                event
        );
    }

    @MessageMapping("/run.nextStep")
    public void nextStep(NextStepRequest request, Principal principal) {
        String userId = principal.getName();

        RunEvent event = factorGraphRunService.nextStep(
                userId,
                request.getRunId(),
                request.getCategory()
        );

        messagingTemplate.convertAndSendToUser(
                userId,
                "/queue/run-events",
                event
        );
    }
}
