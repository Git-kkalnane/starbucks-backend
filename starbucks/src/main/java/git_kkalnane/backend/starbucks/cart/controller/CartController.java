package git_kkalnane.backend.starbucks.cart.controller;


import git_kkalnane.backend.starbucks._global.success.SuccessResponse;
import git_kkalnane.backend.starbucks.cart.common.success.CartSuccessCode;
import git_kkalnane.backend.starbucks.cart.dto.request.CartAddItemRequest;
import git_kkalnane.backend.starbucks.cart.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
public class CartController {

    private final CartService cartService;

    @Operation(summary = "카트에 Item 추가")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "카트에 Item 추가 성공")
    })
    @PostMapping
    public ResponseEntity<SuccessResponse> addItem(@Valid @RequestBody CartAddItemRequest cartAddItemRequest) {

        Long memberId = 1L;

        cartService.addItem(cartAddItemRequest, memberId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(SuccessResponse.of(CartSuccessCode.CART_SUCCESS_CODE, cartAddItemRequest));
    }
}
