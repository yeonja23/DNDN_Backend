package com.dndn.backend.dndn.domain.welfare.api.dto.response;

import com.dndn.backend.dndn.domain.model.enums.HouseholdType;
import com.dndn.backend.dndn.domain.model.enums.LifeCycle;
import com.dndn.backend.dndn.domain.welfare.domain.Welfare;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class RecommendedWelfareResponseDTO {

    private Long welfareId;
    private String title;
    private String summary;
    private String department;

    private List<LifeCycle> lifeCycles;               // 복지의 대상 생애주기
    private List<HouseholdType> householdTypes;      // 복지의 대상 가구유형

    private String region; // ex: "서울특별시 양천구"

    public static RecommendedWelfareResponseDTO from(Welfare w) {
        return new RecommendedWelfareResponseDTO(
                w.getId(),
                w.getTitle(),
                w.getSummary(),
                w.getDepartment(),
                List.copyOf(w.getLifeCycles()),
                List.copyOf(w.getHouseholdTypes()),
                w.getCtpvNm() + " " + w.getSggNm()
        );
    }
}
