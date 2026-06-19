package com.dndn.backend.dndn.domain.welfare.domain;

import com.dndn.backend.dndn.domain.model.enums.HouseholdType;
import com.dndn.backend.dndn.domain.model.enums.InterestTopic;
import com.dndn.backend.dndn.domain.model.enums.LifeCycle;
import com.dndn.backend.dndn.domain.model.entity.BaseEntity;
import com.dndn.backend.dndn.domain.welfare.domain.enums.SourceType;
import jakarta.persistence.*;
import org.hibernate.annotations.BatchSize;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Welfare extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "service_id", nullable = false, unique = true)
    private String servId;

    @Column(nullable = false)
    private String title;

    // 본문은 TEXT/CLOB로
    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    // 상세 링크
    @Column(name = "service_link")
    private String servLink;

    // 시도명
    @Column(name = "ctpv_nm", nullable = true)
    private String ctpvNm;

    // 시군구명
    @Column(name = "sgg_nm", nullable = true)
    private String sggNm;

    // 대상자 설명
    @Lob
    @Column(name = "eligible_user", nullable = false, columnDefinition = "TEXT")
    private String eligibleUser;

    // 상세 정보
    @Column(name = "detail_info", length = 1000)
    private String detailInfo;

    // 담당부처
    @Column(name = "department", length = 200, nullable = true)
    private String department;

    // 담당기관(조직)
    @Column(name = "org", length = 200, nullable = true)
    private String org;

    // 요약 정보
    @Column(name = "summary", length = 1000)
    private String summary;


    // 생애주기
    @ElementCollection(fetch = FetchType.LAZY)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "welfare_life_cycle", joinColumns = @JoinColumn(name = "welfare_id"))
    @Column(name = "life_cycle")
    @BatchSize(size = 100)
    private Set<LifeCycle> lifeCycles = new HashSet<>();

    // 가구유형
    @ElementCollection(fetch = FetchType.LAZY)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "welfare_household_type", joinColumns = @JoinColumn(name = "welfare_id"))
    @Column(name = "household_type")
    @BatchSize(size = 100)
    private Set<HouseholdType> householdTypes = new HashSet<>();

    // 관심주제
    @ElementCollection(fetch = FetchType.LAZY)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "welfare_interest_topic", joinColumns = @JoinColumn(name = "welfare_id"))
    @Column(name = "interest_topic")
    @BatchSize(size = 100)
    private Set<InterestTopic> interestTopics = new HashSet<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false, length = 20)
    private SourceType sourceType;

    @Builder
    private Welfare(String servId, String title, String summary , String content, String servLink,
                    String ctpvNm, String sggNm, String eligibleUser,
                    String detailInfo, String department, String org,
                    SourceType sourceType,
                    Set<LifeCycle> lifeCycles, Set<HouseholdType> householdTypes, Set<InterestTopic> interestTopics) {
        this.servId = servId;
        this.title = title;
        this.summary = summary;
        this.content = content;
        this.servLink = servLink;
        this.detailInfo = detailInfo;
        this.ctpvNm = ctpvNm;
        this.sggNm = sggNm;
        this.eligibleUser = eligibleUser;
        this.department = department;
        this.org = org;
        this.sourceType = sourceType;
        this.lifeCycles = lifeCycles != null ? lifeCycles : new HashSet<>();
        this.householdTypes = householdTypes != null ? householdTypes : new HashSet<>();
        this.interestTopics = interestTopics != null ? interestTopics : new HashSet<>();
    }

    public void updateCategories(Set<LifeCycle> lifeCycles,
                                 Set<HouseholdType> householdTypes,
                                 Set<InterestTopic> interestTopics) {
        this.lifeCycles = lifeCycles;
        this.householdTypes = householdTypes;
        this.interestTopics = interestTopics;
    }

    public void updateRegion(String ctpvNm, String sggNm) {
        this.ctpvNm = ctpvNm;
        this.sggNm = sggNm;
    }

    public void update(String summary, String content, String servLink,
                       String department, String org,
                       String eligibleUser, String detailInfo) {
        this.summary = summary;
        this.content = content;
        this.servLink = servLink;
        this.department = department;
        this.org = org;
        this.eligibleUser = eligibleUser;
        this.detailInfo = detailInfo;
    }

}
