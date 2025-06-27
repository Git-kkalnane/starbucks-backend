package git_kkalnane.backend.starbucks.paycard.domain;

import git_kkalnane.backend.starbucks.member.domain.Member;
import git_kkalnane.backend.starbucks.paycard.common.exception.PayCardErrorCode;
import git_kkalnane.backend.starbucks.paycard.common.exception.PayCardException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("PayCard 도메인 테스트")
class PayCardTest {

    private Member member;
    private PayCard payCard;

    @BeforeEach
    void setUp() {
        member = Member.builder()
                .id(1L)
                .email("test@example.com")
                .build();

        payCard = PayCard.builder()
                .cardNumber("1234-5678-9012-3456")
                .cardAmount(10000)
                .member(member)
                .build();
    }

    @Test
    @DisplayName("카드_잔액_차감_성공")
    void decreaseCardAmount_WithSufficientBalance_DecreasesAmount() {
        // Given
        int amountToDecrease = 5000;

        // When
        payCard.decreaseCardAmount(amountToDecrease);

        // Then
        assertThat(payCard.getCardAmount()).isEqualTo(5000);
    }

    @Test
    @DisplayName("카드_잔액_부족시_예외_발생")
    void decreaseCardAmount_WithInsufficientBalance_ThrowsException() {
        // Given
        int amountToDecrease = 15000; // 잔액보다 큰 금액

        // When & Then
        assertThatThrownBy(() -> payCard.decreaseCardAmount(amountToDecrease))
                .isInstanceOf(PayCardException.class)
                .hasMessageContaining(PayCardErrorCode.NOT_ENOUGH_PAY_CARD_AMOUNT.getMessage());
        
        // 잔액이 변경되지 않았는지 확인
        assertThat(payCard.getCardAmount()).isEqualTo(10000);
    }

    @Test
    @DisplayName("카드_잔액_정확한_금액_차감시_0으로_변경")
    void decreaseCardAmount_WithExactBalance_DecreasesToZero() {
        // Given
        int amountToDecrease = 10000; // 정확한 잔액

        // When
        payCard.decreaseCardAmount(amountToDecrease);

        // Then
        assertThat(payCard.getCardAmount()).isEqualTo(0);
    }

    @Test
    @DisplayName("카드_잔액_0원_차감시_변화없음")
    void decreaseCardAmount_WithZeroAmount_NoChange() {
        // Given
        int amountToDecrease = 0;

        // When
        payCard.decreaseCardAmount(amountToDecrease);

        // Then
        assertThat(payCard.getCardAmount()).isEqualTo(10000);
    }

    @Test
    @DisplayName("카드_잔액_연속_차감_정상_처리")
    void decreaseCardAmount_WithMultipleDecreases_ProcessesCorrectly() {
        // Given
        int firstDecrease = 3000;
        int secondDecrease = 4000;

        // When
        payCard.decreaseCardAmount(firstDecrease);
        payCard.decreaseCardAmount(secondDecrease);

        // Then
        assertThat(payCard.getCardAmount()).isEqualTo(3000); // 10000 - 3000 - 4000
    }

    @Test
    @DisplayName("카드_생성시_기본_잔액_0으로_설정")
    void createPayCard_WithDefaultAmount_SetsZeroBalance() {
        // Given & When
        PayCard newPayCard = PayCard.builder()
                .cardNumber("9999-8888-7777-6666")
                .member(member)
                .build();

        // Then
        assertThat(newPayCard.getCardAmount()).isEqualTo(0);
    }

    @Test
    @DisplayName("카드_정보_정상_설정")
    void createPayCard_WithValidData_SetsCorrectInformation() {
        // Given & When
        PayCard newPayCard = PayCard.builder()
                .cardNumber("1111-2222-3333-4444")
                .cardAmount(5000)
                .member(member)
                .build();

        // Then
        assertThat(newPayCard.getCardNumber()).isEqualTo("1111-2222-3333-4444");
        assertThat(newPayCard.getCardAmount()).isEqualTo(5000);
        assertThat(newPayCard.getMember()).isEqualTo(member);
    }
} 