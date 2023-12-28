package com.team.araq.chat;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@Slf4j
@RestController
@RequiredArgsConstructor
public class WebRTCController {
    private final SimpMessagingTemplate simpMessagingTemplate;

    @MessageMapping("/peer/offer")
    public void sendOffer(@Payload String candidate, Principal principal) {
        log.info("[offer] {}", candidate);
        String target = principal.getName().equals("njk7740") ? "asdf" : "njk7740";
        simpMessagingTemplate.convertAndSend("/topic/peer/offer/" + target, candidate);
    }

    @MessageMapping("/peer/answer")
    public void sendAnswer(@Payload String candidate, Principal principal) {
        log.info("[answer] {}", candidate);
        String target = principal.getName().equals("njk7740") ? "asdf" : "njk7740";
        simpMessagingTemplate.convertAndSend("/topic/peer/answer/" + target, candidate);
    }

    @MessageMapping("/peer/candidate")
    public void setCandidate(@Payload String candidate, Principal principal) {
        log.info("[answer] {}", candidate);
        String target = principal.getName().equals("njk7740") ? "asdf" : "njk7740";
        simpMessagingTemplate.convertAndSend("/topic/peer/candidate/" + target, candidate);
    }
}
