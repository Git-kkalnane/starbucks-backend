package git_kkalnane.backend.starbucks.notification.dto.response;


import git_kkalnane.backend.starbucks.item.domain.dessert.DessertItem;
import lombok.Getter;

@Getter
public class OrderNotificationSendDessertItemResponse {
    private DessertItem dessert;

    private OrderNotificationSendDessertItemResponse(DessertItem dessert) {
        this.dessert = dessert;
    }

    public static OrderNotificationSendDessertItemResponse of(DessertItem dessert) {
        return new OrderNotificationSendDessertItemResponse(dessert);
    }
}
