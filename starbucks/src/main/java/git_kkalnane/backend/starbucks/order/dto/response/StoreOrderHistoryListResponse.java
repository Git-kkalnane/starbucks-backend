package git_kkalnane.backend.starbucks.order.dto.response;

import git_kkalnane.backend.starbucks.order.domain.Order;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * 매장의 과거 주문 내역 목록과 페이지네이션 정보를 함께 담는 최종 응답 DTO.
 *
 * @param orders        주문 요약 정보 리스트
 * @param currentPage   현재 페이지 번호
 * @param totalPages    전체 페이지 수
 * @param totalElements 전체 주문 개수
 */
public record StoreOrderHistoryListResponse(
        List<StoreOrderHistoryResponse> orders,
        int currentPage,
        int totalPages,
        long totalElements
) {
    public static StoreOrderHistoryListResponse from(Page<Order> orderPage) {
        List<StoreOrderHistoryResponse> orderResponses = orderPage.getContent().stream()
                .map(StoreOrderHistoryResponse::from)
                .toList();

        return new StoreOrderHistoryListResponse(
                orderResponses,
                orderPage.getNumber(),
                orderPage.getTotalPages(),
                orderPage.getTotalElements()
        );
    }
}
