package git_kkalnane.backend.starbucks.notification.dto.response;


import git_kkalnane.backend.starbucks.order.domain.PickupType;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class OrderNotificationSendResponse {
    private List<OrderNotificationSendBeverageItemResponse> beverageItems;
    private List<OrderNotificationSendDessertItemResponse> dessertItems;
    private Long orderId;
    private Long orderNumber;
    private PickupType pickupType;
    private String orderRequestMemo;
    private Long orderExpectedPickupTime;
    private String memberName;
    private Long memberId;
    private Long storeId;
}
