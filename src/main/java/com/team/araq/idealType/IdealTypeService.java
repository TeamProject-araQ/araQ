package com.team.araq.idealType;

import com.team.araq.user.SiteUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class IdealTypeService {

    private final IdealTypeRepository idealTypeRepository;

    public void createIdealType(SiteUser user, IdealTypeDTO idealTypeDTO) {
        IdealType idealType = new IdealType();
        idealType.setUser(user);
        idealType.setEducation(idealTypeDTO.getEducation());
        idealType.setDrinking(idealTypeDTO.getDrinking());
        idealType.setMaxAge(idealTypeDTO.getMaxAge());
        idealType.setMinAge(idealTypeDTO.getMinAge());
        idealType.setMaxHeight(idealTypeDTO.getMaxHeight());
        idealType.setMinHeight(idealTypeDTO.getMinHeight());
        idealType.setSmoking(idealTypeDTO.getSmoking());
        idealType.setReligion(idealTypeDTO.getReligion());
        this.idealTypeRepository.save(idealType);
    }

    public IdealType getIdealTypeByUser(SiteUser user) {
        Optional<IdealType> idealType = this.idealTypeRepository.findByUser(user);
        if (idealType.isPresent()) return idealType.get();
        else throw new RuntimeException("그런 이상형 없습니다.");
    }

    public void modifyIdealType(IdealType idealType, IdealTypeDTO idealTypeDTO) {
        idealType.setEducation(idealTypeDTO.getEducation());
        idealType.setDrinking(idealTypeDTO.getDrinking());
        idealType.setMaxAge(idealTypeDTO.getMaxAge());
        idealType.setMinAge(idealTypeDTO.getMinAge());
        idealType.setMaxHeight(idealTypeDTO.getMaxHeight());
        idealType.setMinHeight(idealTypeDTO.getMinHeight());
        idealType.setSmoking(idealTypeDTO.getSmoking());
        idealType.setReligion(idealTypeDTO.getReligion());
        this.idealTypeRepository.save(idealType);
    }
}
