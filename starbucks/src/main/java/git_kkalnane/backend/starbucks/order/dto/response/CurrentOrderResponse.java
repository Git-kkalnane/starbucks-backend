package git_kkalnane.backend.starbucks.order.dto.response;

import git_kkalnane.backend.starbucks.item.domain.ItemType;
import git_kkalnane.backend.starbucks.item.domain.beverage.BeverageItem;
import git_kkalnane.backend.starbucks.item.domain.beverage.enums.BeverageSizeOption;
import git_kkalnane.backend.starbucks.item.domain.beverage.enums.BeverageTemperatureOption;
import git_kkalnane.backend.starbucks.order.domain.Order;
import git_kkalnane.backend.starbucks.order.domain.OrderItem;
import git_kkalnane.backend.starbucks.order.domain.OrderStatus;
import git_kkalnane.backend.starbucks.order.domain.PickupType;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 현재 진행중인 주문 목록 조회를 위한 상세 응답 DTO
 */
public record CurrentOrderResponse (
        Long orderId,
        String orderIdUI,
        Long storeId,
        String storeName,
        String storeAddress,
        OrderStatus orderStatus,
        PickupType pickupType,
        List<OrderItemDetail> orderItems
) {

    /**
     * Order 엔티티를 CurrentOrderResponse DTO로 변환하는 정적 팩토리 메서드
     * @param order 변환할 Order 엔티티
     * @return CurrentOrderResponse DTO
     */
    public static CurrentOrderResponse from(Order order) {
        List<OrderItemDetail> itemDetails = order.getOrderItems().stream()
                .map(OrderItemDetail::from)
                .collect(Collectors.toList());

        return new CurrentOrderResponse(
                order.getId(),
                order.getOrderNumber(),
                order.getStore().getId(),
                order.getStore().getName(),
                order.getStore().getAddress(),
                order.getOrderStatus(),
                order.getPickupType(),
                itemDetails
        );
    }

    /**
     * 주문에 포함된 개별 상품의 상세 정보를 담는 내부 DTO
     */
    public record OrderItemDetail(
            String name_ko,
            String name_en,
            ItemType itemType,
            boolean isCoffee,
            Set<BeverageTemperatureOption> temperatureOption,
            Set<BeverageSizeOption> sizeOption,
            String img_url,
            int quantity,
            int totalPrice,
            int itemPrice

    ) {
        public static OrderItemDetail from(OrderItem orderItem) {
            if (orderItem.getBeverageItem() != null) {
                BeverageItem beverage = orderItem.getBeverageItem();
                String imageUrl;

                if (orderItem.getTemperatureOption() == BeverageTemperatureOption.HOT) {
                    imageUrl = beverage.getHotImageUrl();
                } else {
                    imageUrl = beverage.getIceImageUrl();
                }

                return new OrderItemDetail(
                        orderItem.getItemName(),
                        beverage.getBeverageItemNameEn(),
                        beverage.isCoffee() ? ItemType.COFFEE : ItemType.BEVERAGE,
                        beverage.isCoffee(),
                        beverage.getSupportedTemperatures(),
                        beverage.getSupportedSizes(),
                        imageUrl,
                        orderItem.getOrderItemQuantity(),
                        orderItem.getUnitPrice() * orderItem.getOrderItemQuantity(),
                        orderItem.getUnitPrice()
                );
            } else {
                return new OrderItemDetail(
                        orderItem.getItemName(),
                        orderItem.getDessertItem().getDessertItemNameEn(),
                        ItemType.DESSERT,
                        false,
                        null,
                        null,
                        orderItem.getDessertItem().getImageUrl(),
                        orderItem.getOrderItemQuantity(),
                        orderItem.getUnitPrice() * orderItem.getOrderItemQuantity(),
                        orderItem.getUnitPrice()
                );
            }
        }
    }
}
