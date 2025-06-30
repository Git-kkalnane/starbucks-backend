package git_kkalnane.backend.starbucks._global.validation.annotation;

import git_kkalnane.backend.starbucks._global.validation.validator.ValidNicknameValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidNicknameValidator.class)
@Documented
public @interface ValidNickname {
    String message() default "닉네임은 1자 이상 6자 이하의 한글만 입력 가능합니다.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
