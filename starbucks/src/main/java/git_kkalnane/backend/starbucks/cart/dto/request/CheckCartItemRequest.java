package git_kkalnane.backend.starbucks.cart.dto.request;

import git_kkalnane.backend.starbucks.item.domain.ItemType;
import lombok.Builder;

import java.util.List;

@Builder
public record CheckCartItemRequest(
        Long cartItemId,
        ItemType itemType,
        String itemName,
        int quantity,
        List<CheckCartItemOptionRequest> options) {
}
