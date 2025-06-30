package git_kkalnane.backend.starbucks.order.dto.response;

import git_kkalnane.backend.starbucks.order.domain.Order;
import git_kkalnane.backend.starbucks.order.domain.OrderItem;
import git_kkalnane.backend.starbucks.order.domain.OrderStatus;
import git_kkalnane.backend.starbucks.order.domain.PickupType;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 매장의 과거 주문 내역 목록에 표시될 단일 주문의 상세 정보를 담는 DTO.
 *
 * @param id                    주문 ID
 * @param orderNumber             주문 번호
 * @param orderTotalPrice         주문 총액
 * @param orderStatus             주문 상태 (COMPLETED 또는 CANCELED)
 * @param pickupType              픽업 유형
 * @param orderRequestMemo        요청 사항
 * @param orderCompletedTime      주문 완료/취소 시간
 * @param storeName               매장 이름
 * @param memberNickname          주문자 닉네임
 * @param orderItems              주문 상품 요약 목록
 */
public record StoreOrderHistoryResponse(
        Long id,
        String orderNumber,
        int orderTotalPrice,
        OrderStatus orderStatus,
        PickupType pickupType,
        String orderRequestMemo,
        LocalDateTime orderCompletedTime,
        String storeName,
        String memberNickname,
        List<OrderItemSummary> orderItems
) {

    /**
     * 주문에 포함된 개별 상품의 요약 정보를 나타내는 내부 DTO.
     *
     * @param itemName 상품명
     * @param quantity 수량
     */
    private record OrderItemSummary(
            String itemName,
            int quantity
    ) {
        public static OrderItemSummary from(OrderItem orderItem) {
            return new OrderItemSummary(
                    orderItem.getItemName(),
                    orderItem.getOrderItemQuantity()
            );
        }
    }
    public static StoreOrderHistoryResponse from(Order order) {
        List<OrderItemSummary> itemSummaries = order.getOrderItems().stream()
                .map(OrderItemSummary::from)
                .toList();

        return new StoreOrderHistoryResponse(
                order.getId(),
                order.getOrderNumber(),
                order.getOrderTotalPrice(),
                order.getOrderStatus(),
                order.getPickupType(),
                order.getOrderRequestMemo(),
                order.getModifiedAt(),
                order.getStore().getName(),
                order.getMember().getNickname(),
                itemSummaries
        );
    }
}
