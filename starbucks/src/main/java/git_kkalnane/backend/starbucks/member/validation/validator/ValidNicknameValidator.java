package git_kkalnane.backend.starbucks.member.validation.validator;

import git_kkalnane.backend.starbucks.member.validation.annotation.ValidNickname;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ValidNicknameValidator implements ConstraintValidator<ValidNickname, String> {

    private static final String NICKNAME_PATTERN = "^[가-힣]+$";
    private static final int MIN_LENGTH = 1;
    private static final int MAX_LENGTH = 6;

    @Override
    public void initialize(ValidNickname constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return false;
        }

        // 길이 검증
        if (value.length() < MIN_LENGTH || value.length() > MAX_LENGTH) {
            return false;
        }

        // 정규표현식 검증 (한글만)
        return value.matches(NICKNAME_PATTERN);
    }
}
