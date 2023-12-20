package com.team.araq.review;

import jakarta.persistence.Column;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReviewDTO {

    private String answer1;

    private String answer2;

    private String answer3;

    private String answer4;

    private String answer5;

    private double star;

}
