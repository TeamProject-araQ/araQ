package com.team.araq.board.Post;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class PostController {

    @GetMapping("/post/create")
    public String create() {
        return "post/writePost";
    }

}
