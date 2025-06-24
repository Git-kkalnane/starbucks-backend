package git_kkalnane.backend.starbucks.order.dto.request;

import git_kkalnane.backend.starbucks.item.domain.ItemType;
import git_kkalnane.backend.starbucks.item.domain.beverage.enums.BeverageSizeOption;
import git_kkalnane.backend.starbucks.item.domain.beverage.enums.BeverageTemperatureOption;

import java.util.List;

/**
 *  OrderItem(주문상품) RequestRecord입니다.
 * @param itemId : 아이템ID
 * @param itemType : 아이템 Type
 * @param beverageSizeOption : 음료사이즈
 * @param beverageTemperatureOption : 음료 온도
 * @param options : 추가옵션들
 * @param totalPrice : 총 가격
 * @param itemPrice : 아이템 개별 가격
 */
public record OrderItemRequest(
        Long itemId,
        ItemType itemType,
        BeverageSizeOption beverageSizeOption,
        BeverageTemperatureOption beverageTemperatureOption,
        List<ItemOptionRequest> options,
        int itemPrice,
        int totalPrice,
        int quantity
        ) {

}
