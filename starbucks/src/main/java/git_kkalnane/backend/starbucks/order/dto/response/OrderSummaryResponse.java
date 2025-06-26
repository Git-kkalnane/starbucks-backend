package git_kkalnane.backend.starbucks.order.dto.response;

import git_kkalnane.backend.starbucks.order.domain.Order;
import git_kkalnane.backend.starbucks.order.domain.OrderStatus;

import java.time.LocalDateTime;

public record OrderSummaryResponse(
        Long orderId,
        String orderNumber,
        String storeName,
        int orderTotalPrice,
        OrderStatus orderStatus,
        LocalDateTime orderCreatedAt
) {
    /**
     * Order 엔티티를 OrderSummaryResponse DTO로 변환하는 메서드.
     * @param order 변환할 Order 엔티티 객체
     * @return OrderSummaryResponse DTO 객체
     */
    public static OrderSummaryResponse from(Order order) {
        return new OrderSummaryResponse(
                order.getId(),
                order.getOrderNumber(),
                order.getStore().getName(),
                order.getOrderTotalPrice(),
                order.getOrderStatus(),
                order.getCreatedAt()
        );

    }
}
