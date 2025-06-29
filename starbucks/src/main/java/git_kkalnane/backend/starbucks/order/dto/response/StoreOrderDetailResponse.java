package git_kkalnane.backend.starbucks.order.dto.response;

import git_kkalnane.backend.starbucks.order.domain.Order;
import git_kkalnane.backend.starbucks.order.domain.OrderItem;
import git_kkalnane.backend.starbucks.order.domain.OrderStatus;
import git_kkalnane.backend.starbucks.order.domain.PickupType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 매장(Store) 관점에서 특정 주문의 상세 정보를 담는 응답 DTO(Data Transfer Object).
 * Java Record를 사용하여 불변 데이터 객체로 정의되었습니다.
 *
 * @param id                      주문의 고유 ID
 * @param orderNumber             주문 번호 (e.g., A-1)
 * @param orderTotalPrice         주문 총액
 * @param orderStatus             현재 주문 상태
 * @param pickupType              픽업 유형 (매장 식사, 포장)
 * @param orderRequestMemo        주문 시 요청 사항
 * @param orderExpectedPickupTime 예상 픽업 시간
 * @param storeName               주문이 접수된 매장 이름
 * @param memberNickname          주문한 회원의 닉네임
 * @param orderItems              주문에 포함된 상품 목록 (OrderItemDetail DTO 리스트)
 */
public record StoreOrderDetailResponse (
        Long id,
        String orderNumber,
        int orderTotalPrice,
        OrderStatus orderStatus,
        PickupType pickupType,
        String orderRequestMemo,
        LocalDateTime orderExpectedPickupTime,
        String storeName,
        String memberNickname,
        List<OrderItemDetail> orderItems
){

    /**
     * 주문 상세 정보에 포함될 개별 상품의 정보를 나타내는 DTO.
     *
     * @param itemId   상품의 고유 ID (음료 또는 디저트)
     * @param itemName 주문 당시의 상품명
     * @param quantity 주문한 수량
     * @param unitPrice  주문 당시의 개당 가격
     */
    private record OrderItemDetail(
            Long itemId,
            String itemName,
            int quantity,
            int unitPrice
    ) {
        public static OrderItemDetail from(OrderItem orderItem) {
            Long itemId = orderItem.getBeverageItem() != null
                    ? orderItem.getBeverageItem().getId()
                    : orderItem.getDessertItem().getId();

            return new OrderItemDetail(
                    itemId,
                    orderItem.getItemName(),
                    orderItem.getOrderItemQuantity(),
                    orderItem.getUnitPrice()
            );
        }
    }

    /**
     * Order 엔티티를 StoreOrderDetailResponse DTO로 변환하는 정적 팩토리 메서드.
     *
     * @param order 변환할 주문 엔티티
     * @return 변환된 StoreOrderDetailResponse DTO
     */
    public static StoreOrderDetailResponse from(Order order) {

        return new StoreOrderDetailResponse(
                order.getId(),
                order.getOrderNumber(),
                order.getOrderTotalPrice(),
                order.getOrderStatus(),
                order.getPickupType(),
                order.getOrderRequestMemo(),
                order.getOrderExpectedPickupTime(),
                order.getStore().getName(),
                order.getMember().getNickname(),
                order.getOrderItems().stream()
                        .map(OrderItemDetail::from)
                        .collect(Collectors.toList())
        );
    }
}
