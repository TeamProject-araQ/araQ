package com.team.araq.inquiry.answer;

import com.team.araq.inquiry.Inquiry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;


@Service
@RequiredArgsConstructor
public class AnswerService {

    private final AnswerRepository answerRepository;

    public void createAnswer(Inquiry inquiry, String content) {
        Answer answer = new Answer();
        answer.setContent(content);
        answer.setCreateDate(LocalDateTime.now());
        answer.setInquiry(inquiry);
        this.answerRepository.save(answer);
    }
}
