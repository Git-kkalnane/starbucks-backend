package git_kkalnane.backend.starbucks.cart.dto.request;

import git_kkalnane.backend.starbucks.item.domain.ItemType;

public record CartItemOptionRequest(

        Long itemOptionId,
        String syrupName,
        boolean isRequired,
        int quantity,
        int additonalPrice,
        ItemType itemType
) {

}
