package git_kkalnane.backend.starbucks.cart.dto.request;

import java.util.List;

public record AddCartItemRequest(
        Long id,
        List<AddItemsRequest> items,
        int totalPrice
) {
}
