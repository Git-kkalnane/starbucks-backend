package git_kkalnane.backend.starbucks.cart.dto.request;

import java.util.List;

public record ModifyCartItemRequest(
        Long id,
        List<ModifyCartItemsRequest> cartItems
) {
}
