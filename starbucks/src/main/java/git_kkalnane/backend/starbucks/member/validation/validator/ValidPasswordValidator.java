package git_kkalnane.backend.starbucks.member.validation.validator;

import git_kkalnane.backend.starbucks.member.validation.annotation.ValidPassword;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ValidPasswordValidator implements ConstraintValidator<ValidPassword, String> {

    // 영문 대문자, 영문 소문자, 숫자, 특수문자(!@#$%^*+=) 최소 1개 이상 포함, 10-20자, 공백/한글/하이픈/언더스코어 제외
    private static final String PASSWORD_PATTERN = "^(?=.*[A-Z])(?=.*[a-z])(?=.*[0-9])(?=.*[!@#$%^*+=])[^\\s가-힣\\-_]{10,20}$";

    @Override
    public void initialize(ValidPassword constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return false;
        }

        // 비밀번호 정규표현식 검증
        return value.matches(PASSWORD_PATTERN);
    }
}
