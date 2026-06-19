package com.dndn.backend.dndn.domain.welfare.application;

import com.dndn.backend.dndn.domain.model.enums.HouseholdType;
import com.dndn.backend.dndn.domain.model.enums.InterestTopic;
import com.dndn.backend.dndn.domain.model.enums.LifeCycle;
import com.dndn.backend.dndn.domain.user.domain.entity.User;
import com.dndn.backend.dndn.domain.user.domain.repository.UserRepository;
import com.dndn.backend.dndn.domain.welfare.api.response.WelfareDetailResDto;
import com.dndn.backend.dndn.domain.welfare.api.response.WelfareListResDto;
import com.dndn.backend.dndn.domain.welfare.domain.Welfare;
import com.dndn.backend.dndn.domain.welfare.domain.repository.WelfareRepository;
import com.dndn.backend.dndn.domain.welfare.exception.WelfareException;
import com.dndn.backend.dndn.domain.welfare.support.WelfareWithScore;
import com.dndn.backend.dndn.global.error.code.status.ErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WelfareServiceImpl implements WelfareService {

    private final WelfareRepository welfareRepository;

    // 복지 서비스 목록 전체 조회
    @Override
    public WelfareListResDto welfareFindAll(int page, int size) {
        Page<Welfare> result = welfareRepository.findAll(PageRequest.of(page, size));
        return WelfareListResDto.from(result);
    }

    // 복지 id로 복지 서비스 상세 조회
    @Override
    public WelfareDetailResDto welfareFindById(Long welfareId) {
        Welfare welfare = welfareRepository.findById(welfareId)
                .orElseThrow(() -> new WelfareException(ErrorStatus._WELFARE_NOT_FOUND));
        return WelfareDetailResDto.of(welfare);
    }

    // 복지 이름으로 복지 서비스 목록 조회
    @Override
    public WelfareListResDto welfareFindByTitle(String title, int page, int size) {
        Page<Welfare> result = welfareRepository.findByTitleContaining(title, PageRequest.of(page, size));
        return WelfareListResDto.from(result);
    }

    @Override
    public WelfareListResDto welfareFindByCategory(
            LifeCycle lifeCycle,
            List<HouseholdType> householdTypes,
            List<InterestTopic> interestTopics,
            int page,
            int size
    ) {
        // null-safe 처리
        List<HouseholdType> hh = (householdTypes == null) ? Collections.emptyList() : householdTypes;
        List<InterestTopic> it = (interestTopics == null) ? Collections.emptyList() : interestTopics;

        Page<Welfare> result = welfareRepository.findByCategoryFilters(
                lifeCycle, hh, hh.isEmpty(), it, it.isEmpty(),
                PageRequest.of(page, size)
        );

        return WelfareListResDto.from(result);
    }

    // 검색어 + 카테고리 복지 서비스 목록 조회
    @Override
    public WelfareListResDto welfareSearch(
            String keyword,
            LifeCycle lifeCycle,
            List<HouseholdType> householdTypes,
            List<InterestTopic> interestTopics,
            int page,
            int size
    ) {
        if (lifeCycle == null || keyword == null || keyword.isBlank()) {
            throw new WelfareException(ErrorStatus._BAD_REQUEST); // 프로젝트 공통 에러코드에 맞춰 사용
        }

        List<HouseholdType> hh = (householdTypes == null) ? Collections.emptyList() : householdTypes;
        List<InterestTopic> it = (interestTopics == null) ? Collections.emptyList() : interestTopics;

        Page<Welfare> result = welfareRepository.searchByKeywordAndCategory(
                keyword.trim(), lifeCycle,
                hh, hh.isEmpty(),
                it, it.isEmpty(),
                PageRequest.of(page, size)
        );

        return WelfareListResDto.from(result);
    }

    @Override
    public List<Welfare> getRecommendedWelfares(User user) {
        List<Welfare> all = welfareRepository.findAll();

        return all.stream()
                .map(w -> new WelfareWithScore(w, calculateScore(user, w)))
                .filter(w -> w.getScore() >= 0) // 지역 조건 불일치 등으로 점수 0이면 제외
                .sorted(Comparator.comparingDouble(WelfareWithScore::getScore).reversed())
                .limit(5)
                .map(WelfareWithScore::getWelfare)
                .toList();
    }

    private double calculateScore(User user, Welfare welfare) {

        double score = 0;

        if (!isRegionMatch(user.getAddress(), welfare.getCtpvNm(), welfare.getSggNm())) {
            score -=100;
        }

        if (isLifeCycleMatched(user, welfare)) {
            score += 1;
        }

        score += countHouseholdTypeMatches(user, welfare);

        return score;
    }

    private boolean isLifeCycleMatched(User user, Welfare welfare) {
        LifeCycle userCycle = user.getLifeCycle();
        Set<LifeCycle> targetCycles = welfare.getLifeCycles();
        return targetCycles.contains(userCycle);
    }

    private long countHouseholdTypeMatches(User user, Welfare welfare) {
        Set<HouseholdType> userTypes = user.getHouseholdTypes();
        Set<HouseholdType> targetTypes = welfare.getHouseholdTypes();
        return targetTypes.stream()
                .filter(userTypes::contains)
                .count();
    }

    private boolean isRegionMatch(String userAddress, String ctpvNm, String sggNm) {
        if (userAddress == null || ctpvNm == null || sggNm == null) return false;

        String[] tokens = userAddress.split(" ");
        if (tokens.length < 2) return false;

        String userRegion = tokens[0] + " " + tokens[1];
        String targetRegion = ctpvNm + " " + sggNm;

        return userRegion.equals(targetRegion);
    }
}
