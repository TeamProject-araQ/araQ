package com.team.araq.chat;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class ChatDto {
    private String content;
    private String writer;
    private String code;
    private String target;
    private String writerImage;
    private String writerNick;
    private LocalDateTime createDate;
    private Integer confirm;
    private Integer difDate;
    private List<String> images;
}
