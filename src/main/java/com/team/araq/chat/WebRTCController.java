package com.team.araq.chat;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class WebRTCController {
    private final SimpMessagingTemplate simpMessagingTemplate;

    @MessageMapping("/peer/offer/{target}")
    public void sendOffer(@Payload String candidate, @DestinationVariable("target") String target) {
        simpMessagingTemplate.convertAndSend("/topic/peer/offer/" + target, candidate);
    }

    @MessageMapping("/peer/answer/{target}")
    public void sendAnswer(@Payload String candidate, @DestinationVariable("target") String target) {
        simpMessagingTemplate.convertAndSend("/topic/peer/answer/" + target, candidate);
    }

    @MessageMapping("/peer/candidate/{target}")
    public void setCandidate(@Payload String candidate, @DestinationVariable("target") String target) {
        simpMessagingTemplate.convertAndSend("/topic/peer/candidate/" + target, candidate);
    }
}
