package git_kkalnane.backend.starbucks.cart.dto.request;

import git_kkalnane.backend.starbucks.item.domain.ItemType;
import git_kkalnane.backend.starbucks.item.domain.beverage.enums.BeverageSizeOption;
import git_kkalnane.backend.starbucks.item.domain.beverage.enums.BeverageTemperatureOption;
import lombok.Builder;

import java.util.List;
import java.util.Set;

@Builder
public record CheckCartItemRequest(
        Long cartItemId,
        ItemType itemType,
        String itemName,
        int quantity,
        String imageUrl,
        BeverageSizeOption beverageSizeOption,
        BeverageTemperatureOption beverageTemperatureOption,
        List<CheckCartItemOptionRequest> options) {
}
