package git_kkalnane.backend.starbucks.notification.service;


import git_kkalnane.backend.starbucks.notification.common.success.NotificationSuccessCode;
import git_kkalnane.backend.starbucks.notification.domain.Notification;
import git_kkalnane.backend.starbucks.notification.domain.NotificationTargetType;
import git_kkalnane.backend.starbucks.notification.domain.NotificationType;
import git_kkalnane.backend.starbucks.notification.domain.SseEmitterId;
import git_kkalnane.backend.starbucks.notification.domain.vo.NotificationEvent;
import git_kkalnane.backend.starbucks.notification.domain.vo.NotificationReceiver;
import git_kkalnane.backend.starbucks.notification.domain.vo.NotificationSender;
import git_kkalnane.backend.starbucks.notification.dto.request.NotificationSendRequest;
import git_kkalnane.backend.starbucks.notification.dto.response.NotificationItemResponse;
import git_kkalnane.backend.starbucks.notification.dto.response.NotificationResponse;
import git_kkalnane.backend.starbucks.notification.dto.response.NotificationsResponse;
import git_kkalnane.backend.starbucks.notification.repository.EmitterRepository;
import git_kkalnane.backend.starbucks.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private static final Long DEFAULT_TIMEOUT = 60L * 1000 * 60;

    private final EmitterRepository emitterRepository;
    private final NotificationRepository notificationRepository;

    /**
     * SSE 연결을 구독하는 메서드
     *
     * @param receiverId                    수신자 ID
     * @param notificationTargetTypeName    알림 대상 타입 이름 (CUSTOMER, MERCHANT 등)
     * @return 생성된 SSE Emitter
     */
    public SseEmitter subscribe(Long receiverId, String notificationTargetTypeName) {
        // emitterId 생성
        NotificationTargetType notificationTargetType =
                NotificationTargetType.findByName(notificationTargetTypeName);

        SseEmitterId sseEmitterId = SseEmitterId.of(receiverId, notificationTargetType);

        // 하나의 클라이언트에 대한 emitter 저장
        SseEmitter emitter = emitterRepository.save(sseEmitterId, new SseEmitter(DEFAULT_TIMEOUT));

        // 클라이언트의 연결 종료 및 타임아웃에 대한 이벤트 처리 -> Emiiter 삭제
        emitter.onCompletion(() -> emitterRepository.deleteById(sseEmitterId.getId()));
        emitter.onTimeout(() -> emitterRepository.deleteById(sseEmitterId.getId()));

        // 503 에러를 방지하기 위한 구독용 더미 이벤트 전송
        NotificationEvent event = NotificationEvent.of
                (receiverId, NotificationTargetType.CUSTOMER, NotificationType.SUBSCRIBE);

        send(emitter, event, sseEmitterId.getId(),
                NotificationSuccessCode.NOTIFICATION_SUBSCRIBED.getMessage(receiverId));

        return emitter;
    }

    /**
     * 회원 ID로 알림 목록을 페이징하여 조회하는 메서드
     *
     * @param memberId  조회할 회원 ID
     * @param pageable  페이징 정보
     * @return 알림 목록과 페이징 정보를 포함한 응답
     */
    public NotificationsResponse fetchNotificationsByMemberId(Long memberId, Pageable pageable){
        Page<Notification> notifications = notificationRepository.findAllByReceiverId(memberId, pageable);

        return NotificationsResponse.builder()
                .notifications(
                        notifications.stream()
                                .map(Notification::toDto)
                                .toList()
                )
                .total(notifications.getTotalElements())
                .page(notifications.getNumber())
                .pageSize(notifications.getSize())
                .totalPages(notifications.getTotalPages()).build();
    }

    /**
     * 아이템과 함께 알림을 전송하는 메서드 (매장용)
     *
     * @param item                        전송할 아이템 데이터
     * @param title                       알림 제목
     * @param message                     알림 메시지
     * @param senderId                    발신자 ID
     * @param receiverId                  수신자 ID
     * @param notificationType            알림 타입
     * @param notificationTargetType      알림 대상 타입
     * @param <T>                        아이템 타입
     * @return 생성된 알림 엔티티
     */
    // TODO: 매장에 전달하는 알림은 데이터 전송 용도로 활용할 것
    @Transactional
    public <T> Notification sendNotification(T item, String title, String message,
                                     Long senderId, Long receiverId,
                                     NotificationType notificationType,
                                     NotificationTargetType notificationTargetType) {
        NotificationEvent event =
                NotificationEvent.of(receiverId, notificationTargetType, notificationType);

        Notification notification =
                createNotification(message, title, event,
                        NotificationReceiver.of(receiverId),
                        NotificationSender.of(senderId),
                        notificationType, notificationTargetType);

        // TODO : 스프링 이벤트 분리를 통해 비동기 작업으로 처리
        notificationRepository.save(notification);

        Map<String, SseEmitter> emitters = emitterRepository
                .findAllEmitterStartWithByReceiverIdAndNotificationTargetType(
                        receiverId,
                        notificationTargetType);

        // TODO: 트랜잭션 실패로 인한 롤백 처리 등의 안정성 고려하기
        emitters.forEach(
                (key, emitter) -> {
                    NotificationItemResponse<T> responseDto = notification.toDto(item);

                    emitterRepository.saveEventCache(key, notification);
                    send(emitter, event, key, responseDto);
                }
        );

        return notification;
    }

    /**
     * 기본 알림을 전송하는 메서드
     *
     * @param title                       알림 제목
     * @param message                     알림 메시지
     * @param senderId                    발신자 ID
     * @param receiverId                  수신자 ID
     * @param notificationType            알림 타입
     * @param notificationTargetType      알림 대상 타입
     * @return 생성된 알림 엔티티
     */
    @Transactional
    public Notification sendNotification(String title, String message,
                                 Long senderId, Long receiverId,
                                 NotificationType notificationType,
                                 NotificationTargetType notificationTargetType) {
        NotificationEvent event =
                NotificationEvent.of(receiverId, notificationTargetType, notificationType);

        Notification notification =
                createNotification(message, title, event,
                        NotificationReceiver.of(receiverId),
                        NotificationSender.of(senderId),
                        notificationType, notificationTargetType);


        // TODO : 스프링 이벤트 분리를 통해 비동기 작업으로 처리
        notificationRepository.save(notification);

        Map<String, SseEmitter> emitters = emitterRepository
                .findAllEmitterStartWithByReceiverIdAndNotificationTargetType(
                        receiverId,
                        notificationTargetType);

        // TODO: 트랜잭션 실패로 인한 롤백 처리 등의 안정성 고려하기
        emitters.forEach(
                (key, emitter) -> {
                    NotificationResponse responseDto = notification.toDto();

                    emitterRepository.saveEventCache(key, notification);
                    send(emitter, event, key, responseDto);
                }
        );

        return notification;
    }

    /**
     * NotificationSendRequest를 통해 알림을 전송하는 메서드
     *
     * @param requestDto 알림 전송 요청 DTO
     * @return 생성된 알림 엔티티
     */
    @Transactional
    public Notification sendNotification(NotificationSendRequest requestDto) {
        NotificationType notificationType =
                NotificationType.findByName(requestDto.getNotificationType());
        NotificationTargetType notificationTargetType =
                NotificationTargetType.findByName(requestDto.getNotificationTargetType());

        return sendNotification(
                requestDto.getTitle(),
                requestDto.getMessage(),
                requestDto.getSenderId(),
                requestDto.getReceiverId(),
                notificationType,
                notificationTargetType
        );
    }

    /**
     * SSE Emitter를 통해 이벤트를 전송하는 메서드
     *
     * @param emitter    전송할 SSE Emitter
     * @param event      전송할 이벤트
     * @param emitterId  Emitter ID
     * @param data       전송할 데이터
     */
    private void send(SseEmitter emitter, NotificationEvent event, String emitterId, Object data) {
        try {
            emitter.send(SseEmitter.event()
                    .id(event.value())
                    .name("sse")
                    .data(data)
            );
        } catch (IOException exception) {
            emitterRepository.deleteById(emitterId);
        }
    }

    /**
     * 특정 수신자와 알림 대상 타입에 해당하는 모든 Emitter를 조회하는 메서드
     *
     * @param receiverId               수신자 ID
     * @param notificationTargetType   알림 대상 타입
     * @return Emitter ID와 Emitter의 맵
     */
    public Map<String, SseEmitter> getEmitters(Long receiverId,
                                               NotificationTargetType notificationTargetType) {
        return emitterRepository.findAllEmitterStartWithByReceiverIdAndNotificationTargetType(
                receiverId,
                notificationTargetType);
    }

    /**
     * 알림 엔티티를 생성하는 메서드
     *
     * @param message                   알림 메시지
     * @param title                     알림 제목
     * @param event                     알림 이벤트
     * @param receiver                  수신자 정보
     * @param sender                    발신자 정보
     * @param notificationType          알림 타입
     * @param notificationTargetType    알림 대상 타입
     * @return 생성된 알림 엔티티
     */
    private Notification createNotification(
                                            String message, String title,
                                            NotificationEvent event,
                                            NotificationReceiver receiver,
                                            NotificationSender sender,
                                            NotificationType notificationType,
                                            NotificationTargetType notificationTargetType) {
        return Notification.builder()
                .title(title)
                .message(message)
                .event(event)
                .receiver(receiver)
                .sender(sender)
                .notificationTargetType(notificationTargetType)
                .notificationType(notificationType)
                .isRead(false)
                .build();
    }
}
