package git_kkalnane.backend.starbucks.paycard.service;

import git_kkalnane.backend.starbucks.member.domain.Member;
import git_kkalnane.backend.starbucks.order.domain.Order;
import git_kkalnane.backend.starbucks.paycard.domain.PointAwardReasonMessage;
import git_kkalnane.backend.starbucks.paycard.domain.PointTransaction;
import git_kkalnane.backend.starbucks.paycard.domain.TransactionType;
import git_kkalnane.backend.starbucks.paycard.repository.PointTransactionRepository;
import git_kkalnane.backend.starbucks.payment.domain.Payment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("PointTransactionService 테스트")
class PointTransactionServiceTest {

    @Mock
    private PointTransactionRepository pointTransactionRepository;

    @InjectMocks
    private PointTransactionService pointTransactionService;

    private Member member;
    private Order order;
    private Payment payment;
    private PointTransaction savedPointTransaction;

    @BeforeEach
    void setUp() {
        member = Member.builder()
                .id(1L)
                .email("test@example.com")
                .build();

        order = Order.builder()
                .id(1L)
                .member(member)
                .build();

        payment = Payment.builder()
                .id(1L)
                .amountPaidByPoint(10000)
                .member(member)
                .order(order)
                .build();

        savedPointTransaction = PointTransaction.builder()
                .id(1L)
                .amount(500)
                .description(PointAwardReasonMessage.PAYMENT.getDescription())
                .transactionType(TransactionType.PAYMENT)
                .member(member)
                .payment(payment)
                .build();
    }

    @Test
    @DisplayName("결제_포인트_적립_생성_성공")
    void createPointTransaction_WithValidPayment_CreatesPointTransaction() {
        // Given
        PointAwardReasonMessage reasonMessage = PointAwardReasonMessage.PAYMENT;
        when(pointTransactionRepository.save(any(PointTransaction.class)))
                .thenReturn(savedPointTransaction);

        // When
        PointTransaction result = pointTransactionService.createPointTransaction(payment, reasonMessage);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getAmount()).isEqualTo(500.0);
        assertThat(result.getTransactionType()).isEqualTo(TransactionType.PAYMENT);
        assertThat(result.getMember()).isEqualTo(member);
        assertThat(result.getPayment()).isEqualTo(payment);
    }

    @Test
    @DisplayName("결제_포인트_적립_정확한_금액_계산")
    void createPointTransaction_WithValidPayment_CalculatesCorrectAmount() {
        // Given
        payment = Payment.builder()
                .id(1L)
                .amountPaidByPoint(20000) // 20000 * 0.05 = 1000
                .member(member)
                .order(order)
                .build();

        PointAwardReasonMessage reasonMessage = PointAwardReasonMessage.PAYMENT;

        savedPointTransaction = PointTransaction.builder()
                .id(1L)
                .amount(1000)
                .description(reasonMessage.getDescription())
                .transactionType(TransactionType.PAYMENT)
                .member(member)
                .payment(payment)
                .build();

        when(pointTransactionRepository.save(any(PointTransaction.class)))
                .thenReturn(savedPointTransaction);

        // When
        PointTransaction result = pointTransactionService.createPointTransaction(payment, reasonMessage);

        // Then
        assertThat(result.getAmount()).isEqualTo(1000.0);
    }

    @Test
    @DisplayName("결제_포인트_적립_리포지토리_저장_호출")
    void createPointTransaction_WithValidPayment_CallsRepositorySave() {
        // Given
        PointAwardReasonMessage reasonMessage = PointAwardReasonMessage.PAYMENT;
        ArgumentCaptor<PointTransaction> pointTransactionCaptor = ArgumentCaptor.forClass(PointTransaction.class);

        // When
        pointTransactionService.createPointTransaction(payment, reasonMessage);

        // Then
        verify(pointTransactionRepository).save(pointTransactionCaptor.capture());
        PointTransaction capturedTransaction = pointTransactionCaptor.getValue();
        
        assertThat(capturedTransaction.getAmount()).isEqualTo(500.0);
        assertThat(capturedTransaction.getDescription()).isEqualTo(reasonMessage.getDescription());
        assertThat(capturedTransaction.getTransactionType()).isEqualTo(TransactionType.PAYMENT);
        assertThat(capturedTransaction.getMember()).isEqualTo(member);
        assertThat(capturedTransaction.getPayment()).isEqualTo(payment);
    }

    @Test
    @DisplayName("결제_포인트_적립_0원_결제시_0포인트_적립")
    void createPointTransaction_WithZeroAmount_CreatesZeroPointTransaction() {
        // Given
        payment = Payment.builder()
                .id(1L)
                .amountPaidByPoint(0)
                .member(member)
                .order(order)
                .build();

        PointAwardReasonMessage reasonMessage = PointAwardReasonMessage.PAYMENT;

        savedPointTransaction = PointTransaction.builder()
                .id(1L)
                .amount(0)
                .description(reasonMessage.getDescription())
                .transactionType(TransactionType.PAYMENT)
                .member(member)
                .payment(payment)
                .build();

        when(pointTransactionRepository.save(any(PointTransaction.class)))
                .thenReturn(savedPointTransaction);

        // When
        PointTransaction result = pointTransactionService.createPointTransaction(payment, reasonMessage);

        // Then
        assertThat(result.getAmount()).isEqualTo(0.0);
    }
} 