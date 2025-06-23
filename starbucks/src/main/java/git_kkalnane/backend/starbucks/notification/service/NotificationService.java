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
import git_kkalnane.backend.starbucks.notification.dto.response.NotificationResponse;
import git_kkalnane.backend.starbucks.notification.repository.EmitterRepository;
import git_kkalnane.backend.starbucks.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
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

    @Transactional
    public void sendNotification(String title, String message,
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
                    NotificationResponse responseDto = notification.toDto(key);

                    emitterRepository.saveEventCache(key, notification);
                    send(emitter, event, key, responseDto);
                }
        );
    }

    @Transactional
    public void sendNotification(NotificationSendRequest requestDto) {
        NotificationType notificationType =
                NotificationType.findByName(requestDto.getNotificationType());
        NotificationTargetType notificationTargetType =
                NotificationTargetType.findByName(requestDto.getNotificationTargetType());

        sendNotification(
                requestDto.getTitle(),
                requestDto.getMessage(),
                requestDto.getSenderId(),
                requestDto.getReceiverId(),
                notificationType,
                notificationTargetType
        );
    }

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

    public Map<String, SseEmitter> getEmitters(Long receiverId,
                                               NotificationTargetType notificationTargetType) {
        return emitterRepository.findAllEmitterStartWithByReceiverIdAndNotificationTargetType(
                receiverId,
                notificationTargetType);
    }

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
