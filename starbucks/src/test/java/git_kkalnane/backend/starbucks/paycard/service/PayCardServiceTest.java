package git_kkalnane.backend.starbucks.paycard.service;

import git_kkalnane.backend.starbucks.member.domain.Member;
import git_kkalnane.backend.starbucks.paycard.common.exception.PayCardErrorCode;
import git_kkalnane.backend.starbucks.paycard.common.exception.PayCardException;
import git_kkalnane.backend.starbucks.paycard.domain.PayCard;
import git_kkalnane.backend.starbucks.paycard.repository.PayCardRepository;
import git_kkalnane.backend.starbucks.payment.domain.Payment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("PayCardService 테스트")
class PayCardServiceTest {

    @Mock
    private PayCardRepository payCardRepository;

    @InjectMocks
    private PayCardService payCardService;

    private Member member;
    private PayCard payCard;
    private Payment payment;

    @BeforeEach
    void setUp() {
        member = Member.builder()
                .id(1L)
                .email("test@example.com")
                .build();

        payCard = PayCard.builder()
                .id(1L)
                .cardNumber("1234-5678-9012-3456")
                .cardAmount(10000)
                .member(member)
                .build();

        payment = Payment.builder()
                .id(1L)
                .amountPaidByPoint(5000)
                .member(member)
                .build();
    }

    @Test
    @DisplayName("결제_충분한_잔액_있을때_카드잔액_차감_성공")
    void pay_WithSufficientBalance_DecreasesCardAmount() {
        // Given
        when(payCardRepository.getByMemberId(anyLong())).thenReturn(payCard);

        // When
        PayCard result = payCardService.pay(payment);

        // Then
        assertThat(result.getCardAmount()).isEqualTo(5000);
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("결제_잔액_부족할때_예외_발생")
    void pay_WithInsufficientBalance_ThrowsException() {
        // Given
        payment = Payment.builder()
                .id(1L)
                .amountPaidByPoint(15000) // 잔액보다 큰 금액
                .member(member)
                .build();

        when(payCardRepository.getByMemberId(anyLong())).thenReturn(payCard);

        // When & Then
        assertThatThrownBy(() -> payCardService.pay(payment))
                .isInstanceOf(PayCardException.class)
                .hasMessageContaining(PayCardErrorCode.NOT_ENOUGH_PAY_CARD_AMOUNT.getMessage());
    }

    @Test
    @DisplayName("결제_정확한_잔액일때_카드잔액_0으로_차감")
    void pay_WithExactBalance_DecreasesCardAmountToZero() {
        // Given
        payment = Payment.builder()
                .id(1L)
                .amountPaidByPoint(10000) // 정확한 잔액
                .member(member)
                .build();

        when(payCardRepository.getByMemberId(anyLong())).thenReturn(payCard);

        // When
        PayCard result = payCardService.pay(payment);

        // Then
        assertThat(result.getCardAmount()).isEqualTo(0);
    }

    @Test
    @DisplayName("결제_0원_결제시_카드잔액_변화없음")
    void pay_WithZeroAmount_NoChangeInCardAmount() {
        // Given
        payment = Payment.builder()
                .id(1L)
                .amountPaidByPoint(0)
                .member(member)
                .build();

        when(payCardRepository.getByMemberId(anyLong())).thenReturn(payCard);

        // When
        PayCard result = payCardService.pay(payment);

        // Then
        assertThat(result.getCardAmount()).isEqualTo(10000);
    }
} 