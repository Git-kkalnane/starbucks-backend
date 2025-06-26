package git_kkalnane.backend.starbucks.notification.repository;


import git_kkalnane.backend.starbucks.notification.domain.NotificationTargetType;
import git_kkalnane.backend.starbucks.notification.domain.SseEmitterId;
import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
public class EmitterRepositoryImpl implements EmitterRepository {
    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();
    private final Map<String, Object> eventCache = new ConcurrentHashMap<>();

    @Override
    public SseEmitter save(SseEmitterId emitterId, SseEmitter sseEmitter) {
        emitters.put(emitterId.getId(), sseEmitter);
        return sseEmitter;
    }

    @Override
    public void saveEventCache(String eventCacheId, Object event) {
        eventCache.put(eventCacheId, event);
    }

    @Override
    public Map<String, SseEmitter> findAllEmitterStartWithByReceiverIdAndNotificationTargetType
            (Long receiverId, NotificationTargetType notificationTargetType) {
        String prefix = SseEmitterId.getEmitterIdPrefix(receiverId, notificationTargetType);

        return emitters.entrySet().stream()
                .filter(entry -> entry.getKey().startsWith(prefix))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    @Override
    public Map<String, Object> findAllEventCacheStartWithByReceiverIdAndNotificationTargetType
            (Long receiverId, NotificationTargetType notificationTargetType) {
        String prefix = SseEmitterId.getEmitterIdPrefix(receiverId, notificationTargetType);

        return eventCache.entrySet().stream()
                .filter(entry -> entry.getKey().startsWith(prefix))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }


    @Override
    public Map<String, SseEmitter> findAll() {
        return emitters;
    }

    @Override
    public void deleteById(String id) {
        emitters.remove(id);
    }

    @Override
    public void deleteById(SseEmitterId sseEmitterId) {
        emitters.remove(sseEmitterId.getId());
    }

    @Override
    public void deleteAllEmitterStartWithNotificationTargetTypeAndReceiverId(
            Long receiverId, NotificationTargetType notificationTargetType) {
        emitters.forEach(
                (key, emitter) -> {
                    if (key.startsWith(SseEmitterId.getEmitterIdPrefix(receiverId, notificationTargetType))) {
                        emitters.remove(key);
                    }
                }
        );
    }

    @Override
    public void deleteAllEventCacheStartWithNotificationTargetTypeAndReceiverId(
            Long receiverId, NotificationTargetType notificationTargetType) {
        eventCache.forEach(
                (key, emitter) -> {
                    if (key.startsWith(SseEmitterId.getEmitterIdPrefix(receiverId, notificationTargetType))) {
                        eventCache.remove(key);
                    }
                }
        );
    }
}
