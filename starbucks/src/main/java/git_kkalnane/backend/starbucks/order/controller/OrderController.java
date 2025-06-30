package git_kkalnane.backend.starbucks.order.controller;

import git_kkalnane.backend.starbucks._global.success.SuccessResponse;
import git_kkalnane.backend.starbucks.order.common.success.OrderSuccessCode;
import git_kkalnane.backend.starbucks.order.domain.Order;
import git_kkalnane.backend.starbucks.order.dto.request.StoreOrderStatusUpdateRequest;
import git_kkalnane.backend.starbucks.order.dto.response.CurrentOrderResponse;
import git_kkalnane.backend.starbucks.order.dto.response.OrderDetailResponse;
import git_kkalnane.backend.starbucks.order.dto.response.OrderListResponse;
import git_kkalnane.backend.starbucks.order.dto.request.CreateOrderDTO;
import git_kkalnane.backend.starbucks.order.dto.response.StoreOrderDetailResponse;
import git_kkalnane.backend.starbucks.order.dto.response.StoreOrderResponse;
import git_kkalnane.backend.starbucks.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 주문 관련 API 컨트롤러
 * 주문 생성, 수정, 삭제 담당
 *
 * @author Dotae
 * version 1.0
 */

@RestController
@RequiredArgsConstructor
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    @Operation(summary = "주문 생성")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "주문 생성 성공")
    })
    @PostMapping
    public ResponseEntity<SuccessResponse> createOrder(@Valid @RequestBody CreateOrderDTO request) {

        Long memberId = 1L;
        orderService.createOrder(request, memberId);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(SuccessResponse.of(OrderSuccessCode.ORDER_SUCCESS_CREATED, request));
    }

    @Operation(summary = "주문 상세 조회", description = "특정 주문의 상세 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "주문 상세 조회 성공"),
            @ApiResponse(responseCode = "404", description = "주문 찾을 수 없음")
    })
    @GetMapping("/{orderId}")
    public ResponseEntity<SuccessResponse> getOrderDetail(@PathVariable Long orderId) {
        OrderDetailResponse orderDetail = orderService.getOrderDetail(orderId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(SuccessResponse.of(OrderSuccessCode.ORDER_DETAIL_VIEWED, orderDetail));
    }

    @Operation(summary = "과거 주문 내역 목록 조회", description = "특정 회원의 과거 주문 목록을 페이지네이션하여 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "주문 목록 조회 성공"),
            @ApiResponse(responseCode = "404", description = "회원 찾을 수 없음")
    })
    @GetMapping("/history")
    public ResponseEntity<SuccessResponse> getOrderHistory(
            @RequestParam Long memberId,
            @PageableDefault(page = 0, size = 15, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        OrderListResponse orderList = orderService.getOrderHistory(memberId, pageable);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(SuccessResponse.of(OrderSuccessCode.ORDER_DETAIL_VIEWED, orderList));
    }

    @Operation(summary = "고객 현재 주문 목록 조회", description = "현재 로그인한 사용자의 진행중인(접수, 준비중, 픽업 가능) 모든 주문 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    @GetMapping("/users/{memberId}/current")
    public ResponseEntity<SuccessResponse> getCurrentOrders(@PathVariable Long memberId) {

        List<CurrentOrderResponse> result = orderService.getCurrentOrders(memberId);

        return ResponseEntity
                .ok(SuccessResponse.of(OrderSuccessCode.ORDER_CURRENT_VIEWED, result));
    }

    /**
     * [매장용 API] 특정 주문에 대한 상세 정보를 조회합니다.
     *
     * // TODO: 추후 Auth 로직 구현 시, @AuthenticationPrincipal을 사용하도록 변경.
     *
     * @param storeId 매장의 ID (임시로 URL 경로에서 직접 받습니다.)
     * @param orderId 조회할 주문의 ID
     * @return 주문 상세 정보를 담은 ResponseEntity
     */
    @GetMapping("/store/{storeId}/{orderId}") // 임시 url
    public ResponseEntity<SuccessResponse> getStoreOrderDetail(
            @PathVariable Long storeId, // 임시
            @PathVariable Long orderId
    ) {
        // TODO: 인증 구현 후 @AuthenticationPrincipal 사용
        Order order = orderService.getStoreOrderDetail(storeId, orderId);
        StoreOrderDetailResponse responseDto = StoreOrderDetailResponse.from(order);

        return ResponseEntity
                .ok(SuccessResponse.of(OrderSuccessCode.STORE_ORDER_DETAIL_VIEWED, responseDto));
    }

    @Operation(summary = "매장의 현재 주문 목록 조회", description = "특정 매장의 진행중인(접수, 준비중, 픽업 가능) 모둔 주문 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "매장을 찾을 수 없음")
    })
    @GetMapping("/store/{storeId}")
    public ResponseEntity<SuccessResponse> getStoreCurrentOrders(@PathVariable Long storeId) {

        List<StoreOrderResponse> result = orderService.getStoreCurrentOrders(storeId);

        return ResponseEntity
                .ok(SuccessResponse.of(OrderSuccessCode.STORE_ORDERS_VIEWED, result));
    }

    /**
     * [매장용 API] 특정 주문의 상태를 변경합니다. (예: 접수 -> 준비중)
     *
     * // TODO: 추후 Auth 로직 구현 시, @AuthenticationPrincipal을 사용하도록 변경.
     *
     * @param storeId           상태를 변경할 주문이 속한 매장의 ID (임시로 URL 경로에서 받습니다)
     * @param orderId           상태를 변경할 주문의 ID
     * @param request           새로운 주문 상태를 담은 DTO
     * @return 성공 응답
     */
    @PatchMapping("/store/{storeId}/{orderId}/status") // 임시
    public ResponseEntity<SuccessResponse> updateOrderStatus(
            @PathVariable Long storeId, // 임시
            @PathVariable Long orderId,
            @Valid @RequestBody StoreOrderStatusUpdateRequest request
    ) {

        orderService.updateOrderStatus(storeId, orderId, request.newStatus());

        return ResponseEntity
                .ok(SuccessResponse.of(OrderSuccessCode.ORDER_STATUS_UPDATED));
    }
}
