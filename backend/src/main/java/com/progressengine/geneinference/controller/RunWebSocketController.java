package com.progressengine.geneinference.controller;

import com.progressengine.geneinference.dto.NextStepRequest;
import com.progressengine.geneinference.dto.RunEvent;
import com.progressengine.geneinference.dto.StartRunRequest;
import com.progressengine.geneinference.service.DemoRunService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
public class RunWebSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final DemoRunService demoRunService;

    public RunWebSocketController(
            SimpMessagingTemplate messagingTemplate,
            DemoRunService demoRunService
    ) {
        this.messagingTemplate = messagingTemplate;
        this.demoRunService = demoRunService;
    }

    @MessageMapping("/run.start")
    public void startRun(StartRunRequest request, Principal principal) {
        String userId = principal.getName();

        RunEvent event = demoRunService.startRun(userId);

        messagingTemplate.convertAndSendToUser(
                userId,
                "/queue/run-events",
                event
        );
    }

    @MessageMapping("/run.nextStep")
    public void nextStep(NextStepRequest request, Principal principal) {
        String userId = principal.getName();

        RunEvent event = demoRunService.nextStep(userId, request.getRunId());

        messagingTemplate.convertAndSendToUser(
                userId,
                "/queue/run-events",
                event
        );
    }
}
