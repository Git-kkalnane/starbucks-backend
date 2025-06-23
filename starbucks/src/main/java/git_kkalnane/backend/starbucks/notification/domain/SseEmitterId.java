package git_kkalnane.backend.starbucks.notification.domain;

public class SseEmitterId {
    private final Long receiverId;
    private final NotificationTargetType notificationTargetType;
    private final String systemCurrentMillis;

    private final String emitterId;

    private SseEmitterId(Long receiverId, NotificationTargetType notificationTargetType, String systemCurrentMillis) {
        this.receiverId = receiverId;
        this.notificationTargetType = notificationTargetType;
        this.systemCurrentMillis = systemCurrentMillis;
        this.emitterId = "%s_%s_%s".formatted(
                notificationTargetType.name(),
                receiverId,
                systemCurrentMillis
        );
    }

    public String getId(){
        return emitterId;
    }

    public static String getEmitterIdPrefix(Long receiverId, NotificationTargetType notificationTargetType) {
        return "%s_%s".formatted(notificationTargetType.name(), receiverId);
    }

    public static SseEmitterId of(Long receiverId, NotificationTargetType notificationTargetType) {
        return new SseEmitterId(
                receiverId,
                notificationTargetType,
                String.valueOf(System.currentTimeMillis())
        );
    }
}
