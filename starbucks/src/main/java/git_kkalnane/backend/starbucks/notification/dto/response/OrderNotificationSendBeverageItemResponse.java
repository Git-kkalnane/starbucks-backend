package git_kkalnane.backend.starbucks.notification.dto.response;


import git_kkalnane.backend.starbucks.item.domain.beverage.BeverageItem;
import git_kkalnane.backend.starbucks.item.domain.beverage.BeverageItemSyrup;
import lombok.Getter;

import java.util.List;

@Getter
public class OrderNotificationSendBeverageItemResponse {
    private BeverageItem beverage;
    private List<BeverageItemSyrup> syrups;


    private OrderNotificationSendBeverageItemResponse(BeverageItem beverage, List<BeverageItemSyrup> syrups) {
        this.beverage = beverage;
        this.syrups = syrups;
    }

    public static OrderNotificationSendBeverageItemResponse of(BeverageItem beverage) {
        return new OrderNotificationSendBeverageItemResponse(beverage, beverage.getBeverageItemSyrup());
    }
}
