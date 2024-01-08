package com.team.araq.plaza;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PlazaService {
    private final PlazaRepo plazaRepo;

    public void create(PlazaDto plazaDto) {
        Plaza plaza = new Plaza();
        plaza.setTitle(plazaDto.getTitle());
        plaza.setMaxPeople(plazaDto.getPeople());
        plaza.setPeople(0);
        plaza.setPassword(plazaDto.getPassword());
        plaza.setCode(plazaDto.getCode());
        plaza.setCreateDate(LocalDateTime.now());
        plaza.setManager(plazaDto.getManager());
        plazaRepo.save(plaza);
    }

    public List<Plaza> getAll() {
        return plazaRepo.findAll();
    }

    public Plaza getByCode(String code) {
        Optional<Plaza> plaza = plazaRepo.findByCode(code);
        if (plaza.isPresent()) return plaza.get();
        throw new RuntimeException("그런 광장 없습니다.");
    }
}
