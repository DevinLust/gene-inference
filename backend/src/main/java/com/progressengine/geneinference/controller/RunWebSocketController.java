package com.progressengine.geneinference.controller;

import com.progressengine.geneinference.dto.RunEvent;
import com.progressengine.geneinference.dto.StartRunRequest;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.Map;
import java.util.UUID;

@Controller
public class RunWebSocketController {

    private final SimpMessagingTemplate messagingTemplate;

    public RunWebSocketController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/run.start")
    public void startRun(StartRunRequest request, Principal principal) {
        System.out.println("principal = " + principal);
        if (principal == null) {
            throw new IllegalStateException("Principal is still null in startRun");
        }
        System.out.println("principal name = " + principal.getName());


        String runId = UUID.randomUUID().toString();

        messagingTemplate.convertAndSendToUser(
                principal.getName(),
                "/queue/run-events",
                new RunEvent("stage_changed", runId, Map.of("stage", "message_passing"))
        );
    }
}
