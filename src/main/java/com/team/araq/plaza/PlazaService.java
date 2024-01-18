package com.team.araq.plaza;

import com.team.araq.user.SiteUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PlazaService {
    private final PlazaRepo plazaRepo;

    public Plaza create(PlazaDto plazaDto) {
        Plaza plaza = new Plaza();
        plaza.setTitle(plazaDto.getTitle());
        plaza.setMaxPeople(plazaDto.getPeople());
        plaza.setPeople(0);
        plaza.setPassword(plazaDto.getPassword());
        plaza.setCode(plazaDto.getCode());
        plaza.setCreateDate(LocalDateTime.now());
        plaza.setManager(plazaDto.getManager());
        plaza.setReported(false);
        return plazaRepo.save(plaza);
    }

    public List<Plaza> getAll() {
        return plazaRepo.findAll();
    }

    public Plaza getByCode(String code) {
        Optional<Plaza> plaza = plazaRepo.findByCode(code);
        if (plaza.isPresent()) return plaza.get();
        throw new RuntimeException("그런 광장 없습니다.");
    }

    public void setPeople(Plaza plaza, Integer people) {
        plaza.setPeople(people);
        plazaRepo.save(plaza);
    }

    public void delete(Plaza plaza) {
        plazaRepo.delete(plaza);
    }

    public void changeManager(Plaza plaza, SiteUser user) {
        plaza.setManager(user);
        plazaRepo.save(plaza);
    }

    public void modify(Plaza plaza, PlazaDto plazaDto) {
        plaza.setTitle(plazaDto.getTitle());
        plaza.setPassword(plazaDto.getPassword());
        plaza.setMaxPeople(plazaDto.getPeople());
        plazaRepo.save(plaza);
    }

    public void setImg(Plaza plaza, String img) {
        plaza.setBackground(img);
        plazaRepo.save(plaza);
    }

    public void setReport(Plaza plaza, boolean report) {
        plaza.setReported(report);
        plazaRepo.save(plaza);
    }
}
