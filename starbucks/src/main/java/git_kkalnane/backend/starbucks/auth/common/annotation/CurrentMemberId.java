package git_kkalnane.backend.starbucks.auth.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Controller 메서드의 파라미터에 사용되며, 현재 인증된 사용자의 ID를 주입받기 위한 어노테이션입니다.
 * 이 어노테이션이 붙은 파라미터에는 JWT 토큰에서 추출한 memberId가 자동으로 주입됩니다.
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface CurrentMemberId {
}
