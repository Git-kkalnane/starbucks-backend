package git_kkalnane.backend.starbucks.notification.dto.response;


import git_kkalnane.backend.starbucks.item.domain.dessert.DessertItem;
import lombok.Getter;

@Getter
public class OrderNotificationSendDessertItemResponse {
    private DessertItem beverage;

    private OrderNotificationSendDessertItemResponse(DessertItem beverage) {
        this.beverage = beverage;
    }

    public static OrderNotificationSendDessertItemResponse of(DessertItem beverage) {
        return new OrderNotificationSendDessertItemResponse(beverage);
    }
}
