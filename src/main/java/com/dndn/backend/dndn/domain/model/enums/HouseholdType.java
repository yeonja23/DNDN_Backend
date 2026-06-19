package com.dndn.backend.dndn.domain.model.enums;

import lombok.Getter;

@Getter
public enum HouseholdType {

    MULTICULTURAL("다문화·탈북민"),
    MULTI_CHILD("다자녀"),
    PATRIOT("보훈대상자"),
    DISABLED("장애인"),
    LOW_INCOME("저소득"),
    SINGLE_PARENT("한부모·조손");

    private final String kor;

    HouseholdType(String kor) {
        this.kor = kor;
    }
}
