package git_kkalnane.backend.starbucks.cart.controller;


import git_kkalnane.backend.starbucks._global.success.SuccessResponse;
import git_kkalnane.backend.starbucks.cart.common.success.CartSuccessCode;
import git_kkalnane.backend.starbucks.cart.dto.request.AddCartItemRequest;
import git_kkalnane.backend.starbucks.cart.dto.request.ModifyCartItemRequest;
import git_kkalnane.backend.starbucks.cart.dto.response.ModifyCartItemResponse;
import git_kkalnane.backend.starbucks.cart.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
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
public class CartController {

    private final CartService cartService;

    @Operation(summary = "카트에 Item 추가")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "카트에 Item 추가 성공")
    })
    @PostMapping
    public ResponseEntity<SuccessResponse> addItem(@Valid @RequestBody AddCartItemRequest addCartItemRequest) {

        Long memberId = 1L;

        cartService.addItem(addCartItemRequest, memberId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(SuccessResponse.of(CartSuccessCode.CART_SUCCESS_CODE, addCartItemRequest));
    }

    @Operation(summary = "카트 Item 수량 수정")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "카트 Item 수정 성공")
    })
    @PutMapping
    public ResponseEntity<SuccessResponse> updateItem(@RequestBody ModifyCartItemRequest modifyCartItemRequest) {
        Long memberId = 1L;

        ModifyCartItemResponse modifyCartItemResponse = cartService.modifiyCartItem(modifyCartItemRequest, memberId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(SuccessResponse.of(CartSuccessCode.CART_SUCCESS_MODIFIED, modifyCartItemResponse));
    }

    @Operation(summary = "카트 Item 삭제")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "카트 Item 삭제")
    })
    @DeleteMapping
    public ResponseEntity<SuccessResponse> deleteItem(@RequestParam Long id) {

        Long memberId = 1L;

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(SuccessResponse.of(CartSuccessCode.CART_SUCCESS_DELETED));
    }
}
