package git_kkalnane.backend.starbucks.notification;

import git_kkalnane.backend.starbucks.notification.common.exception.NotificationErrorCode;
import git_kkalnane.backend.starbucks.notification.common.exception.NotificationException;
import git_kkalnane.backend.starbucks.notification.domain.NotificationTargetType;
import git_kkalnane.backend.starbucks.notification.domain.NotificationType;
import git_kkalnane.backend.starbucks.notification.dto.request.NotificationSendRequest;
import git_kkalnane.backend.starbucks.notification.repository.EmitterRepository;
import git_kkalnane.backend.starbucks.notification.repository.NotificationRepository;
import git_kkalnane.backend.starbucks.notification.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Collections;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

class NotificationServiceTest {
    @Mock
    private EmitterRepository emitterRepository;
    @Mock
    private NotificationRepository notificationRepository;
    @InjectMocks
    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Nested
    @DisplayName("subscribe() 테스트")
    class SubscribeTest {
        @Test
        @DisplayName("성공: 정상 구독")
        void subscribe_success() {
            // given
            Long receiverId = 1L;
            String targetType = "CUSTOMER";
            SseEmitter emitter = new SseEmitter();
            given(emitterRepository.save(any(), any())).willReturn(emitter);

            // when
            SseEmitter result = notificationService.subscribe(receiverId, targetType);

            // then
            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("실패: 잘못된 notificationTargetType")
        void subscribe_invalidTargetType() {
            // given
            Long receiverId = 1L;
            String targetType = "INVALID";

            // when & then
            assertThatThrownBy(() -> notificationService.subscribe(receiverId, targetType))
                    .isInstanceOf(NotificationException.class)
                    .hasMessageContaining(NotificationErrorCode.INVALID_NOTIFICATION_TYPE.getMessage(targetType));
        }
    }

    @Nested
    @DisplayName("sendNotification(NotificationSendRequest) 테스트")
    class SendNotificationRequestTest {
        @Test
        @DisplayName("성공: 정상 알림 전송")
        void sendNotification_success() {
            // given
            NotificationSendRequest request = new NotificationSendRequest(
                    "title", "message", 1L, 2L, "SUBSCRIBE", "CUSTOMER"
            );
            given(emitterRepository.findAllEmitterStartWithByReceiverIdAndNotificationTargetType(anyLong(), any())).willReturn(Collections.emptyMap());

            // when
            notificationService.sendNotification(request);

            // then
            verify(notificationRepository, times(1)).save(any());
        }

        @Test
        @DisplayName("실패: 잘못된 notificationType")
        void sendNotification_invalidType() {
            // given
            NotificationSendRequest request = new NotificationSendRequest(
                    "title", "message", 1L, 2L, "INVALID", "CUSTOMER"
            );

            // when & then
            assertThatThrownBy(() -> notificationService.sendNotification(request))
                    .isInstanceOf(NotificationException.class)
                    .hasMessageContaining(NotificationErrorCode.INVALID_NOTIFICATION_TYPE
                            .getMessage(request.getNotificationType()));
        }

        @Test
        @DisplayName("실패: 잘못된 notificationTargetType")
        void sendNotification_invalidTargetType() {
            // given
            NotificationSendRequest request = new NotificationSendRequest(
                    "title", "message", 1L, 2L, "SUBSCRIBE", "INVALID"
            );

            // when & then
            assertThatThrownBy(() -> notificationService.sendNotification(request))
                    .isInstanceOf(NotificationException.class)
                    .hasMessageContaining(NotificationErrorCode.INVALID_NOTIFICATION_TYPE
                            .getMessage(request.getNotificationTargetType()));
        }
    }

    @Nested
    @DisplayName("getEmitters() 테스트")
    class GetEmittersTest {
        @Test
        @DisplayName("성공: emitter 목록 조회")
        void getEmitters_success() {
            // given
            Long receiverId = 1L;
            NotificationTargetType targetType = NotificationTargetType.CUSTOMER;
            Map<String, SseEmitter> emitters = Collections.singletonMap("key", new SseEmitter());
            given(emitterRepository.findAllEmitterStartWithByReceiverIdAndNotificationTargetType(receiverId, targetType)).willReturn(emitters);

            // when
            Map<String, SseEmitter> result = notificationService.getEmitters(receiverId, targetType);

            // then
            assertThat(result).isEqualTo(emitters);
        }
    }
} 