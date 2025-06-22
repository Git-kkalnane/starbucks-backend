package git_kkalnane.backend.starbucks.order.dto.request;

import git_kkalnane.backend.starbucks.order.domain.OrderStatus;
import git_kkalnane.backend.starbucks.order.domain.PickupType;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Order생성을 위한 Request요청 Record파일
 *
 * @param storeId : 매장ID
 * @param pickupType : 픽업Type
 * @param orderTotalPrice : 총 가격
 * @param orderStatus : Order상태(기본값 PLACED로 되어있음)
 * @param orderExpectedPickupTime : 픽업 걸리는 시간
 * @param orderItems :주문 상품들
 */
public record CreateRequest(
        Long storeId,
        PickupType pickupType,
        int orderTotalPrice,
        OrderStatus orderStatus,
        LocalDateTime orderExpectedPickupTime,
        List<OrderItemRequest> orderItems
) {

}
