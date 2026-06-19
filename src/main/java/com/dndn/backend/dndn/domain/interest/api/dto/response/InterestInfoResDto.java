package com.dndn.backend.dndn.domain.interest.api.dto.response;

import com.dndn.backend.dndn.domain.interest.domain.Interest;
import lombok.Builder;

import java.util.List;

@Builder
public record InterestInfoResDto(
        Long welfareId,
        Boolean interestStatus,
        String welfareTitle,
        List<String> lifeCycleNames,
        List<String> householdTypeNames,
        List<String> interestTopicNames,
        String department,
        String org
) {
    public static InterestInfoResDto from(Interest interest) {
        var welfare = interest.getWelfare();

        List<String> lifeCycleNames = welfare.getLifeCycles().stream()
                .map(it -> it.getKor())
                .distinct()
                .toList();

        List<String> householdTypeNames = welfare.getHouseholdTypes().stream()
                .map(it -> it.getKor())
                .distinct()
                .toList();

        List<String> interestTopicNames = welfare.getInterestTopics().stream()
                .map(it -> it.getKor())
                .distinct()
                .toList();

        return InterestInfoResDto.builder()
                .welfareId(welfare.getId())
                .welfareTitle(welfare.getTitle())
                .interestStatus(interest.getInterestStatus())
                .lifeCycleNames(lifeCycleNames)
                .householdTypeNames(householdTypeNames)
                .interestTopicNames(interestTopicNames)
                .department(welfare.getDepartment())
                .org(welfare.getOrg())
                .build();
    }
}
