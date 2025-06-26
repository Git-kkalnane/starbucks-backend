package git_kkalnane.backend.starbucks.order.dto.response;

import org.springframework.data.domain.Page;

import java.util.List;

public record OrderListResponse(
    List<OrderSummaryResponse> orders,
    int currentPage,
    int totalPages,
    long totalElements,
    int size,
    boolean isFirst,
    boolean isLast
) {
    /**
     * Spring Date Page 객체로부터 OrderListResponse DTO를 생성하는 메서드.
     * @param orderPage Spring Date Page<Order> 객체
     * @return OrderListResponse DTO 객체
     */
    public static OrderListResponse from(Page<OrderSummaryResponse> orderPage) {
        return new OrderListResponse(
                orderPage.getContent(),
                orderPage.getNumber(),
                orderPage.getTotalPages(),
                orderPage.getTotalElements(),
                orderPage.getSize(),
                orderPage.isFirst(),
                orderPage.isLast()
        );
    }
}
