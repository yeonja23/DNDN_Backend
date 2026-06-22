package com.dndn.backend.dndn.domain.welfare.application;

import com.dndn.backend.dndn.domain.model.enums.HouseholdType;
import com.dndn.backend.dndn.domain.model.enums.InterestTopic;
import com.dndn.backend.dndn.domain.model.enums.LifeCycle;
import com.dndn.backend.dndn.domain.user.domain.entity.User;
import com.dndn.backend.dndn.domain.welfare.api.dto.response.WelfareDetailResDto;
import com.dndn.backend.dndn.domain.welfare.api.dto.response.WelfareListResDto;
import com.dndn.backend.dndn.domain.welfare.domain.Welfare;

import java.util.List;

public interface WelfareService {

    // 복지 서비스 전체 목록 조회
    WelfareListResDto welfareFindAll(int page, int numOfRows);

    // 복지 서비스 상세 조회
    WelfareDetailResDto welfareFindById(Long servId);

    // 이름으로 복지 서비스 목록 조회
    WelfareListResDto welfareFindByTitle(String title, int page, int size);

    // 카테고리별 복지 서비스 목록 조회
    WelfareListResDto welfareFindByCategory(
            LifeCycle lifeCycle,
            List<HouseholdType> householdTypes,
            List<InterestTopic> interestTopics,
            int page,
            int size
    );

    // 검색어 + 카테고리 복지 서비스 목록 조회

    WelfareListResDto welfareSearch(
            String keyword, LifeCycle lifeCycle,
            List<HouseholdType> householdTypes,
            List<InterestTopic> interestTopics,
            int page, int size
    );

    // 복지 추천 로직
    List<Welfare> getRecommendedWelfares(User user);
}

