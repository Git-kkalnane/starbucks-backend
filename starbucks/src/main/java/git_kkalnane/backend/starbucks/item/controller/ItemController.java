package git_kkalnane.backend.starbucks.item.controller;

import git_kkalnane.backend.starbucks._global.success.SuccessResponse;
import git_kkalnane.backend.starbucks.item.common.success.ItemSuccessCode;
import git_kkalnane.backend.starbucks.item.dto.response.ItemListResponse;
import git_kkalnane.backend.starbucks.item.service.ItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 아이템(상품) 관련 API를 제공하는 컨트롤러 클래스입니다.
 * 주로 아이템 목록 조회 기능을 담당합니다.
 *
 * @author Seongjun In
 * @version 1.0
 */
@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
@Tag(name = "Item", description = "스타벅스 아이템(상품) 관련 API")
public class ItemController {

    private final ItemService itemService;

    /**
     * 모든 아이템(음료, 디저트)의 목록을 조회합니다.
     *
     * @return {@link ItemListResponse} 형식의 아이템 목록과 성공 응답.
     */
    @Operation(
            summary = "아이템 목록 조회",
            description = "시스템에 등록된 모든 음료 및 디저트 아이템의 목록을 조회합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "아이템 목록 조회 성공"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @GetMapping
    public ResponseEntity<SuccessResponse<ItemListResponse>> getItems() {
        ItemListResponse response = itemService.getOverallItems();
        return ResponseEntity.ok(SuccessResponse.of(ItemSuccessCode.ITEM_LIST_RETRIEVED, response));
    }
}
