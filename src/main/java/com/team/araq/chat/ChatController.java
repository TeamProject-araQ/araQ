package com.team.araq.chat;

import com.team.araq.user.SiteUser;
import com.team.araq.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
@RequestMapping("/chat")
public class ChatController {
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final UserService userService;
    private final RoomService roomService;
    private final ChatService chatService;

    @MessageMapping("/send")
    public void sendMessage(ChatDto chatDto) {
        SiteUser user = userService.getByUsername(chatDto.getWriter());
        SiteUser target = userService.getByUsername(chatDto.getTarget());
        Room room = roomService.get(chatDto.getCode());

        Chat chat = chatService.create(room, user, target, chatDto.getContent(), room.getRecentDate());
        roomService.setRecent(room, chat.getCreateDate());

        chatDto.setWriterNick(user.getNickName());
        chatDto.setWriterImage(user.getImage());
        chatDto.setCreateDate(chat.getCreateDate());

        MessageDto messageDto = new MessageDto();
        messageDto.setType("sendChat");
        messageDto.setNickname(user.getNickName());
        messageDto.setContent(chatDto.getContent());
        messageDto.setImage(user.getImage());
        messageDto.setTarget(chatDto.getCode());

        simpMessagingTemplate.convertAndSend("/topic/chat/" + chatDto.getCode(), chatDto);
        simpMessagingTemplate.convertAndSend("/topic/all/" + target.getUsername(), messageDto);
    }

    @MessageMapping("/alert")
    public void alert(MessageDto messageDto, Principal principal) {
        SiteUser user = userService.getByUsername(principal.getName());
        messageDto.setNickname(user.getNickName());
        messageDto.setContent(user.getNickName() + "님이 거절하셨습니다.");
        simpMessagingTemplate.convertAndSend("/topic/all/" + messageDto.getTarget(), messageDto);
    }

    @PostMapping("/create")
    @ResponseBody
    public String chat(Principal principal, @RequestBody String targetName) {
        SiteUser user = userService.getByUsername(principal.getName());
        SiteUser target = userService.getByUsername(targetName);
        String uuid = UUID.randomUUID().toString();

        roomService.create(uuid, user, target);

        MessageDto messageDto = new MessageDto("acceptChat", user.getNickName(), user.getUsername(), null,
                null, null, uuid, null);
        simpMessagingTemplate.convertAndSend("/topic/all/" + targetName, messageDto);
        return uuid;
    }

    @GetMapping("/join/{code}")
    public String join(Model model, Principal principal, @PathVariable("code") String code) {
        SiteUser user = userService.getByUsername(principal.getName());
        Room room = roomService.get(code);

        if (!roomService.check(room, user)) throw new RuntimeException("권한이 없습니다.");

        if (user.getUsername().equals(room.getParticipant1().getUsername()))
            model.addAttribute("target", room.getParticipant2());
        else model.addAttribute("target", room.getParticipant1());
        model.addAttribute("user", user);
        model.addAttribute("room", room);
        model.addAttribute("chatList", room.getChats());
        return "conn/chat";
    }

    @GetMapping("/list")
    public String room(Model model, Principal principal) {
        SiteUser user = userService.getByUsername(principal.getName());
        List<Room> roomList = roomService.getList(user);

        model.addAttribute("roomList", roomList);
        return "conn/room";
    }

    @PostMapping("/request")
    @ResponseBody
    public String request(Principal principal, @RequestBody String username) {
        SiteUser user = userService.getByUsername(principal.getName());
        MessageDto messageDto = new MessageDto("chatRequest", user.getNickName(), user.getUsername(), user.getAge(),
                user.getIntroduce(), user.getImage(), user.getNickName() + "님이 채팅을 신청했습니다.", username);
        simpMessagingTemplate.convertAndSend("/topic/all/" + username, messageDto);
        return null;
    }

    @PostMapping("/delete")
    @ResponseBody
    public String delete(@RequestBody String code) {
        roomService.delete(roomService.get(code));
        return null;
    }

    @PostMapping("/confirm")
    @ResponseBody
    public String confirm(@RequestBody MessageDto messageDto) {
        SiteUser writer = userService.getByUsername(messageDto.getTarget());
        Room room = roomService.get(messageDto.getContent());
        List<Chat> chats = chatService.getByRoomAndWriter(room, writer);
        chatService.confirm(chats);

        ChatDto chatDto = new ChatDto();
        chatDto.setCode("confirm");
        chatDto.setTarget(messageDto.getTarget());

        simpMessagingTemplate.convertAndSend("/topic/chat/" + messageDto.getContent(), chatDto);
        return null;
    }

    @PostMapping("/uploadImage")
    @ResponseBody
    public String uploadImage(@RequestParam("files") MultipartFile[] files) {
        // todo 이미지 업로드
        return null;
    }
}
