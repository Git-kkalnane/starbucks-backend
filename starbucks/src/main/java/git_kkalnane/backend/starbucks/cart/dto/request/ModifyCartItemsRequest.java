package git_kkalnane.backend.starbucks.cart.dto.request;

public record ModifyCartItemsRequest(
        Long cartItemId,
        int changeQuantity,
        int price
) {
}
