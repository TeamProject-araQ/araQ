package com.team.araq.taste;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TasteService {

    private final TasteRepository tasteRepository;
}
