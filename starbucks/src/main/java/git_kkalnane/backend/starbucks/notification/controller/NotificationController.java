package git_kkalnane.backend.starbucks.notification.controller;


import git_kkalnane.backend.starbucks._global.success.SuccessResponse;
import git_kkalnane.backend.starbucks.notification.common.success.NotificationSuccessCode;
import git_kkalnane.backend.starbucks.notification.domain.NotificationTargetType;
import git_kkalnane.backend.starbucks.notification.dto.request.OrderNotificationSendRequest;
import git_kkalnane.backend.starbucks.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;


    @GetMapping(value = "/subscribe", produces = "text/event-stream")
    @Operation(summary = "클라이언트 알림 구독 요청"
            , description = "클라이언트가 알림을 구독하기 위한 요청입니다. SSE를 통해 실시간 알림을 받을 수 있습니다.")
    @ApiResponse(
            responseCode = "200",
            description = "알림 구독 성공"
    )
    public SseEmitter subscribe(@RequestParam Long receiverId,
                                @RequestParam String notificationTargetType) {

        return notificationService.subscribe(receiverId, notificationTargetType);
    }

    @PostMapping
    @Operation(summary = "멤버에 알림 전송 요청"
            , description = "특정 멤버에게 알림 전송을 요청합니다. 매장에서 멤버에 알림 전송을 요청할 때 쓰입니다.")
    @ApiResponse(
            responseCode = "200",
            description = "알림 전송 성공"
    )
    public ResponseEntity<SuccessResponse<?>> notificationRequest(@RequestBody OrderNotificationSendRequest request) {
        notificationService.sendNotification(request);
        return ResponseEntity.ok(SuccessResponse.of(
                NotificationSuccessCode.NOTIFICATION_DELIVERED));
    }

    @GetMapping
    @Operation(summary = "멤버 알림 목록 조회"
            , description = "멤버의 알림 목록을 조회합니다. 조회되지 않으면 빈 리스트를 반환합니다.")
    @ApiResponse(
            responseCode = "200",
            description = "알림 목록 조회 완료"
    )
    public ResponseEntity<SuccessResponse<?>> fetchNotifications(
            @RequestAttribute Long memberId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        return ResponseEntity.ok(SuccessResponse.of(
                NotificationSuccessCode.NOTIFICATION_SUBSCRIPTION_RETRIEVED
                , notificationService.fetchNotificationsByMemberId(memberId, pageable)));
    }


    @GetMapping(value = "/subscribe/status")
    @Operation(summary = "알림 구독 현황 목록 조회"
            , description = "알림 구독 현황(SseEmitter) 목록을 조회합니다. 조회되지 않으면 빈 리스트를 반환합니다.")
    @ApiResponse(
            responseCode = "200",
            description = "알림 구독 목록 조회 완료"
    )
    public ResponseEntity<SuccessResponse<?>> fetchSubscribeList(@RequestAttribute Long memberId,
                                                                @RequestParam String notificationTargetType) {
        return ResponseEntity.ok(SuccessResponse.of(
                NotificationSuccessCode.NOTIFICATION_SUBSCRIPTION_RETRIEVED
                , notificationService.getEmitters(memberId, NotificationTargetType.findByName(notificationTargetType))));
    }
}
