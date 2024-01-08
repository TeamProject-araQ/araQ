package com.team.araq.message;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.team.araq.friend.Friend;
import com.team.araq.friend.FriendService;
import com.team.araq.user.SiteUser;
import com.team.araq.user.UserService;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.data.domain.Page;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/message")
public class MessageController {

    private final MessageService messageService;

    private final UserService userService;

    private final FriendService friendService;

    private final SimpMessageSendingOperations messagingTemplate;

    @GetMapping("/receive")
    public String getReceive(Principal principal, Model model, @RequestParam(value = "page", defaultValue = "0") int page,
                             @RequestParam(value = "keyword", defaultValue = "") String keyword) {
        SiteUser user = this.userService.getByUsername(principal.getName());
        Page<Message> receiveList = this.messageService.getListByReceiver(user, keyword, page);
        model.addAttribute("receiveList", receiveList);
        return "message/receive";
    }

    @GetMapping("/send")
    public String getSend(Principal principal, Model model, @RequestParam(value = "page", defaultValue = "0") int page,
                          @RequestParam(value = "keyword", defaultValue = "") String keyword) {
        SiteUser user = this.userService.getByUsername(principal.getName());
        List<Friend> friendList = this.friendService.getList(user);
        Page<Message> sendList = this.messageService.getListBySender(user, keyword, page);
        model.addAttribute("keyword", keyword);
        model.addAttribute("paging", sendList);
        model.addAttribute("friendList", friendList);
        return "message/send";
    }

    @MessageMapping("/send/message")
    public void sendMessage(@Payload String messageInfo) throws JsonProcessingException {
        JSONObject jsonObject = new JSONObject(messageInfo);
        SiteUser sender = this.userService.getByUsername(jsonObject.getString("sender"));
        SiteUser receiver = this.userService.getByUsername(jsonObject.getString("receiver"));
        String content = jsonObject.getString("content");
        Message message = this.messageService.sendMessage(sender, receiver, content);
        LocalDateTime dateTime = message.getDateTime();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        String formattedDateTime = dateTime.format(formatter);
        Map<String, String> data = new HashMap<>();
        data.put("image", sender.getImage());
        data.put("senderNick", sender.getNickName());
        data.put("sender", sender.getUsername());
        data.put("receiver", receiver.getUsername());
        data.put("datetime", formattedDateTime);
        data.put("content", content);
        data.put("messageId", String.valueOf(message.getId()));
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonData = objectMapper.writeValueAsString(data);
        messagingTemplate.convertAndSend("/topic/send/message/" + receiver.getUsername(), jsonData);
    }

    @PostMapping("/read")
    @ResponseBody
    public String updateStatus(@RequestBody String messageId) {
        Message message = this.messageService.getMessage(Integer.parseInt(messageId));
        this.messageService.updateStatus(message);
        return null;
    }
}
