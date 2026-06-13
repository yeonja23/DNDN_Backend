package com.dndn.backend.dndn.domain.welfare.api;

import com.dndn.backend.dndn.domain.welfare.application.CentralWelfareSyncService;
import com.dndn.backend.dndn.domain.welfare.application.LocalWelfareSyncService;
import com.dndn.backend.dndn.global.common.response.BaseResponse;
import com.dndn.backend.dndn.global.error.code.status.SuccessStatus;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// [개발자용] 복지 데이터를 OpenAPI에서 가져와 DB에 적재하는 수동 트리거.
// 테스트 편의용이며 운영 노출 대상이 아님.
@RestController
@RequestMapping("/welfare/sync")
@RequiredArgsConstructor
public class WelfareSyncController {

    private final CentralWelfareSyncService centralWelfareSyncService;
    private final LocalWelfareSyncService localWelfareSyncService;

    @PostMapping("/central")
    @Operation(
            summary = "[개발자용] 중앙부처 복지 소량 동기화",
            description = "count 건수만큼만 OpenAPI에서 가져와 DB에 적재합니다. 일일 호출 한도 보호용 테스트 트리거."
    )
    public BaseResponse<String> syncCentral(@RequestParam(defaultValue = "5") int count) {
        int saved = centralWelfareSyncService.syncCentralWelfareData(count);
        return BaseResponse.onSuccess(SuccessStatus.OK, "중앙부처 동기화 완료 - 성공 " + saved + "건");
    }

    @PostMapping("/local")
    @Operation(
            summary = "[개발자용] 지자체 복지 소량 동기화",
            description = "count 건수만큼만 OpenAPI에서 가져와 DB에 적재합니다. 일일 호출 한도 보호용 테스트 트리거."
    )
    public BaseResponse<String> syncLocal(@RequestParam(defaultValue = "5") int count) {
        int saved = localWelfareSyncService.syncLocalWelfareData(count);
        return BaseResponse.onSuccess(SuccessStatus.OK, "지자체 동기화 완료 - 성공 " + saved + "건");
    }
}
