package git_kkalnane.backend.starbucks.member.validation.validator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import git_kkalnane.backend.starbucks.member.validation.annotation.ValidName;
import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class ValidNameValidatorTest {

    private ValidNameValidator validator;
    private ConstraintValidatorContext context;

    @BeforeEach
    void setUp() {
        validator = new ValidNameValidator();
        context = mock(ConstraintValidatorContext.class);
        validator.initialize(mock(ValidName.class));
    }

    @Nested
    @DisplayName("정상 케이스")
    class ValidCases {

        @Test
        @DisplayName("유효한 한글 이름은 검증을 통과한다")
        void validKoreanName_ShouldPass() {
            // given
            String validName = "홍길동";

            // when
            boolean result = validator.isValid(validName, context);

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
            String nullName = null;

            // when
            boolean result = validator.isValid(nullName, context);

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("빈 문자열은 검증을 실패한다")
        void emptyString_ShouldFail() {
            // given
            String emptyName = "";

            // when
            boolean result = validator.isValid(emptyName, context);

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("영문이 포함된 이름은 검증을 실패한다")
        void nameWithEnglish_ShouldFail() {
            // given
            String nameWithEnglish = "홍길동Kim";

            // when
            boolean result = validator.isValid(nameWithEnglish, context);

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("숫자가 포함된 이름은 검증을 실패한다")
        void nameWithNumbers_ShouldFail() {
            // given
            String nameWithNumbers = "홍길동123";

            // when
            boolean result = validator.isValid(nameWithNumbers, context);

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("특수문자가 포함된 이름은 검증을 실패한다")
        void nameWithSpecialCharacters_ShouldFail() {
            // given
            String nameWithSpecialChars = "홍길동!";

            // when
            boolean result = validator.isValid(nameWithSpecialChars, context);

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("공백이 포함된 이름은 검증을 실패한다")
        void nameWithSpaces_ShouldFail() {
            // given
            String nameWithSpaces = "홍 길동";

            // when
            boolean result = validator.isValid(nameWithSpaces, context);

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("영문만으로 구성된 이름은 검증을 실패한다")
        void englishOnlyName_ShouldFail() {
            // given
            String englishOnlyName = "HongGilDong";

            // when
            boolean result = validator.isValid(englishOnlyName, context);

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("숫자만으로 구성된 이름은 검증을 실패한다")
        void numbersOnlyName_ShouldFail() {
            // given
            String numbersOnlyName = "12345";

            // when
            boolean result = validator.isValid(numbersOnlyName, context);

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("특수문자만으로 구성된 이름은 검증을 실패한다")
        void specialCharsOnlyName_ShouldFail() {
            // given
            String specialCharsOnlyName = "!@#$%";

            // when
            boolean result = validator.isValid(specialCharsOnlyName, context);

            // then
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("경계값 테스트")
    class BoundaryTests {

        @Test
        @DisplayName("정확히 2자인 이름은 검증을 통과한다")
        void exactlyTwoCharacters_ShouldPass() {
            // given
            String twoCharName = "가나";

            // when
            boolean result = validator.isValid(twoCharName, context);

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("정확히 15자인 이름은 검증을 통과한다")
        void exactlyFifteenCharacters_ShouldPass() {
            // given
            String fifteenCharName = "김가나다라마바사아자차카타파하";

            // when
            boolean result = validator.isValid(fifteenCharName, context);

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("1자인 이름은 검증을 실패한다")
        void exactlyOneCharacter_ShouldFail() {
            // given
            String oneCharName = "가";

            // when
            boolean result = validator.isValid(oneCharName, context);

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("16자인 이름은 검증을 실패한다")
        void exactlySixteenCharacters_ShouldFail() {
            // given
            String sixteenCharName = "가나다라마바사아자차카타파하가나";

            // when
            boolean result = validator.isValid(sixteenCharName, context);

            // then
            assertThat(result).isFalse();
        }
    }
}
