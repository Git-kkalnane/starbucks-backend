package git_kkalnane.backend.starbucks.notification.repository;

import git_kkalnane.backend.starbucks.notification.domain.NotificationTargetType;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;

public interface EmitterRepository {
    SseEmitter save(String emitterId, SseEmitter sseEmitter);

    void saveEventCache(String emitterId, Object event);

    Map<String, SseEmitter> findAllEmitterStartWithByReceiverIdAndNotificationTargetType
            (Long receiverId, NotificationTargetType notificationTargetType);

    Map<String, Object> findAllEventCacheStartWithByReceiverIdAndNotificationTargetType
            (Long receiverId, NotificationTargetType notificationTargetType);

    Map<String, SseEmitter> findAll();

    void deleteById(String id);

    void deleteAllEmitterStartWithId(String memberId);

    void deleteAllEventCacheStartWithId(String memberId);

}
