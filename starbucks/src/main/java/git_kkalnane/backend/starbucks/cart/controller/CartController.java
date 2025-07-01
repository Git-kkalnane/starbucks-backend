package git_kkalnane.backend.starbucks.cart.controller;


import git_kkalnane.backend.starbucks._global.success.SuccessResponse;
import git_kkalnane.backend.starbucks.cart.common.success.CartSuccessCode;
import git_kkalnane.backend.starbucks.cart.dto.request.AddCartItemRequest;
import git_kkalnane.backend.starbucks.cart.dto.request.ModifyCartItemRequest;
import git_kkalnane.backend.starbucks.cart.dto.response.CheckCartItemResponse;
import git_kkalnane.backend.starbucks.cart.dto.response.ModifyCartItemResponse;
import git_kkalnane.backend.starbucks.cart.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 카트에 Item 추가 API
 * 카트에 Item 추가, 수정, 삭제 담당
 *
 * @author 최다빈
 * version 1.0
 */

@RestController
@RequestMapping(value = "/carts")
@RequiredArgsConstructor
@Tag(name = "Cart", description = "장바구니 관련 API")
public class CartController {

    private final CartService cartService;

    @Operation(
            summary = "장바구니에 상품 추가",
            description = "로그인한 사용자의 장바구니에 상품을 추가합니다. 이미 담긴 동일한 상품(옵션 포함)의 경우 수량이 더해집니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "장바구니 상품 추가 성공"),
            @ApiResponse(responseCode = "400", description = "요청 데이터 유효성 오류 (예: 수량이 0 이하)"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 사용자 또는 상품")
    })
    @PostMapping
    public ResponseEntity<SuccessResponse> addItem(
            @Parameter(description = "추가할 상품의 ID와 수량, 옵션 등의 정보") @RequestBody AddCartItemRequest addCartItemRequest,
            @RequestAttribute(name = "memberId") Long memberId) {

        cartService.addItem(addCartItemRequest, memberId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(SuccessResponse.of(CartSuccessCode.CART_SUCCESS_CODE, addCartItemRequest));
    }

    @Operation(
            summary = "장바구니 상품 수량 수정",
            description = "로그인한 사용자의 장바구니에 담긴 특정 상품의 수량을 변경합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "장바구니 상품 수량 변경 성공"),
            @ApiResponse(responseCode = "400", description = "요청 데이터 유효성 오류 (예: 수량이 0 이하)"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 장바구니 상품이거나, 해당 사용자의 장바구니에 없는 상품일 경우")
    })
    @PutMapping
    public ResponseEntity<SuccessResponse> updateItem(
            @Parameter(description = "수정할 장바구니 상품의 ID와 새로운 수량 정보") @RequestBody ModifyCartItemRequest modifyCartItemRequest,
            @RequestAttribute(name = "memberId") Long memberId) {


        ModifyCartItemResponse modifyCartItemResponse = cartService.modifyCartItem(modifyCartItemRequest, memberId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(SuccessResponse.of(CartSuccessCode.CART_SUCCESS_MODIFIED, modifyCartItemResponse));
    }

    @Operation(
            summary = "장바구니 상품 삭제",
            description = "로그인한 사용자의 장바구니에 담긴 특정 상품을 삭제합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "장바구니 상품 삭제 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 장바구니 상품이거나, 해당 사용자의 장바구니에 없는 상품일 경우")
    })
    @DeleteMapping
    public ResponseEntity<SuccessResponse> deleteItem(
            @Parameter(description = "삭제할 장바구니 상품의 ID", required = true) @RequestParam Long cartItemId,
            @RequestAttribute(name = "memberId") Long memberId) {

        cartService.deleteCartItem(cartItemId, memberId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(SuccessResponse.of(CartSuccessCode.CART_SUCCESS_DELETED));
    }

    @Operation(
            summary = "장바구니 상품 목록 조회",
            description = "현재 로그인한 사용자의 장바구니에 담긴 모든 상품 목록과 총액 정보를 조회합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "장바구니 아이템 목록 조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    })
    @GetMapping
    public ResponseEntity<SuccessResponse> getItems(@RequestAttribute(name = "memberId") Long memberId) {

        CheckCartItemResponse checkCartItemResponse = cartService.getCartItems(memberId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(SuccessResponse.of(CartSuccessCode.CART_SUCCESS_CHECK, checkCartItemResponse));
    }
}
