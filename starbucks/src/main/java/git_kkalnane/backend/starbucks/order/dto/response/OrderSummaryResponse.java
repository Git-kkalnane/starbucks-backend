package git_kkalnane.backend.starbucks.order.dto.response;

import git_kkalnane.backend.starbucks.order.domain.Order;
import git_kkalnane.backend.starbucks.order.domain.OrderStatus;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
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
        return OrderSummaryResponse.builder()
                .orderId(order.getId())
                .orderNumber(order.getOrderNumber())
                .storeName(order.getStore().getName())
                .orderTotalPrice(order.getOrderTotalPrice())
                .orderStatus(order.getOrderStatus())
                .orderCreatedAt(order.getCreatedAt())
                .build();
    }
}
