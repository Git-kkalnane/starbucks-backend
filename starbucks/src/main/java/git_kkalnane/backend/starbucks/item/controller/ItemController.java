package git_kkalnane.backend.starbucks.item.controller;

import git_kkalnane.backend.starbucks._global.success.SuccessResponse;
import git_kkalnane.backend.starbucks.item.common.success.ItemSuccessCode;
import git_kkalnane.backend.starbucks.item.dto.response.ItemListResponse;
import git_kkalnane.backend.starbucks.item.service.ItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 아이템(상품) 관련 API를 제공하는 컨트롤러 클래스입니다.
 * 주로 아이템 목록 조회 기능을 담당합니다.
 *
 * @author Seongjun In
 * @version 1.0
 */
@RestController
@RequestMapping("/items") // 경로 일관성을 위해 /api/v1 추가
@RequiredArgsConstructor
@Tag(name = "Item", description = "스타벅스 아이템(상품) 관련 API")
public class ItemController {

    private final ItemService itemService;

    /**
     * 모든 음료(커피 포함) 아이템 목록을 조회합니다.
     */
    @Operation(summary = "전체 음료 목록 조회")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "음료 목록 조회 성공")
    })
    @GetMapping("/drinks")
    public ResponseEntity<SuccessResponse<ItemListResponse>> getDrinkItems(
            @Parameter(description = "조회할 페이지 번호 (0부터 시작)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "한 페이지당 아이템 개수") @RequestParam(defaultValue = "15") int size
    ) {
        ItemListResponse response = itemService.getDrinkItems(page, size);
        return ResponseEntity.ok(SuccessResponse.of(ItemSuccessCode.ITEM_LIST_RETRIEVED, response));
    }

    /**
     * 모든 디저트 아이템 목록을 조회합니다.
     */
    @Operation(summary = "전체 디저트 목록 조회")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "디저트 목록 조회 성공")
    })
    @GetMapping("/desserts")
    public ResponseEntity<SuccessResponse<ItemListResponse>> getDessertItems(
            @Parameter(description = "조회할 페이지 번호 (0부터 시작)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "한 페이지당 아이템 개수") @RequestParam(defaultValue = "15") int size
    ) {
        ItemListResponse response = itemService.getDessertItems(page, size);
        return ResponseEntity.ok(SuccessResponse.of(ItemSuccessCode.ITEM_LIST_RETRIEVED, response));
    }
}