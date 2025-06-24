package git_kkalnane.backend.starbucks.cart.dto.request;

import java.util.List;

public record AddCartItemRequest(
        Long id,
        List<ItemsRequest> items,
        int totalPrice
) {
}
