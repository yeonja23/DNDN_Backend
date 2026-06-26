package com.dndn.backend.dndn.domain.welfare.application.sync;

import com.dndn.backend.dndn.domain.model.enums.HouseholdType;
import com.dndn.backend.dndn.domain.model.enums.InterestTopic;
import com.dndn.backend.dndn.domain.model.enums.LifeCycle;
import com.dndn.backend.dndn.domain.welfare.domain.enums.SourceType;
import com.dndn.backend.dndn.domain.welfare.domain.repository.WelfareRepository;
import com.dndn.backend.dndn.domain.welfare.infra.central.client.CentralWelfareClient;
import com.dndn.backend.dndn.domain.welfare.infra.central.dto.response.CentralDetailResDto;
import com.dndn.backend.dndn.domain.welfare.infra.central.dto.response.CentralListResDto;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static com.dndn.backend.dndn.domain.welfare.support.CategoryParserUtils.*;

@Service
public class CentralWelfareSyncService
        extends AbstractWelfareSyncService<CentralListResDto.ServiceItem, CentralDetailResDto> {

    private final CentralWelfareClient centralClient;

    public CentralWelfareSyncService(WelfareRepository welfareRepository, CentralWelfareClient centralClient) {
        super(welfareRepository);
        this.centralClient = centralClient;
    }

    /** 컨트롤러/스케줄러용 진입점. */
    public int syncCentralWelfareData(int maxCount) {
        return sync(maxCount);
    }

    @Override
    protected String label() {
        return "중앙복지";
    }

    @Override
    protected List<CentralListResDto.ServiceItem> fetchListItems(int page, int numOfRows) {
        CentralListResDto list = centralClient.getWelfareList(page, numOfRows);
        if (list == null || list.getServList() == null) return List.of();
        return list.getServList();
    }

    @Override
    protected String servIdOf(CentralListResDto.ServiceItem item) {
        return item.getServId();
    }

    @Override
    protected CentralDetailResDto fetchDetail(String servId) {
        return centralClient.getWelfareDetail(servId);
    }

    @Override
    protected WelfareData mapToData(String servId, CentralListResDto.ServiceItem item, CentralDetailResDto dtl) {
        // 중앙부처는 생애주기/가구상황/관심주제가 목록·상세 둘 다에 올 수 있어 목록(item) 우선
        List<LifeCycle> lifeCycles    = parseLifeCycles(nzOr(item.getLifeArray(), dtl.getLifeArray()));
        List<HouseholdType> household = parseHouseholdTypes(nzOr(item.getTrgterIndvdlArray(), dtl.getTrgterIndvdlArray()));
        List<InterestTopic> interests = parseInterestTopics(nzOr(item.getIntrsThemaArray(), dtl.getIntrsThemaArray()));

        String detailInfo = Optional.ofNullable(dtl.getBasfrmList())
                .orElse(List.of())
                .stream()
                .map(CentralDetailResDto.ServDetail::getServSeDetailLink)
                .filter(s -> s != null && !s.isBlank())
                .findFirst()
                .orElse("상세정보링크 미제공");

        return WelfareData.builder()
                .servId(servId)
                .title(nzOr(dtl.getServNm(), item.getServNm(), "제목 미제공"))
                .summary(nzOr(item.getServDgst(), dtl.getWlfareInfoOutlCn(), "요약 미제공"))
                .content(nzOr(dtl.getWlfareInfoOutlCn(), item.getServDgst(), "내용 미제공"))
                .servLink(nz(item.getServDtlLink()))
                .ctpvNm("전국")
                .sggNm("전국")
                .eligibleUser(nzOr(dtl.getTgtrDtlCn(), "대상자 정보 미제공"))
                .detailInfo(detailInfo)
                .department(nzOr(item.getJurMnofNm(), dtl.getJurMnofNm(), "담당부처 미제공"))
                .org(nzOr(item.getJurOrgNm(), dtl.getJurOrgNm(), "담당기관 미제공"))
                .sourceType(SourceType.CENTRAL)
                .lifeCycles(new HashSet<>(lifeCycles))
                .householdTypes(new HashSet<>(household))
                .interestTopics(new HashSet<>(interests))
                .build();
    }
}
