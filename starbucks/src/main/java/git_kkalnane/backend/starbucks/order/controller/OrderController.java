package git_kkalnane.backend.starbucks.order.controller;

import git_kkalnane.backend.starbucks._global.success.SuccessCode;
import git_kkalnane.backend.starbucks._global.success.SuccessResponse;
import git_kkalnane.backend.starbucks.order.common.success.OrderSuccessCode;
import git_kkalnane.backend.starbucks.order.domain.Order;
import git_kkalnane.backend.starbucks.order.dto.request.CreateRequest;
import git_kkalnane.backend.starbucks.order.dto.response.CreateResponse;
import git_kkalnane.backend.starbucks.order.dto.response.OrderDetailResponse;
import git_kkalnane.backend.starbucks.order.dto.response.OrderListResponse;
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
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
            @ApiResponse(responseCode = "200", description = "주문 생성 성공")
    })
    @PostMapping
    public ResponseEntity<SuccessResponse> createOrder(@Valid @RequestBody CreateRequest request) {

        Long memberId = 1L;
        CreateResponse createResponse = orderService.createOrder(request, memberId);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(SuccessResponse.of(OrderSuccessCode.ORDER_SUCCESS_CREATED, createResponse));
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

}
