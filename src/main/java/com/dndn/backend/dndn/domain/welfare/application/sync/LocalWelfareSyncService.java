package com.dndn.backend.dndn.domain.welfare.application.sync;

import com.dndn.backend.dndn.domain.model.enums.HouseholdType;
import com.dndn.backend.dndn.domain.model.enums.InterestTopic;
import com.dndn.backend.dndn.domain.model.enums.LifeCycle;
import com.dndn.backend.dndn.domain.welfare.domain.enums.SourceType;
import com.dndn.backend.dndn.domain.welfare.domain.repository.WelfareRepository;
import com.dndn.backend.dndn.domain.welfare.infra.local.client.LocalWelfareClient;
import com.dndn.backend.dndn.domain.welfare.infra.local.dto.response.LocalDetailResDto;
import com.dndn.backend.dndn.domain.welfare.infra.local.dto.response.LocalListResDto;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static com.dndn.backend.dndn.domain.welfare.support.CategoryParserUtils.*;

@Service
public class LocalWelfareSyncService
        extends AbstractWelfareSyncService<LocalListResDto.ServiceItem, LocalDetailResDto> {

    private final LocalWelfareClient localClient;

    public LocalWelfareSyncService(WelfareRepository welfareRepository, LocalWelfareClient localClient) {
        super(welfareRepository);
        this.localClient = localClient;
    }

    /** 컨트롤러/스케줄러용 진입점. */
    public int syncLocalWelfareData(int maxCount) {
        return sync(maxCount);
    }

    @Override
    protected String label() {
        return "지자체";
    }

    @Override
    protected List<LocalListResDto.ServiceItem> fetchListItems(int page, int numOfRows) {
        LocalListResDto list = localClient.getWelfareList(page, numOfRows);
        if (list == null || list.getServList() == null) return List.of();
        return list.getServList();
    }

    @Override
    protected String servIdOf(LocalListResDto.ServiceItem item) {
        return item.getServId();
    }

    @Override
    protected LocalDetailResDto fetchDetail(String servId) {
        return localClient.getWelfareDetail(servId);
    }

    @Override
    protected WelfareData mapToData(String servId, LocalListResDto.ServiceItem item, LocalDetailResDto detail) {
        // 지자체는 생애주기/가구상황이 목록엔 없고 상세에만 오므로 상세(detail) 우선
        List<LifeCycle> life     = parseLifeCycles(nzOr(detail.getLifeNmArray(), item.getLifeNmArray()));
        List<HouseholdType> hh   = parseHouseholdTypes(nzOr(detail.getTrgterIndvdlNmArray(), item.getTrgterIndvdlNmArray()));
        List<InterestTopic> it   = parseInterestTopics(nzOr(detail.getIntrsThemaNmArray(), item.getIntrsThemaNmArray()));

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

        return WelfareData.builder()
                .servId(servId)
                .title(nzOr(detail.getServNm(), item.getServNm(), "제목 미제공"))
                .summary(nzOr(detail.getServDgst(), "요약 미제공"))
                .content(nzOr(detail.getAlwServCn(), "내용 미제공"))
                .servLink(nz(item.getServDtlLink()))
                .ctpvNm(item.getCtpvNm())
                .sggNm(item.getSggNm())
                .eligibleUser(nzOr(detail.getSprtTrgtCn(), "대상자 정보 미제공"))
                .detailInfo(detailInfo)
                .department(nzOr(detail.getBizChrDeptNm(), "담당부처 미제공"))
                .org(org)
                .sourceType(SourceType.LOCAL)
                .lifeCycles(new HashSet<>(life))
                .householdTypes(new HashSet<>(hh))
                .interestTopics(new HashSet<>(it))
                .build();
    }
}
