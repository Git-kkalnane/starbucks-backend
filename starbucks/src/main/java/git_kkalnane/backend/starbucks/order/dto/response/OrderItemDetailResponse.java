package git_kkalnane.backend.starbucks.order.dto.response;

import git_kkalnane.backend.starbucks.item.domain.ItemType;
import git_kkalnane.backend.starbucks.order.common.exception.OrderErrorCode;
import git_kkalnane.backend.starbucks.order.common.exception.OrderException;
import git_kkalnane.backend.starbucks.order.domain.OrderItem;
import lombok.Builder;

@Builder
public record OrderItemDetailResponse (
        Long orderItemId,
        String itemName,
        int orderItemQuantity,
        int unitPrice,
        ItemType itemType,
        Long originalItemId
){
    /**
     * OrderItem 엔티티를 OrderItemDetailResponse DTO로 변환하는 정적 팩토리 메서드.
     * @param orderItem 변환할 OrderItem 엔티티 객체
     * @return OrderItemDetailResponse DTO 객체
     */
    public static OrderItemDetailResponse from(OrderItem orderItem) {
        ItemType type;
        Long originalId;

        if (orderItem.getBeverageItem() !=null) {
            type = ItemType.COFFEE;
            originalId = orderItem.getBeverageItem().getId();
        } else if (orderItem.getDessertItem() != null) {
            type = ItemType.DESSERT;
            originalId = orderItem.getDessertItem().getId();
        } else {
            throw new OrderException(OrderErrorCode.INVALID_ORDER_ITEM_REFERENCE);
        }

        return OrderItemDetailResponse.builder()
                .orderItemId(orderItem.getId())
                .itemName(orderItem.getItemName())
                .orderItemQuantity(orderItem.getOrderItemQuantity())
                .unitPrice(orderItem.getUnitPrice())
                .itemType(type)
                .originalItemId(originalId)
                .build();
    }
}
