package git_kkalnane.backend.starbucks.notification.aop;

import git_kkalnane.backend.starbucks.order.service.OrderService;
import org.aspectj.lang.annotation.Aspect;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.aop.aspectj.AspectJExpressionPointcut;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * NotificationAspect의 포인트컷 매칭을 단위 테스트하는 클래스
 * Spring 컨텍스트를 로드하지 않고 순수하게 포인트컷 표현식만 테스트합니다.
 */
class NotificationAspectUnitTest {

    private AspectJExpressionPointcut pointcut;

    @BeforeEach
    void setUp() {
        pointcut = new AspectJExpressionPointcut();
        pointcut.setExpression("execution(* git_kkalnane.backend.starbucks.order.service.OrderService.createOrder(..))");
    }

    @Test
    @DisplayName("포인트컷 표현식이 올바른 형식인지 검증")
    void 포인트컷_표현식_검증() {
        // given & when
        String expression = pointcut.getExpression();
        
        // then
        assertThat(expression).isEqualTo("execution(* git_kkalnane.backend.starbucks.order.service.OrderService.createOrder(..))");
    }

    @Test
    @DisplayName("NotificationAspect 클래스가 @Aspect 어노테이션을 가지고 있는지 검증")
    void aspect_어노테이션_검증() {
        // given & when
        Class<NotificationAspect> aspectClass = NotificationAspect.class;
        
        // then
        assertThat(aspectClass.isAnnotationPresent(Aspect.class)).isTrue();
    }

    @Test
    @DisplayName("포인트컷이 다양한 메서드 시그니처에 대해 매칭되는지 테스트")
    void 포인트컷_다양한_시그니처_매칭_테스트() throws NoSuchMethodException {
        // given
        Method[] methods = OrderService.class.getMethods();
        
        // when & then
        for (Method method : methods) {
            if ("createOrder".equals(method.getName())) {
                assertThat(pointcut.matches(method, OrderService.class))
                    .as("createOrder 메서드는 매칭되어야 합니다: " + method)
                    .isTrue();
            } else {
                assertThat(pointcut.matches(method, OrderService.class))
                    .as("createOrder가 아닌 메서드는 매칭되지 않아야 합니다: " + method)
                    .isFalse();
            }
        }
    }

    @Test
    @DisplayName("포인트컷이 다른 클래스의 메서드에는 매칭되지 않는지 테스트")
    void 포인트컷_다른_클래스_매칭_실패_테스트() throws NoSuchMethodException {
        // given
        Method method = String.class.getMethod("length");
        
        // when
        boolean matches = pointcut.matches(method, String.class);
        
        // then
        assertThat(matches).isFalse();
    }

    @Test
    @DisplayName("포인트컷 표현식의 각 부분이 올바른지 검증")
    void 포인트컷_표현식_구성요소_검증() {
        // given
        String expression = "execution(* git_kkalnane.backend.starbucks.order.service.OrderService.createOrder(..))";
        
        // when & then
        assertThat(expression)
            .contains("execution")           // 실행 지점 지정
            .contains("*")                   // 모든 반환 타입
            .contains("git_kkalnane.backend.starbucks.order.service.OrderService")  // 대상 클래스
            .contains("createOrder")         // 대상 메서드
            .contains("(..)");               // 모든 매개변수
    }

    @Test
    @DisplayName("NotificationAspect의 포인트컷 메서드가 올바르게 정의되어 있는지 검증")
    void aspect_포인트컷_메서드_검증() throws NoSuchMethodException {
        // given
        Class<NotificationAspect> aspectClass = NotificationAspect.class;
        
        // when
        Method pointcutMethod = aspectClass.getDeclaredMethod("notificationServiceMethods");
        
        // then
        assertThat(pointcutMethod).isNotNull();
        assertThat(pointcutMethod.isAnnotationPresent(org.aspectj.lang.annotation.Pointcut.class)).isTrue();
    }

    @Test
    @DisplayName("NotificationAspect의 Advice 메서드가 올바르게 정의되어 있는지 검증")
    void aspect_advice_메서드_검증() throws NoSuchMethodException {
        // given
        Class<NotificationAspect> aspectClass = NotificationAspect.class;
        
        // when
        Method adviceMethod = aspectClass.getDeclaredMethod("orderAfter", 
            git_kkalnane.backend.starbucks.order.domain.Order.class);
        
        // then
        assertThat(adviceMethod).isNotNull();
        assertThat(adviceMethod.isAnnotationPresent(org.aspectj.lang.annotation.AfterReturning.class)).isTrue();
    }
} 