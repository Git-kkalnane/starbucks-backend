package git_kkalnane.backend.starbucks.order.dto.request;

import git_kkalnane.backend.starbucks.order.domain.OrderStatus;
import jakarta.validation.constraints.NotNull;

/**
 * 매장에서 주문 상태 변경을 요청할 때 사용하는 DTO (Data Transfer Object).
 * Java Record를 사용하여 불변 객체로 정의합니다.
 *
 * @param newStatus 변경하고자 하는 새로운 주문 상태 (PREPARING, READY_FOR_PICKUP, CANCELED 등)
 */
public record StoreOrderStatusUpdateRequest (
        @NotNull(message = "변경할 주문 상태는 필수입니다.")
        OrderStatus newStatus
) {
}
