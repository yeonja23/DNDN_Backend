package com.dndn.backend.dndn.domain.welfare.application;

import com.dndn.backend.dndn.domain.category.application.CategoryService;
import com.dndn.backend.dndn.domain.category.domain.Category;
import com.dndn.backend.dndn.domain.category.domain.enums.HouseholdType;
import com.dndn.backend.dndn.domain.category.domain.enums.InterestTopic;
import com.dndn.backend.dndn.domain.category.domain.enums.LifeCycle;
import com.dndn.backend.dndn.domain.welfare.domain.Welfare;
import com.dndn.backend.dndn.domain.welfare.domain.enums.SourceType;
import com.dndn.backend.dndn.domain.welfare.domain.repository.WelfareRepository;
import com.dndn.backend.dndn.domain.welfareOpenApi.central.client.CentralWelfareClient;
import com.dndn.backend.dndn.domain.welfareOpenApi.central.dto.response.CentralDetailResDto;
import com.dndn.backend.dndn.domain.welfareOpenApi.central.dto.response.CentralListResDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Objects;
import java.util.Optional;

import static com.dndn.backend.dndn.domain.category.util.CategoryParserUtils.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class CentralWelfareSyncService {

    private final WelfareRepository welfareRepository;
    private final CentralWelfareClient centralClient;
    private final CategoryService categoryService;

    public int syncCentralWelfareData(int maxCount) {
        int numOfRows = Math.min(maxCount, 100);
        int page = 1;
        int processed = 0;
        int skipped = 0;

        while (processed < maxCount) {
            CentralListResDto list = centralClient.getWelfareList(page, numOfRows);
            log.info("[동기화] {}페이지 응답 도착", page);

            // ✅ 지자체 스타일: null 또는 리스트 비었으면 종료
            if (list == null || list.getServList() == null || list.getServList().isEmpty()) {
                log.info("serviceItems 비어있음. 종료");
                break;
            }

            List<CentralListResDto.ServiceItem> items = list.getServList();
            log.info("[동기화] {}개의 서비스 처리 시작", items.size());

            for (CentralListResDto.ServiceItem item : items) {
                if (processed >= maxCount) break;
                processed++;
                try {
                String servId = item.getServId();
                if (isBlank(servId)) continue;

                CentralDetailResDto dtl = centralClient.getWelfareDetail(servId);
                if (dtl == null) {
                    log.warn("상세 null (servId={})", servId);
                    continue;
                }

                // ✅ 상세도 resultCode 체크 제거
                String title    = nzOr(dtl.getServNm(), item.getServNm(), "제목 미제공");
                String outline  = nzOr(dtl.getWlfareInfoOutlCn(), item.getServDgst(), "내용 미제공");
                String summary  = nzOr(item.getServDgst(), dtl.getWlfareInfoOutlCn(), "요약 미제공");
                String link     = nz(item.getServDtlLink());
                String eligible = nzOr(dtl.getTgtrDtlCn(), "대상자 정보 미제공");
                String submit   = nzOr(dtl.getAlwServCn(), "제출서류 정보 미제공");
                String dept     = nzOr(item.getJurMnofNm(), dtl.getJurMnofNm(), "담당부처 미제공");
                String org      = nzOr(item.getJurOrgNm(), dtl.getJurOrgNm(), "담당기관 미제공");
                String detailInfo = Optional.ofNullable(dtl.getBasfrmList())
                        .orElse(List.of())
                        .stream()
                        .map(CentralDetailResDto.ServDetail::getServSeDetailLink)
                        .filter(s -> s != null && !s.isBlank())
                        .findFirst()
                        .orElse("상세정보링크 미제공");

                // 카테고리 매핑
                List<LifeCycle> lifeCycles     = parseLifeCycles(nzOr(item.getLifeArray(), dtl.getLifeArray()));
                List<HouseholdType> household  = parseHouseholdTypes(nzOr(item.getTrgterIndvdlArray(), dtl.getTrgterIndvdlArray()));
                List<InterestTopic> interests  = parseInterestTopics(nzOr(item.getIntrsThemaArray(), dtl.getIntrsThemaArray()));
                Category category = categoryService.findOrCreateCategory(lifeCycles, household, interests);
                if (category == null) {
                    log.warn("카테고리 null (servId={})", servId);
                    continue;
                }

                Welfare welfare = welfareRepository.findByServId(servId).orElse(null);

                if (welfare == null) {
                    Welfare newWelfare = Welfare.builder()
                            .servId(servId)
                            .title(title)
                            .summary(summary)
                            .content(outline)
                            .servLink(link)
                            .ctpvNm("전국")
                            .sggNm("전국")
                            .eligibleUser(eligible)
                            .department(dept)
                            .org(org)
                            .detailInfo(detailInfo)
                            .sourceType(SourceType.CENTRAL)
                            .category(category)
                            .build();
                    welfareRepository.save(newWelfare);
                } else {
                    boolean updated = false;

                    if (!Objects.equals(welfare.getContent(), outline) ||
                            !Objects.equals(welfare.getServLink(), link) ||
                            !Objects.equals(welfare.getEligibleUser(), eligible)) {

                        welfare.update(
                                summary,
                                outline,
                                link,
                                dept,
                                org,
                                eligible,
                                detailInfo
                        );

                        updated = true;
                    }

                    if (welfare.getCategory() == null ||
                            !Objects.equals(welfare.getCategory().getId(), category.getId())) {
                        welfare.updateCategory(category);
                        updated = true;
                    }

                    if (isBlank(welfare.getCtpvNm()) || isBlank(welfare.getSggNm())) {
                        welfare.updateRegion("전국", "전국");
                        updated = true;
                    }

                    if (updated) welfareRepository.save(welfare);
                }
                } catch (Exception e) {
                    skipped++;
                    log.warn("[중앙복지] 항목 스킵 (servId={}) - {}", item.getServId(), e.getMessage());
                }
            }

            log.info("[복지 동기화] {}페이지 처리 완료 (누적 {}건)", page, processed);
            page++;
        }

        int success = processed - skipped;
        log.info("[복지 동기화] 중앙부처 동기화 완료 - 시도 {}, 성공 {}, 스킵 {}", processed, success, skipped);
        return success;
    }

    /* ---------- helpers ---------- */
    private static String nz(String s) { return s == null ? "" : s; }

    private static String nzOr(String... candidates) {
        for (String c : candidates) if (!isBlank(c)) return c;
        return "";
    }

    private static boolean isBlank(String s) { return s == null || s.isBlank(); }
}
