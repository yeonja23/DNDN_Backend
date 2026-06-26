package com.dndn.backend.dndn.domain.welfare.application.sync;

import com.dndn.backend.dndn.domain.welfare.domain.Welfare;
import com.dndn.backend.dndn.domain.welfare.domain.repository.WelfareRepository;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

// 중앙/지자체 복지 동기화의 공통 흐름을 담는 템플릿.
@Slf4j
public abstract class AbstractWelfareSyncService<I, D> {

    protected final WelfareRepository welfareRepository;

    protected AbstractWelfareSyncService(WelfareRepository welfareRepository) {
        this.welfareRepository = welfareRepository;
    }

    public int sync(int maxCount) {
        int numOfRows = Math.min(maxCount, 100);
        int page = 1;
        int processed = 0;
        int skipped = 0;

        while (processed < maxCount) {
            List<I> items = fetchListItems(page, numOfRows);
            if (items.isEmpty()) {
                log.info("[{}] {}페이지 데이터 없음. 종료", label(), page);
                break;
            }

            log.info("[{}] {}페이지, {}건 처리 시작", label(), page, items.size());

            for (I item : items) {
                if (processed >= maxCount) break;
                processed++;
                try {
                    String servId = servIdOf(item);
                    if (isBlank(servId)) continue;

                    D detail = fetchDetail(servId);
                    if (detail == null) {
                        log.warn("[{}] 상세 조회 실패 servId={}", label(), servId);
                        continue;
                    }

                    upsert(mapToData(servId, item, detail));
                } catch (Exception e) {
                    skipped++;
                    log.warn("[{}] 항목 스킵 (servId={}) - {}", label(), servIdOf(item), e.getMessage());
                }
            }

            log.info("[{}] {}페이지 처리 완료 (누적 {}건)", label(), page, processed);
            page++;
        }

        int success = processed - skipped;
        log.info("[{}] 동기화 완료 - 시도 {}, 성공 {}, 스킵 {}", label(), processed, success, skipped);
        return success;
    }

    // servId로 기존 레코드를 찾아 없으면 저장, 있으면 갱신한다.
    private void upsert(WelfareData data) {
        Welfare existing = welfareRepository.findByServId(data.servId()).orElse(null);

        if (existing == null) {
            welfareRepository.save(toEntity(data));
            return;
        }

        existing.update(data.summary(), data.content(), data.servLink(),
                data.department(), data.org(), data.eligibleUser(), data.detailInfo());
        existing.updateCategories(data.lifeCycles(), data.householdTypes(), data.interestTopics());
        existing.updateRegion(data.ctpvNm(), data.sggNm());
        welfareRepository.save(existing);
    }

    private Welfare toEntity(WelfareData d) {
        return Welfare.builder()
                .servId(d.servId())
                .title(d.title())
                .summary(d.summary())
                .content(d.content())
                .servLink(d.servLink())
                .ctpvNm(d.ctpvNm())
                .sggNm(d.sggNm())
                .eligibleUser(d.eligibleUser())
                .detailInfo(d.detailInfo())
                .department(d.department())
                .org(d.org())
                .sourceType(d.sourceType())
                .lifeCycles(d.lifeCycles())
                .householdTypes(d.householdTypes())
                .interestTopics(d.interestTopics())
                .build();
    }

    /* ---------- 소스별 hook ---------- */

    // 로그 라벨 (예: "중앙복지", "지자체")
    protected abstract String label();

    protected abstract List<I> fetchListItems(int page, int numOfRows);

    protected abstract String servIdOf(I item);

    protected abstract D fetchDetail(String servId);

    protected abstract WelfareData mapToData(String servId, I item, D detail);

    /* ---------- 공통 헬퍼 ---------- */

    protected static String nz(String s) {
        return s == null ? "" : s;
    }

    protected static String nzOr(String... candidates) {
        for (String c : candidates) if (!isBlank(c)) return c;
        return "";
    }

    protected static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
