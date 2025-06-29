package git_kkalnane.backend.starbucks.member.validation.validator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import git_kkalnane.backend.starbucks.member.validation.annotation.ValidPassword;
import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class ValidPasswordValidatorTest {

    private ValidPasswordValidator validator;
    private ConstraintValidatorContext context;

    @BeforeEach
    void setUp() {
        validator = new ValidPasswordValidator();
        context = mock(ConstraintValidatorContext.class);
        validator.initialize(mock(ValidPassword.class));
    }

    @Nested
    @DisplayName("정상 케이스")
    class ValidCases {

        @Test
        @DisplayName("모든 조건을 만족하는 비밀번호는 검증을 통과한다")
        void validPassword_ShouldPass() {
            // given
            String validPassword = "Password123!";

            // when
            boolean result = validator.isValid(validPassword, context);

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("다양한 특수문자가 포함된 비밀번호는 검증을 통과한다")
        void passwordWithVariousSpecialChars_ShouldPass() {
            // given
            String passwordWithSpecialChars = "Pass123!@#$%^";

            // when
            boolean result = validator.isValid(passwordWithSpecialChars, context);

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("대소문자 영문이 포함된 비밀번호는 검증을 통과한다")
        void passwordWithMixedCase_ShouldPass() {
            // given
            String mixedCasePassword = "MyPass123!";

            // when
            boolean result = validator.isValid(mixedCasePassword, context);

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("복잡한 조합의 비밀번호는 검증을 통과한다")
        void complexPassword_ShouldPass() {
            // given
            String complexPassword = "MySecurePass123!@#";

            // when
            boolean result = validator.isValid(complexPassword, context);

            // then
            assertThat(result).isTrue();
        }
    }

    @Nested
    @DisplayName("예외 케이스")
    class InvalidCases {

        @Test
        @DisplayName("null 값은 검증을 실패한다")
        void nullValue_ShouldFail() {
            // given
            String nullPassword = null;

            // when
            boolean result = validator.isValid(nullPassword, context);

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("빈 문자열은 검증을 실패한다")
        void emptyString_ShouldFail() {
            // given
            String emptyPassword = "";

            // when
            boolean result = validator.isValid(emptyPassword, context);

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("영문이 없는 비밀번호는 검증을 실패한다")
        void passwordWithoutLetters_ShouldFail() {
            // given
            String passwordWithoutLetters = "123456789!@";

            // when
            boolean result = validator.isValid(passwordWithoutLetters, context);

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("숫자가 없는 비밀번호는 검증을 실패한다")
        void passwordWithoutNumbers_ShouldFail() {
            // given
            String passwordWithoutNumbers = "Password!@#";

            // when
            boolean result = validator.isValid(passwordWithoutNumbers, context);

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("특수문자가 없는(영문과 숫자만 있는) 비밀번호는 검증을 실패한다")
        void passwordWithoutSpecialChars_ShouldFail() {
            // given
            String passwordWithoutSpecialChars = "Password123";

            // when
            boolean result = validator.isValid(passwordWithoutSpecialChars, context);

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("영문 대문자가 없는(영문 소문자와 숫자, 특수문자만 있는) 비밀번호는 검증을 실패한다")
        void passwordWithoutCapitalLetter_ShouldFail() {
            // given
            String passwordWithoutCapitalLetter = "password123!";

            // when
            boolean result = validator.isValid(passwordWithoutCapitalLetter, context);

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("영문 소문자가 없는(영문 소문자와 숫자, 특수문자만 있는) 비밀번호는 검증을 실패한다")
        void passwordWithoutSmallLetter_ShouldFail() {
            // given
            String passwordWithoutSmallLetter = "PASSWORD123!";

            // when
            boolean result = validator.isValid(passwordWithoutSmallLetter, context);

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("영문과 특수문자만 있는 비밀번호는 검증을 실패한다")
        void passwordWithOnlyLettersAndSpecialChars_ShouldFail() {
            // given
            String passwordWithOnlyLettersAndSpecialChars = "Password!@#";

            // when
            boolean result = validator.isValid(passwordWithOnlyLettersAndSpecialChars, context);

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("숫자와 특수문자만 있는 비밀번호는 검증을 실패한다")
        void passwordWithOnlyNumbersAndSpecialChars_ShouldFail() {
            // given
            String passwordWithOnlyNumbersAndSpecialChars = "123456789!@#";

            // when
            boolean result = validator.isValid(passwordWithOnlyNumbersAndSpecialChars, context);

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("공백이 포함된 비밀번호는 검증을 실패한다")
        void passwordWithSpaces_ShouldFail() {
            // given
            String passwordWithSpaces = "Password 123!";

            // when
            boolean result = validator.isValid(passwordWithSpaces, context);

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("한글이 포함된 비밀번호는 검증을 실패한다")
        void passwordWithKorean_ShouldFail() {
            // given
            String passwordWithKorean = "Password123!한글";

            // when
            boolean result = validator.isValid(passwordWithKorean, context);

            // then
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("경계값 테스트")
    class BoundaryTests {

        @Test
        @DisplayName("정확히 10자인 비밀번호는 검증을 통과한다")
        void exactlyTenCharacters_ShouldPass() {
            // given
            String tenCharPassword = "Pass123!@#";

            // when
            boolean result = validator.isValid(tenCharPassword, context);

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("정확히 20자인 비밀번호는 검증을 통과한다")
        void exactlyTwentyCharacters_ShouldPass() {
            // given
            String twentyCharPassword = "Password123!@#$%^*+=";

            // when
            boolean result = validator.isValid(twentyCharPassword, context);

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("9자인 비밀번호는 검증을 실패한다")
        void exactlyNineCharacters_ShouldFail() {
            // given
            String nineCharPassword = "Pass123!@";

            // when
            boolean result = validator.isValid(nineCharPassword, context);

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("21자인 비밀번호는 검증을 실패한다")
        void exactlyTwentyOneCharacters_ShouldFail() {
            // given
            String twentyOneCharPassword = "Password1234!@#$%^*+=";

            // when
            boolean result = validator.isValid(twentyOneCharPassword, context);

            // then
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("특수문자 테스트")
    class SpecialCharacterTests {

        @Test
        @DisplayName("느낌표가 포함된 비밀번호는 검증을 통과한다")
        void passwordWithExclamationMark_ShouldPass() {
            // given
            String passwordWithExclamation = "Password123!";

            // when
            boolean result = validator.isValid(passwordWithExclamation, context);

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("골뱅이가 포함된 비밀번호는 검증을 통과한다")
        void passwordWithAtSign_ShouldPass() {
            // given
            String passwordWithAtSign = "Password123@";

            // when
            boolean result = validator.isValid(passwordWithAtSign, context);

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("샵이 포함된 비밀번호는 검증을 통과한다")
        void passwordWithHash_ShouldPass() {
            // given
            String passwordWithHash = "Password123#";

            // when
            boolean result = validator.isValid(passwordWithHash, context);

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("달러가 포함된 비밀번호는 검증을 통과한다")
        void passwordWithDollar_ShouldPass() {
            // given
            String passwordWithDollar = "Password123$";

            // when
            boolean result = validator.isValid(passwordWithDollar, context);

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("퍼센트가 포함된 비밀번호는 검증을 통과한다")
        void passwordWithPercent_ShouldPass() {
            // given
            String passwordWithPercent = "Password123%";

            // when
            boolean result = validator.isValid(passwordWithPercent, context);

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("캐럿이 포함된 비밀번호는 검증을 통과한다")
        void passwordWithCaret_ShouldPass() {
            // given
            String passwordWithCaret = "Password123^";

            // when
            boolean result = validator.isValid(passwordWithCaret, context);

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("별표가 포함된 비밀번호는 검증을 통과한다")
        void passwordWithAsterisk_ShouldPass() {
            // given
            String passwordWithAsterisk = "Password123*";

            // when
            boolean result = validator.isValid(passwordWithAsterisk, context);

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("플러스가 포함된 비밀번호는 검증을 통과한다")
        void passwordWithPlus_ShouldPass() {
            // given
            String passwordWithPlus = "Password123+";

            // when
            boolean result = validator.isValid(passwordWithPlus, context);

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("등호가 포함된 비밀번호는 검증을 통과한다")
        void passwordWithEquals_ShouldPass() {
            // given
            String passwordWithEquals = "Password123=";

            // when
            boolean result = validator.isValid(passwordWithEquals, context);

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("언더스코어가 포함된 비밀번호는 검증을 실패한다")
        void passwordWithUnderscore_ShouldFail() {
            // given
            String passwordWithUnderscore = "Password123_";

            // when
            boolean result = validator.isValid(passwordWithUnderscore, context);

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("하이픈이 포함된 비밀번호는 검증을 실패한다")
        void passwordWithHyphen_ShouldFail() {
            // given
            String passwordWithHyphen = "Password123-";

            // when
            boolean result = validator.isValid(passwordWithHyphen, context);

            // then
            assertThat(result).isFalse();
        }
    }
}
