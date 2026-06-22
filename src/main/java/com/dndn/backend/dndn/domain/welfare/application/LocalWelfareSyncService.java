package com.dndn.backend.dndn.domain.welfare.application;

import com.dndn.backend.dndn.domain.model.enums.HouseholdType;
import com.dndn.backend.dndn.domain.model.enums.InterestTopic;
import com.dndn.backend.dndn.domain.model.enums.LifeCycle;
import com.dndn.backend.dndn.domain.welfare.domain.Welfare;
import com.dndn.backend.dndn.domain.welfare.domain.enums.SourceType;
import com.dndn.backend.dndn.domain.welfare.domain.repository.WelfareRepository;
import com.dndn.backend.dndn.domain.welfare.infra.local.client.LocalWelfareClient;
import com.dndn.backend.dndn.domain.welfare.infra.local.dto.response.LocalDetailResDto;
import com.dndn.backend.dndn.domain.welfare.infra.local.dto.response.LocalListResDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static com.dndn.backend.dndn.domain.welfare.support.CategoryParserUtils.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class LocalWelfareSyncService {

    private final WelfareRepository welfareRepository;
    private final LocalWelfareClient localClient;

    public int syncLocalWelfareData(int maxCount) {
        int numOfRows = Math.min(maxCount, 100);
        int page = 1;
        int processed = 0;
        int skipped = 0;

        while (processed < maxCount) {
            LocalListResDto listDto = localClient.getWelfareList(page, numOfRows);
            if (listDto == null || listDto.getServList() == null || listDto.getServList().isEmpty()) {
                log.info("[지자체 동기화] {} 페이지에 더 이상 데이터 없음. 종료", page);
                break;
            }

            log.info("[지자체 동기화] {} 페이지, {}건 처리 시작", page, listDto.getServList().size());

            for (LocalListResDto.ServiceItem item : listDto.getServList()) {
                if (processed >= maxCount) break;
                processed++;
                try {
                String servId = item.getServId();
                if (isBlank(servId)) continue;

                // 상세 조회
                LocalDetailResDto detail = localClient.getWelfareDetail(servId);
                if (detail == null) {
                    log.warn("[지자체 동기화] 상세 조회 실패 servId={}", servId);
                    continue;
                }

                // 카테고리 매핑
                // 지자체는 생애주기/가구상황이 LIST엔 없고 DETAIL에만 오므로 detail 우선
                String lifeSrc  = nzOr(detail.getLifeNmArray(), item.getLifeNmArray());
                String hhSrc    = nzOr(detail.getTrgterIndvdlNmArray(), item.getTrgterIndvdlNmArray());
                String intrsSrc = nzOr(detail.getIntrsThemaNmArray(), item.getIntrsThemaNmArray());

                List<LifeCycle> life = parseLifeCycles(nz(lifeSrc));
                List<HouseholdType> hh = parseHouseholdTypes(nz(hhSrc));
                List<InterestTopic> it = parseInterestTopics(nz(intrsSrc));

                String org = Optional.ofNullable(detail.getInqplCtadrList())
                        .orElse(List.of())
                        .stream()
                        .map(LocalDetailResDto.RelatedInfo::getWlfareInfoReldNm)
                        .filter(s -> s != null && !s.isBlank())
                        .findFirst() // 여러 개면 첫 번째만 사용
                        .orElse("기관 미제공");

                String detailInfo = Optional.ofNullable(detail.getBasfrmList())
                        .orElse(List.of())
                        .stream()
                        .map(LocalDetailResDto.RelatedInfo::getWlfareInfoReldCn)
                        .filter(s -> s != null && !s.isBlank())
                        .findFirst()
                        .orElse("상세정보 미제공");


                Welfare welfare = welfareRepository.findByServId(servId).orElse(null);

                if (welfare == null) {
                    welfareRepository.save(Welfare.builder()
                            .servId(servId)
                            .title(nzOr(detail.getServNm(), item.getServNm(), "제목 미제공"))
                            .summary(nzOr(detail.getServDgst(), "요약 미제공"))
                            .content(nzOr(detail.getAlwServCn(), "내용 미제공"))
                            .servLink(nz(item.getServDtlLink()))
                            .eligibleUser(nzOr(detail.getSprtTrgtCn(), "대상자 정보 미제공"))
                            .detailInfo(detailInfo)
                            .department(nzOr(detail.getBizChrDeptNm(), "담당부처 미제공"))
                            .org(org)
                            .sourceType(SourceType.LOCAL)
                            .ctpvNm(item.getCtpvNm())
                            .sggNm(item.getSggNm())
                            .lifeCycles(new HashSet<>(life))
                            .householdTypes(new HashSet<>(hh))
                            .interestTopics(new HashSet<>(it))
                            .build());
                } else {
                    // 새로운 값 추출
                    String newSummary      = detail.getServDgst();
                    String newContent      = nzOr(detail.getAlwServCn(), "내용 미제공");
                    String newServLink     = nz(item.getServDtlLink());
                    String newDepartment   = nzOr(detail.getBizChrDeptNm(), "담당부처 미제공");
                    String newOrg          = org;
                    String newEligibleUser = nzOr(detail.getSprtTrgtCn(), "대상자 정보 미제공");
                    String newDetailInfo   = detailInfo;

                    boolean updated = false;

                    // 주요 필드 변경 여부
                    boolean needsMainUpdate =
                            !safeEq(welfare.getSummary(),      newSummary)      ||
                                    !safeEq(welfare.getContent(),      newContent)      ||
                                    !safeEq(welfare.getServLink(),     newServLink)     ||
                                    !safeEq(welfare.getDepartment(),   newDepartment)   ||
                                    !safeEq(welfare.getOrg(),          newOrg)          ||
                                    !safeEq(welfare.getEligibleUser(), newEligibleUser) ||
                                    !safeEq(welfare.getDetailInfo(),   newDetailInfo);

                    if (needsMainUpdate) {
                        welfare.update(
                                newSummary,
                                newContent,
                                newServLink,
                                newDepartment,
                                newOrg,
                                newEligibleUser,
                                newDetailInfo
                        );
                        updated = true;
                    }

                    // 카테고리
                    welfare.updateCategories(new HashSet<>(life),
                            new HashSet<>(hh),
                            new HashSet<>(it));
                    updated = true;

                    // 지역
                    if (!safeEq(welfare.getCtpvNm(), item.getCtpvNm()) ||
                            !safeEq(welfare.getSggNm(), item.getSggNm())) {
                        welfare.updateRegion(item.getCtpvNm(), item.getSggNm());
                        updated = true;
                    }

                    if (updated) welfareRepository.save(welfare);
                }
                } catch (Exception e) {
                    skipped++;
                    log.warn("[지자체 동기화] 항목 스킵 (servId={}) - {}", item.getServId(), e.getMessage());
                }
            }

            log.info("[지자체 동기화] {} 페이지 처리 완료 (누적 {}건)", page, processed);
            page++;
        }

        int success = processed - skipped;
        log.info("[지자체 동기화] 동기화 완료 - 시도 {}, 성공 {}, 스킵 {}", processed, success, skipped);
        return success;
    }

    private static boolean isBlank(String s) { return s == null || s.isBlank(); }

    private static String nz(String s) { return s == null ? "" : s; }

    private static String nzOr(String... candidates) {
        for (String c : candidates) if (!isBlank(c)) return c;
        return "";
    }

    private boolean safeEq(String a, String b) {
        return (a == null && b == null) || (a != null && a.equals(b));
    }
    private boolean safeEq(LocalDateTime a, LocalDateTime b) {
        return (a == null && b == null) || (a != null && a.equals(b));
    }
}
