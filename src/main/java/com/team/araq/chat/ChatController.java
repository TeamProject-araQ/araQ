package com.team.araq.chat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.team.araq.chat.rate.Rate;
import com.team.araq.chat.rate.RateService;
import com.team.araq.user.SiteUser;
import com.team.araq.user.UserService;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
@RequestMapping("/chat")
public class ChatController {

    private final SimpMessagingTemplate simpMessagingTemplate;

    private final UserService userService;

    private final RoomService roomService;

    private final ChatService chatService;

    private final RateService rateService;

    @MessageMapping("/send")
    public void sendMessage(ChatDto chatDto) {
        SiteUser user = userService.getByUsername(chatDto.getWriter());
        SiteUser target = userService.getByUsername(chatDto.getTarget());
        Room room = roomService.get(chatDto.getCode());

        Chat chat = chatService.create(room, user, target, chatDto.getContent(), room.getRecentDate());
        roomService.setRecent(room, chat.getCreateDate());
        roomService.setConfirm(room, target.getUsername());

        chatDto.setWriterNick(user.getNickName());
        chatDto.setWriterImage(user.getImage());
        chatDto.setCreateDate(chat.getCreateDate());

        MessageDto messageDto = new MessageDto();
        messageDto.setType("sendChat");
        messageDto.setNickname(user.getNickName());
        messageDto.setContent(chatDto.getContent());
        messageDto.setImage(user.getImage());
        messageDto.setTarget(chatDto.getCode());

        this.roomService.saveChatNumbers(room);

        Notification notification = new Notification("채팅 알림",
                user.getNickName() + "님이 채팅를 보냈습니다.", user.getUsername(), target.getUsername(),
                "/chat/join/" + chatDto.getCode());

        simpMessagingTemplate.convertAndSend("/topic/chat/" + chatDto.getCode(), chatDto);
        simpMessagingTemplate.convertAndSend("/topic/all/" + target.getUsername(), messageDto);
        simpMessagingTemplate.convertAndSend("/topic/notification/" + target.getUsername(), notification);
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

        if (user.getUsername().equals(room.getParticipant1().getUsername())) {
            Rate rate = this.rateService.checkRate(user, room.getParticipant2());
            model.addAttribute("target", room.getParticipant2());
            model.addAttribute("rate", rate);
        } else {
            Rate rate = this.rateService.checkRate(user, room.getParticipant1());
            model.addAttribute("target", room.getParticipant1());
            model.addAttribute("rate", rate);
        }
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
        Notification notification = new Notification("채팅 요청", user.getNickName() + "님이 채팅을 신청했습니다.",
                user.getUsername(), username, "#");
        this.userService.useChatPass(user);
        simpMessagingTemplate.convertAndSend("/topic/all/" + username, messageDto);
        simpMessagingTemplate.convertAndSend("/topic/notification/" + username, notification);
        return null;
    }

    @PostMapping("/delete")
    @ResponseBody
    public String delete(@RequestBody String code) {
        File file = new File("uploads/chat/" + code);
        if (file.exists()) deleteFiles(file);
        roomService.delete(roomService.get(code));
        return null;
    }

    @PostMapping("/confirm")
    @ResponseBody
    public String confirm(@RequestBody MessageDto messageDto, Principal principal) {
        SiteUser writer = userService.getByUsername(messageDto.getTarget());
        Room room = roomService.get(messageDto.getContent());
        List<Chat> chats = chatService.getByRoomAndWriter(room, writer);
        chatService.confirm(chats);
        roomService.confirm(room, principal.getName());

        ChatDto chatDto = new ChatDto();
        chatDto.setCode("confirm");
        chatDto.setTarget(messageDto.getTarget());

        simpMessagingTemplate.convertAndSend("/topic/chat/" + messageDto.getContent(), chatDto);
        return null;
    }

    @PostMapping("/uploadImage")
    @ResponseBody
    public String uploadImage(@RequestParam("files") MultipartFile[] files,
                              @RequestParam("chatContainer") String chatContainer) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        ChatDto chatDto = objectMapper.readValue(chatContainer, ChatDto.class);

        SiteUser user = userService.getByUsername(chatDto.getWriter());
        SiteUser target = userService.getByUsername(chatDto.getTarget());
        Room room = roomService.get(chatDto.getCode());

        Chat chat = chatService.create(room, user, target, chatDto.getContent(), room.getRecentDate());
        roomService.setRecent(room, chat.getCreateDate());
        roomService.setConfirm(room, target.getUsername());

        String dirPath = "uploads/chat/" + room.getCode() + "/" + chat.getId();
        File dir = new File(dirPath);
        List<String> images = new ArrayList<>();

        if (!dir.exists()) dir.mkdirs();

        for (MultipartFile file : files) {
            String filename = file.getOriginalFilename();
            File image = new File(dirPath + "/" + filename);
            FileCopyUtils.copy(file.getBytes(), image);
            images.add("/chat/image/" + room.getCode() + "/" + chat.getId() + "/" + filename);
        }

        chatService.setImages(chat, images);

        chatDto.setWriterNick(user.getNickName());
        chatDto.setWriterImage(user.getImage());
        chatDto.setCreateDate(chat.getCreateDate());
        chatDto.setImages(images);

        MessageDto messageDto = new MessageDto();
        messageDto.setType("sendChat");
        messageDto.setNickname(user.getNickName());
        messageDto.setContent(chatDto.getContent());
        messageDto.setImage(user.getImage());
        messageDto.setTarget(chatDto.getCode());

        simpMessagingTemplate.convertAndSend("/topic/chat/" + chatDto.getCode(), chatDto);
        simpMessagingTemplate.convertAndSend("/topic/all/" + target.getUsername(), messageDto);

        return null;
    }

    @MessageMapping("/all/{target}")
    public void handlerAll(@Payload String data, @DestinationVariable("target") String target) {
        simpMessagingTemplate.convertAndSend("/topic/all/" + target, data);
    }

    @Scheduled(fixedRate = 60000)
    public void ping() {
        userService.logout(userService.getLoginUsers());
        simpMessagingTemplate.convertAndSend("/topic/ping", "ping");
    }

    @MessageMapping("/pong")
    public void pong(@Payload String data) {
        JSONObject json = new JSONObject(data);
        SiteUser user = userService.getByUsername(json.get("user").toString());
        userService.login(user);
        userService.setLocation(user, json.get("location").toString());
    }

    public void deleteFiles(File file) {
        if (file.isDirectory()) {
            for (File subfile : Objects.requireNonNull(file.listFiles())) {
                deleteFiles(subfile);
            }
        }
        file.delete();
    }
}
