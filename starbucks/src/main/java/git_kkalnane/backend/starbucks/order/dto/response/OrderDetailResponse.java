package git_kkalnane.backend.starbucks.order.dto.response;

import git_kkalnane.backend.starbucks.order.domain.Order;
import git_kkalnane.backend.starbucks.order.domain.OrderStatus;
import git_kkalnane.backend.starbucks.order.domain.PickupType;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 주문 상세 조회를 위한 응답 Record 파일
 * 특정 주문의 모든 상세 정보를 클라이언트에게 제공합니다.
 */
@Builder
public record OrderDetailResponse (
        Long orderId,
        String orderNumber,
        String storeName,
        String memberName,
        int orderTotalPrice,
        OrderStatus orderStatus,
        PickupType pickupType,
        String orderRequestMemo,
        LocalDateTime orderExpectedPickupTime,
        LocalDateTime orderCreatedAt,

        List<OrderItemDetailResponse> orderItems
){
    /**
     * Order 엔티티를 OrderDetailResponse DTO로 변환하는 정적 팩토리 메서드.
     * Order 엔티티의 연관된 엔티티로부터 필요한 정보를 추출합니다.
     * @param order 변환할 Order 엔티티 객체
     * @return OrderDetailResponse DTO 객체
     */
    public static OrderDetailResponse from(Order order) {
        List<OrderItemDetailResponse> itemDetails = order.getOrderItems().stream()
                .map(OrderItemDetailResponse::from)
                .collect(Collectors.toList());

        return OrderDetailResponse.builder()
                .orderId(order.getId())
                .orderNumber(order.getOrderNumber())
                .storeName(order.getStore().getName())
                .memberName(order.getMember().getName())
                .orderTotalPrice(order.getOrderTotalPrice())
                .orderStatus(order.getOrderStatus())
                .pickupType(order.getPickupType())
                .orderRequestMemo(order.getOrderRequestMemo())
                .orderExpectedPickupTime(order.getOrderExpectedPickupTime())
                .orderCreatedAt(order.getCreatedAt())
                .orderItems(itemDetails)
                .build();
    }

}
