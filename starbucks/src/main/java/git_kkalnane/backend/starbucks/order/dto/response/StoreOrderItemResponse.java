package git_kkalnane.backend.starbucks.order.dto.response;

import git_kkalnane.backend.starbucks.order.domain.OrderItem;

/**
 * StoreOrderResponse에 포함될 주문 상품의 정보를 담는 DTO
 */
public record StoreOrderItemResponse(
        String itemName,
        int quantity,
        int unitPrice
) {
    public static StoreOrderItemResponse from(OrderItem orderItem) {
        return new StoreOrderItemResponse(
                orderItem.getItemName(),
                orderItem.getOrderItemQuantity(),
                orderItem.getUnitPrice()
        );
    }
}