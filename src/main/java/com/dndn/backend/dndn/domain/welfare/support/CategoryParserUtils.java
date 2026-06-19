package com.dndn.backend.dndn.domain.welfare.support;

import com.dndn.backend.dndn.domain.model.enums.HouseholdType;
import com.dndn.backend.dndn.domain.model.enums.InterestTopic;
import com.dndn.backend.dndn.domain.model.enums.LifeCycle;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CategoryParserUtils {

    private CategoryParserUtils() { }

    public static List<LifeCycle> parseLifeCycles(String raw) {
        return parse(raw, LifeCycle.values(), LifeCycle::getKor);
    }

    public static List<HouseholdType> parseHouseholdTypes(String raw) {
        return parse(raw, HouseholdType.values(), HouseholdType::getKor);
    }

    public static List<InterestTopic> parseInterestTopics(String raw) {
        return parse(raw, InterestTopic.values(), InterestTopic::getKor);
    }

    private static <E> List<E> parse(String raw, E[] values, Function<E, String> korFn) {
        if (raw == null || raw.isBlank()) return List.of();

        Map<String, E> lookup = new HashMap<>();
        for (E v : values) {
            lookup.put(normalize(korFn.apply(v)), v);
        }

        return Arrays.stream(raw.split(","))
                .map(CategoryParserUtils::normalize)
                .filter(s -> !s.isEmpty())
                .map(lookup::get)          // 매칭 안 되면 null
                .filter(Objects::nonNull)  // 모르는 값은 버림 (항목 전체 스킵 X)
                .distinct()
                .collect(Collectors.toList());
    }

    // 공백·가운뎃점 등 구분기호 제거 → "임신 · 출산", "문화▪여가" 같은 변형에 견고
    private static String normalize(String s) {
        if (s == null) return "";
        return s.replaceAll("[^가-힣A-Za-z0-9]", "");
    }
}
