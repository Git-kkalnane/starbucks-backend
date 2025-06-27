package git_kkalnane.backend.starbucks.order.dto.response;

import git_kkalnane.backend.starbucks.order.domain.Order;
import git_kkalnane.backend.starbucks.order.domain.OrderStatus;
import git_kkalnane.backend.starbucks.order.domain.PickupType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 매장용 현재 주문 목록의 개별 주문 정보를 담는 DTO
 */
public record StoreOrderResponse(
        Long id,
        String orderNumber,
        int orderTotalPrice,
        OrderStatus orderStatus,
        PickupType pickupType,
        String orderRequestMemo,
        LocalDateTime orderExpectedPickupTime,
        String storeName,
        String memberNickname,
        List<StoreOrderItemResponse> orderItems
) {
    public static StoreOrderResponse from(Order order) {
        List<StoreOrderItemResponse> itemDetails = order.getOrderItems().stream()
                .map(StoreOrderItemResponse::from)
                .collect(Collectors.toList());

        return new StoreOrderResponse(
                order.getId(),
                order.getOrderNumber(),
                order.getOrderTotalPrice(),
                order.getOrderStatus(),
                order.getPickupType(),
                order.getOrderRequestMemo(),
                order.getOrderExpectedPickupTime(),
                order.getStore().getName(),
                order.getMember().getNickname(),
                itemDetails
        );
    }
}