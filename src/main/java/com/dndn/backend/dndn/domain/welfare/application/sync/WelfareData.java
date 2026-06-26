package com.dndn.backend.dndn.domain.welfare.application.sync;

import com.dndn.backend.dndn.domain.model.enums.HouseholdType;
import com.dndn.backend.dndn.domain.model.enums.InterestTopic;
import com.dndn.backend.dndn.domain.model.enums.LifeCycle;
import com.dndn.backend.dndn.domain.welfare.domain.enums.SourceType;
import lombok.Builder;

import java.util.Set;

@Builder
record WelfareData(
        String servId,
        String title,
        String summary,
        String content,
        String servLink,
        String ctpvNm,
        String sggNm,
        String eligibleUser,
        String detailInfo,
        String department,
        String org,
        SourceType sourceType,
        Set<LifeCycle> lifeCycles,
        Set<HouseholdType> householdTypes,
        Set<InterestTopic> interestTopics
) {
}
