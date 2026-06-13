package com.dndn.backend.dndn.domain.welfare.api.response;

import com.dndn.backend.dndn.domain.welfare.domain.Welfare;
import lombok.Builder;
import org.springframework.data.domain.Page;

import java.util.List;

@Builder
public record WelfareListResDto(
        List<WelfareInfoResDto> welfareList,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean last
) {
    // 페이지네이션 응답
    public static WelfareListResDto from(Page<Welfare> p) {
        List<WelfareInfoResDto> content = p.getContent().stream()
                .map(WelfareInfoResDto::from)
                .toList();
        return WelfareListResDto.builder()
                .welfareList(content)
                .page(p.getNumber())
                .size(p.getSize())
                .totalElements(p.getTotalElements())
                .totalPages(p.getTotalPages())
                .last(p.isLast())
                .build();
    }
}
