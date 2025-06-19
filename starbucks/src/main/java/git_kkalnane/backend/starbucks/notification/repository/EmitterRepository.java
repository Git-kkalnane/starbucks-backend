package git_kkalnane.backend.starbucks.notification.repository;

import git_kkalnane.backend.starbucks.notification.domain.NotificationTargetType;
import git_kkalnane.backend.starbucks.notification.domain.SseEmitterId;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;

public interface EmitterRepository {
    SseEmitter save(SseEmitterId emitterId, SseEmitter sseEmitter);

    void saveEventCache(String emitterIdValue, Object event);

    Map<String, SseEmitter> findAllEmitterStartWithByReceiverIdAndNotificationTargetType
            (Long receiverId, NotificationTargetType notificationTargetType);

    Map<String, Object> findAllEventCacheStartWithByReceiverIdAndNotificationTargetType
            (Long receiverId, NotificationTargetType notificationTargetType);

    Map<String, SseEmitter> findAll();

    void deleteById(String id);

    void deleteById(SseEmitterId id);

    void deleteAllEmitterStartWithNotificationTargetTypeAndReceiverId
            (Long receiverId, NotificationTargetType notificationTargetType);

    void deleteAllEventCacheStartWithNotificationTargetTypeAndReceiverId
            (Long receiverId, NotificationTargetType notificationTargetType);


}
