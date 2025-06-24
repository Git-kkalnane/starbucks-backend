package git_kkalnane.backend.starbucks.cart.dto.response;

import java.util.List;

public record ModifiedCartItemResponse(
        int HttpStatus,
        String message,
        List<ModifyCartItemsResponse> cartItem,
        int totalPrice

) {
}
