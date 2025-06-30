package git_kkalnane.backend.starbucks.cart.dto.response;



import git_kkalnane.backend.starbucks.cart.dto.request.CheckCartItemRequest;

import java.util.List;

public record CheckCartItemResponse(
        List<CheckCartItemRequest> cartItem
) {
}
