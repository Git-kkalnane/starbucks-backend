package git_kkalnane.backend.starbucks.cart.dto.response;

public record AddCartItemsResponse(
        Long cartItemId,
        String itemName,
        int quantity,
        int price
) {
}
