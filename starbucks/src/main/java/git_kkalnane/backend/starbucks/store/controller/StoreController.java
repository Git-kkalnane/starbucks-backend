package git_kkalnane.backend.starbucks.store.controller;

import git_kkalnane.backend.starbucks.global.success.SuccessResponse;
import git_kkalnane.backend.starbucks.store.common.success.StoreSuccessCode;
import git_kkalnane.backend.starbucks.store.dto.request.UpdateCrowdLevelRequest;
import git_kkalnane.backend.starbucks.store.dto.response.StoreDetailsResponse;
import git_kkalnane.backend.starbucks.store.service.StoreService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/stores")
@RequiredArgsConstructor
@Tag(name = "Store", description = "스타벅스 매장 관련 API")
public class StoreController {

    private final StoreService storeService;

    @Operation(
            summary = "지점 상세 정보 조회",
            description = "특정 지점의 상세 정보를 조회합니다. ID를 통해 해당 지점의 이름, 주소, 전화번호, 운영시간, 편의시설, 혼잡도 등을 반환"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "지점 상제 정보 조회 성공 "),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 ID"),
            @ApiResponse(responseCode = "404", description = "해당 ID의 지점을 찾을 수 없음")
    })

    @GetMapping("/{storeId}")
    public ResponseEntity<SuccessResponse<StoreDetailsResponse>> getStoreDetails(@PathVariable Long storeId) {

        StoreDetailsResponse response = storeService.getStoreDetails(storeId);

        return ResponseEntity.ok(SuccessResponse.of(StoreSuccessCode.STORE_DETAIL_RETRIEVED, response));
    }


}
