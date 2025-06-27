package git_kkalnane.backend.starbucks.cart.dto.response;

import java.util.List;

public record ModifyCartItemResponse(

        List<ModifyCartItemsResponse> cartItem,
        int totalPrice

) {
}
