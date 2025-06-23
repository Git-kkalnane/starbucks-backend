package git_kkalnane.backend.starbucks.cart.dto.request;

import java.util.List;

public record CartAddItemRequest(
        Long id,
        List<ItemsRequest> items,
        int totalPrice
) {
}
