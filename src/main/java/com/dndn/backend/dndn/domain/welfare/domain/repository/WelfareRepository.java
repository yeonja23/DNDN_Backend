package com.dndn.backend.dndn.domain.welfare.domain.repository;

import com.dndn.backend.dndn.domain.model.enums.HouseholdType;
import com.dndn.backend.dndn.domain.model.enums.InterestTopic;
import com.dndn.backend.dndn.domain.model.enums.LifeCycle;
import com.dndn.backend.dndn.domain.welfare.domain.Welfare;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WelfareRepository extends JpaRepository<Welfare, Long> {

    Page<Welfare> findByTitleContaining(String keyword, Pageable pageable);
    Optional<Welfare> findByServId(String servId);

    @Query(value = """
    select distinct w
    from Welfare w
      join w.lifeCycles lc
      left join w.householdTypes hh
      left join w.interestTopics it
    where lc = :lifeCycle
      and (:householdsEmpty = true or hh in :households)
      and (:interestsEmpty = true or it in :interests)
    """,
    countQuery = """
    select count(distinct w)
    from Welfare w
      join w.lifeCycles lc
      left join w.householdTypes hh
      left join w.interestTopics it
    where lc = :lifeCycle
      and (:householdsEmpty = true or hh in :households)
      and (:interestsEmpty = true or it in :interests)
    """)
    Page<Welfare> findByCategoryFilters(
            @Param("lifeCycle") LifeCycle lifeCycle,
            @Param("households") List<HouseholdType> households,
            @Param("householdsEmpty") boolean householdsEmpty,
            @Param("interests") List<InterestTopic> interests,
            @Param("interestsEmpty") boolean interestsEmpty,
            Pageable pageable
    );

    @Query(value = """
    select distinct w
    from Welfare w
    join w.lifeCycles lc
    left join w.householdTypes hh
    left join w.interestTopics it
    where (
        lower(coalesce(w.title, '')) like concat('%', lower(:keyword), '%')
        or lower(concat(coalesce(w.content, ''), '')) like concat('%', lower(:keyword), '%')
    )
    and lc = :lifeCycle
    and (:householdsEmpty = true or hh in :households)
    and (:interestsEmpty = true or it in :interests)
    """,
    countQuery = """
    select count(distinct w)
    from Welfare w
    join w.lifeCycles lc
    left join w.householdTypes hh
    left join w.interestTopics it
    where (
        lower(coalesce(w.title, '')) like concat('%', lower(:keyword), '%')
        or lower(concat(coalesce(w.content, ''), '')) like concat('%', lower(:keyword), '%')
    )
    and lc = :lifeCycle
    and (:householdsEmpty = true or hh in :households)
    and (:interestsEmpty = true or it in :interests)
    """)
    Page<Welfare> searchByKeywordAndCategory(
            @Param("keyword") String keyword,
            @Param("lifeCycle") LifeCycle lifeCycle,
            @Param("households") List<HouseholdType> households,
            @Param("householdsEmpty") boolean householdsEmpty,
            @Param("interests") List<InterestTopic> interests,
            @Param("interestsEmpty") boolean interestsEmpty,
            Pageable pageable
    );

}
