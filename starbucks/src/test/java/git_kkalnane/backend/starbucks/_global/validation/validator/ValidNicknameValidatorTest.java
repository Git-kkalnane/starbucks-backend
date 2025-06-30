package git_kkalnane.backend.starbucks._global.validation.validator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import git_kkalnane.backend.starbucks._global.validation.annotation.ValidNickname;
import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class ValidNicknameValidatorTest {

    private ValidNicknameValidator validator;
    private ConstraintValidatorContext context;

    @BeforeEach
    void setUp() {
        validator = new ValidNicknameValidator();
        context = mock(ConstraintValidatorContext.class);
        validator.initialize(mock(ValidNickname.class));
    }

    @Nested
    @DisplayName("정상 케이스")
    class ValidCases {

        @Test
        @DisplayName("유효한 한글 닉네임은 검증을 통과한다")
        void validKoreanNickname_ShouldPass() {
            // given
            String validNickname = "길동이";

            // when
            boolean result = validator.isValid(validNickname, context);

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
            String nullNickname = null;

            // when
            boolean result = validator.isValid(nullNickname, context);

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("영문이 포함된 닉네임은 검증을 실패한다")
        void nicknameWithEnglish_ShouldFail() {
            // given
            String nicknameWithEnglish = "길동Kim";

            // when
            boolean result = validator.isValid(nicknameWithEnglish, context);

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("숫자가 포함된 닉네임은 검증을 실패한다")
        void nicknameWithNumbers_ShouldFail() {
            // given
            String nicknameWithNumbers = "길동123";

            // when
            boolean result = validator.isValid(nicknameWithNumbers, context);

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("특수문자가 포함된 닉네임은 검증을 실패한다")
        void nicknameWithSpecialCharacters_ShouldFail() {
            // given
            String nicknameWithSpecialChars = "길동!";

            // when
            boolean result = validator.isValid(nicknameWithSpecialChars, context);

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("공백이 포함된 닉네임은 검증을 실패한다")
        void nicknameWithSpaces_ShouldFail() {
            // given
            String nicknameWithSpaces = "길 동";

            // when
            boolean result = validator.isValid(nicknameWithSpaces, context);

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("영문만으로 구성된 닉네임은 검증을 실패한다")
        void englishOnlyNickname_ShouldFail() {
            // given
            String englishOnlyNickname = "GilDong";

            // when
            boolean result = validator.isValid(englishOnlyNickname, context);

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("숫자만으로 구성된 닉네임은 검증을 실패한다")
        void numbersOnlyNickname_ShouldFail() {
            // given
            String numbersOnlyNickname = "123";

            // when
            boolean result = validator.isValid(numbersOnlyNickname, context);

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("특수문자만으로 구성된 닉네임은 검증을 실패한다")
        void specialCharsOnlyNickname_ShouldFail() {
            // given
            String specialCharsOnlyNickname = "!@#";

            // when
            boolean result = validator.isValid(specialCharsOnlyNickname, context);

            // then
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("경계값 테스트")
    class BoundaryTests {

        @Test
        @DisplayName("정확히 1자인 닉네임은 검증을 통과한다")
        void exactlyOneCharacter_ShouldPass() {
            // given
            String oneCharNickname = "홍";

            // when
            boolean result = validator.isValid(oneCharNickname, context);

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("정확히 6자인 닉네임은 검증을 통과한다")
        void exactlySixCharacters_ShouldPass() {
            // given
            String sixCharNickname = "가나다라마바";

            // when
            boolean result = validator.isValid(sixCharNickname, context);

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("7자인 닉네임은 검증을 실패한다")
        void exactlySevenCharacters_ShouldFail() {
            // given
            String sevenCharNickname = "가나다라마바사";

            // when
            boolean result = validator.isValid(sevenCharNickname, context);

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("0자인 닉네임은 검증을 실패한다")
        void zeroCharacters_ShouldFail() {
            // given
            String zeroCharNickname = "";

            // when
            boolean result = validator.isValid(zeroCharNickname, context);

            // then
            assertThat(result).isFalse();
        }
    }
}
