package git_kkalnane.backend.starbucks.cart.dto.request;

import git_kkalnane.backend.starbucks.item.domain.ItemType;

public record CheckCartItemOptionRequest(
        Long itemOptionId,
        String syrupName,
        int quantity,
        int additionalPrice)
{
}
