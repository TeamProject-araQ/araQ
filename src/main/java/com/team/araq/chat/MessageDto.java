package com.team.araq.chat;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MessageDto {
    private String type;
    private String nickname;
    private String age;
    private String introduce;
    private String image;
    private String content;
    private String target;
}
