package git_kkalnane.backend.starbucks._global.validation.validator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import git_kkalnane.backend.starbucks._global.validation.annotation.ValidEmail;
import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class ValidEmailValidatorTest {

    private ValidEmailValidator validator;
    private ConstraintValidatorContext context;

    @BeforeEach
    void setUp() {
        validator = new ValidEmailValidator();
        context = mock(ConstraintValidatorContext.class);
        validator.initialize(mock(ValidEmail.class));
    }

    @Nested
    @DisplayName("정상 케이스")
    class ValidCases {

        @Test
        @DisplayName("기본적인 유효한 이메일은 검증을 통과한다")
        void basicValidEmail_ShouldPass() {
            // given
            String validEmail = "test@example.com";

            // when
            boolean result = validator.isValid(validEmail, context);

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("숫자가 포함된 이메일은 검증을 통과한다")
        void emailWithNumbers_ShouldPass() {
            // given
            String emailWithNumbers = "test123@example.com";

            // when
            boolean result = validator.isValid(emailWithNumbers, context);

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("점(.)이 포함된 이메일은 검증을 통과한다")
        void emailWithDots_ShouldPass() {
            // given
            String emailWithDots = "test.user@example.com";

            // when
            boolean result = validator.isValid(emailWithDots, context);

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("언더스코어가 포함된 이메일은 검증을 통과한다")
        void emailWithUnderscore_ShouldPass() {
            // given
            String emailWithUnderscore = "test_user@example.com";

            // when
            boolean result = validator.isValid(emailWithUnderscore, context);

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("하이픈이 포함된 이메일은 검증을 통과한다")
        void emailWithHyphen_ShouldPass() {
            // given
            String emailWithHyphen = "test-user@example.com";

            // when
            boolean result = validator.isValid(emailWithHyphen, context);

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("플러스 기호가 포함된 이메일은 검증을 통과한다")
        void emailWithPlus_ShouldPass() {
            // given
            String emailWithPlus = "test+tag@example.com";

            // when
            boolean result = validator.isValid(emailWithPlus, context);

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("퍼센트 기호가 포함된 이메일은 검증을 통과한다")
        void emailWithPercent_ShouldPass() {
            // given
            String emailWithPercent = "test%user@example.com";

            // when
            boolean result = validator.isValid(emailWithPercent, context);

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("복잡한 도메인 이메일은 검증을 통과한다")
        void complexDomainEmail_ShouldPass() {
            // given
            String complexEmail = "test@sub.example.co.kr";

            // when
            boolean result = validator.isValid(complexEmail, context);

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("짧은 도메인 이메일은 검증을 통과한다")
        void shortDomainEmail_ShouldPass() {
            // given
            String shortDomainEmail = "test@example.co";

            // when
            boolean result = validator.isValid(shortDomainEmail, context);

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("긴 도메인 이메일은 검증을 통과한다")
        void longDomainEmail_ShouldPass() {
            // given
            String longDomainEmail = "test@example.info";

            // when
            boolean result = validator.isValid(longDomainEmail, context);

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
            String nullEmail = null;

            // when
            boolean result = validator.isValid(nullEmail, context);

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("빈 문자열은 검증을 실패한다")
        void emptyString_ShouldFail() {
            // given
            String emptyEmail = "";

            // when
            boolean result = validator.isValid(emptyEmail, context);

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("@가 없는 이메일은 검증을 실패한다")
        void emailWithoutAt_ShouldFail() {
            // given
            String emailWithoutAt = "testexample.com";

            // when
            boolean result = validator.isValid(emailWithoutAt, context);

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("@가 여러 개인 이메일은 검증을 실패한다")
        void emailWithMultipleAt_ShouldFail() {
            // given
            String emailWithMultipleAt = "test@example@com";

            // when
            boolean result = validator.isValid(emailWithMultipleAt, context);

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("로컬 부분이 없는 이메일은 검증을 실패한다")
        void emailWithoutLocalPart_ShouldFail() {
            // given
            String emailWithoutLocalPart = "@example.com";

            // when
            boolean result = validator.isValid(emailWithoutLocalPart, context);

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("도메인 부분이 없는 이메일은 검증을 실패한다")
        void emailWithoutDomain_ShouldFail() {
            // given
            String emailWithoutDomain = "test@";

            // when
            boolean result = validator.isValid(emailWithoutDomain, context);

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("도메인에 점이 없는 이메일은 검증을 실패한다")
        void emailWithoutDomainDot_ShouldFail() {
            // given
            String emailWithoutDomainDot = "test@example";

            // when
            boolean result = validator.isValid(emailWithoutDomainDot, context);

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("도메인 확장자가 너무 긴 이메일은 검증을 실패한다")
        void emailWithTooLongExtension_ShouldFail() {
            // given
            String emailWithTooLongExtension = "test@example.loremipsumdolorsitamet";

            // when
            boolean result = validator.isValid(emailWithTooLongExtension, context);

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("공백이 포함된 이메일은 검증을 실패한다")
        void emailWithSpaces_ShouldFail() {
            // given
            String emailWithSpaces = "test user@example.com";

            // when
            boolean result = validator.isValid(emailWithSpaces, context);

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("특수문자가 포함된 이메일은 검증을 실패한다")
        void emailWithSpecialCharacters_ShouldFail() {
            // given
            String emailWithSpecialChars = "test!user@example.com";

            // when
            boolean result = validator.isValid(emailWithSpecialChars, context);

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("한글이 포함된 이메일은 검증을 실패한다")
        void emailWithKorean_ShouldFail() {
            // given
            String emailWithKorean = "테스트@example.com";

            // when
            boolean result = validator.isValid(emailWithKorean, context);

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("도메인에 한글이 포함된 이메일은 검증을 실패한다")
        void emailWithKoreanDomain_ShouldFail() {
            // given
            String emailWithKoreanDomain = "test@테스트.com";

            // when
            boolean result = validator.isValid(emailWithKoreanDomain, context);

            // then
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("경계값 테스트")
    class BoundaryTests {

        @Test
        @DisplayName("최소 길이 도메인 확장자(2자) 이메일은 검증을 통과한다")
        void minLengthExtension_ShouldPass() {
            // given
            String minLengthExtensionEmail = "test@example.co";

            // when
            boolean result = validator.isValid(minLengthExtensionEmail, context);

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("최대 길이 도메인 확장자(6자) 이메일은 검증을 통과한다")
        void maxLengthExtension_ShouldPass() {
            // given
            String maxLengthExtensionEmail = "test@example.abcdef";

            // when
            boolean result = validator.isValid(maxLengthExtensionEmail, context);

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("도메인 확장자가 1자인 이메일은 검증을 실패한다")
        void tooShortExtension_ShouldFail() {
            // given
            String tooShortExtensionEmail = "test@example.c";

            // when
            boolean result = validator.isValid(tooShortExtensionEmail, context);

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("도메인 확장자가 7자인 이메일은 검증을 실패한다")
        void tooLongExtension_ShouldFail() {
            // given
            String tooLongExtensionEmail = "test@example.domains";

            // when
            boolean result = validator.isValid(tooLongExtensionEmail, context);

            // then
            assertThat(result).isFalse();
        }
    }
}
