package git_kkalnane.backend.starbucks.cart.dto.response;

import java.util.List;

/**
 * @param CartItemId : CartItemId
 * @param cartItem : 카트아이템
 * @param totalPrice : 총 가격
 */
public record AddCartItemResponse(
        Long CartItemId,
        List<AddCartItemsResponse> cartItem,
        int totalPrice
) {
}
