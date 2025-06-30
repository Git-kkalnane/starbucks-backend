package git_kkalnane.backend.starbucks._global.validation.validator;

import git_kkalnane.backend.starbucks._global.validation.annotation.ValidName;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ValidNameValidator implements ConstraintValidator<ValidName, String> {

    private static final String NAME_PATTERN = "^[가-힣]+$";
    private static final int MIN_LENGTH = 2;
    private static final int MAX_LENGTH = 15;

    @Override
    public void initialize(ValidName constraintAnnotation) {
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
        return value.matches(NAME_PATTERN);
    }
}