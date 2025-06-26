package git_kkalnane.backend.starbucks.cart.dto.response;

import java.util.List;

public record ModifyCartItemResponse(
        int HttpStatus,
        String message,
        List<ModifyCartItemsResponse> cartItem,
        int totalPrice

) {
}
