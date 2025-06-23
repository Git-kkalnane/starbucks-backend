package git_kkalnane.backend.starbucks.cart.dto.request;

import git_kkalnane.backend.starbucks.item.domain.ItemOption;
import git_kkalnane.backend.starbucks.item.domain.ItemType;
import git_kkalnane.backend.starbucks.item.domain.beverage.enums.BeverageSizeOption;
import git_kkalnane.backend.starbucks.item.domain.beverage.enums.BeverageTemperatureOption;

import java.util.List;

public record CartAddItemRequest(
        Long id,
        List<ItemsRequest> items,
        int totalPrice
) {
}
