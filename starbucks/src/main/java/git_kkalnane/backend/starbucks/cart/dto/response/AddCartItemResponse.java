package git_kkalnane.backend.starbucks.cart.dto.response;

import java.util.List;

/**
 * @param status : 응답상태
 * @param message : 응답코드
 * @param CartItemId : CartItemId
 * @param cartItem : 카트아이템
 * @param totalPrice : 총 가격
 */
public record AddCartItemResponse(
        int status,
        String message,
        Long CartItemId,
        List<AddCartItemsResponse> cartItem,
        int totalPrice
) {
}
