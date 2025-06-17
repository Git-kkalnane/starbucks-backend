package git_kkalnane.backend.starbucks.notification.repository;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;

public interface EmitterRepository {
    SseEmitter save(String emitterId, SseEmitter sseEmitter);
    void saveEventCache(String emitterId, Object event);
    Map<String, SseEmitter> findAllEmitterStartWithByMemberId(String memberId);
    Map<String, Object> findAllEventCacheStartWithByMemberId(String memberId);
    Map<String, SseEmitter> findAll();
    void deleteById(String id);
    void deleteAllEmitterStartWithId(String memberId);
    void deleteAllEventCacheStartWithId(String memberId);

}
