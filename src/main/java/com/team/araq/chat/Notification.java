package com.team.araq.chat;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Notification {
    private String title;
    private String content;
    private String sender;
    private String target;
    private String url;
}
