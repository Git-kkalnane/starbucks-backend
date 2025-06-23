package git_kkalnane.backend.starbucks.cart.dto.response;

import git_kkalnane.backend.starbucks.cart.domain.CartItem;

import java.util.List;

public record CartAddItemResponse(
        int status,
        String message,
        Long CartItemId,
        List<CartItem> cartItem
) {
}
