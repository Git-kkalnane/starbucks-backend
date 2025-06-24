package git_kkalnane.backend.starbucks.cart.dto.response;

public record ModifyCartItemsResponse(
        Long id,
        int quantity,
        int totalPrice
) {
}
