package git_kkalnane.backend.starbucks.cart.dto.request;

public record CartItemsRequest(
        Long cartItemId,
        int changeQuantity,
        int price
) {
}
