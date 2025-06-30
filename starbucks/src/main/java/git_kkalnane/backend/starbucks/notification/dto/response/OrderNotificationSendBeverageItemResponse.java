package git_kkalnane.backend.starbucks.notification.dto.response;


import git_kkalnane.backend.starbucks.item.domain.beverage.BeverageItem;
import lombok.Getter;


@Getter
public class OrderNotificationSendBeverageItemResponse {
    private BeverageItem beverage;

    private OrderNotificationSendBeverageItemResponse(BeverageItem beverage) {
        this.beverage = beverage;
    }

    public static OrderNotificationSendBeverageItemResponse of(BeverageItem beverage) {
        return new OrderNotificationSendBeverageItemResponse(beverage);
    }
}
