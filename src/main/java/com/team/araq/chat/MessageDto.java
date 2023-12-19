package com.team.araq.chat;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class MessageDto {
    private String type;
    private String nickname;
    private String age;
    private String introduce;
    private String image;
    private String content;
}
