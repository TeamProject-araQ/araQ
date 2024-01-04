package com.team.araq.idealType;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class IdealTypeDTO {

    @Min(value = 18, message = "최소 나이는 18살 이상이어야 합니다.")
    private int minAge;

    @Max(value = 100, message = "최대 나이는 100살 이하여야 합니다.")
    private int maxAge;

    @NotBlank(message = "음주 여부를 선택해주세요.")
    private String drinking;

    @NotBlank(message = "최종 학력을 선택해주세요.")
    private String education;

    @Min(value = 100, message = "최소 키는 100cm 이상이어야 합니다.")
    private int minHeight;

    @Max(value = 300, message = "최대 키는 300cm 이하여야 합니다.")
    private int maxHeight;

    @NotBlank(message = "흡연 여부를 선택해주세요.")
    private String smoking;

    @NotBlank(message = "종교를 선택해주세요.")
    private String religion;

}
