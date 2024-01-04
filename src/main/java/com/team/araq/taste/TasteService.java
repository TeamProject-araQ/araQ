package com.team.araq.taste;

import com.team.araq.user.SiteUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TasteService {

    private final TasteRepository tasteRepository;

    public void saveTaste(SiteUser user, TasteDTO tasteDTO) {
        Taste taste = new Taste();
        taste.setUser(user);
        taste.setOption1(tasteDTO.getOption1());
        taste.setOption2(tasteDTO.getOption2());
        taste.setOption3(tasteDTO.getOption3());
        taste.setOption4(tasteDTO.getOption4());
        taste.setOption5(tasteDTO.getOption5());
        taste.setOption6(tasteDTO.getOption6());
        taste.setOption7(tasteDTO.getOption7());
        taste.setOption8(tasteDTO.getOption8());
        taste.setOption9(tasteDTO.getOption9());
        taste.setOption10(tasteDTO.getOption10());
        this.tasteRepository.save(taste);
    }

    public Taste getTaste(SiteUser user) {
        Optional<Taste> taste = this.tasteRepository.findByUser(user);
        if (taste.isPresent()) return taste.get();
        else throw new RuntimeException("그런 취향 없습니다.");
    }

    public void modifyTaste(Taste taste, TasteDTO tasteDTO) {
        taste.setOption1(tasteDTO.getOption1());
        taste.setOption2(tasteDTO.getOption2());
        taste.setOption3(tasteDTO.getOption3());
        taste.setOption4(tasteDTO.getOption4());
        taste.setOption5(tasteDTO.getOption5());
        taste.setOption6(tasteDTO.getOption6());
        taste.setOption7(tasteDTO.getOption7());
        taste.setOption8(tasteDTO.getOption8());
        taste.setOption9(tasteDTO.getOption9());
        taste.setOption10(tasteDTO.getOption10());
        this.tasteRepository.save(taste);
    }
}
