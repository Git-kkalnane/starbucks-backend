package git_kkalnane.backend.starbucks.store.controller;

import git_kkalnane.backend.starbucks._global.success.SuccessResponse;
import git_kkalnane.backend.starbucks.store.common.success.StoreSuccessCode;
import git_kkalnane.backend.starbucks.store.dto.response.StoreDetailsResponse;
import git_kkalnane.backend.starbucks.store.dto.response.StoreListResponse;
import git_kkalnane.backend.starbucks.store.service.StoreService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 스타벅스 매장 관련 API를 제공하는 컨트롤러입니다.
 * 매장 정보 조회 기능을 담당합니다.
 *
 * @author Seongjun In
 * @version 1.0
 */
@RestController
@RequestMapping("/stores")
@RequiredArgsConstructor
@Tag(name = "Store", description = "스타벅스 매장 관련 API")
public class StoreController {

    private final StoreService storeService;

    /**
     *
     * @param storeId 조회할 매장의 고유 ID
     * @return 매장의 상세 정보를 담은 응답 DTO와 성공 응답 (HTTP 200 OK)
     */

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

    /**
     * 페이징 처리된 전체 지점 목록을 조회합니다.
     * 클라이언트에서 page, size, sort 파라미터를 통해 페이징 및 정렬을 제어할 수 있습니다.
     *
     * @param pageable 페이징 및 정렬 정보를 담은 객체
     * @return 페이징된 지점 목록 정보를 담은 ResponseEntity
     */
    @Operation(summary = "전체 지점 목록 조회", description = "페이징 처리된 전체 지점 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "지점 목록 조회 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 페이지 번호 또는 크기 요청 시"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류 발생 시")
    })
    @GetMapping
    public ResponseEntity<SuccessResponse<StoreListResponse>>getStoreList(
           @PageableDefault(size = 15, sort = "name", direction = Sort.Direction.ASC)
    Pageable pageable
    ) {
        StoreListResponse response = storeService.getStoreList(pageable);
        return ResponseEntity.ok(SuccessResponse.of(StoreSuccessCode.STORE_LIST_RETRIEVED, response));
    }

}
