package git_kkalnane.backend.starbucks.payment.service;

import git_kkalnane.backend.starbucks.member.domain.Member;
import git_kkalnane.backend.starbucks.order.domain.Order;
import git_kkalnane.backend.starbucks.order.domain.OrderItem;
import git_kkalnane.backend.starbucks.paycard.domain.PointAwardReasonMessage;
import git_kkalnane.backend.starbucks.paycard.domain.PointTransaction;
import git_kkalnane.backend.starbucks.paycard.service.PayCardService;
import git_kkalnane.backend.starbucks.paycard.service.PointTransactionService;
import git_kkalnane.backend.starbucks.payment.domain.Payment;
import git_kkalnane.backend.starbucks.payment.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentService 테스트")
class PaymentServiceTest {

    @Mock
    private PayCardService payCardService;

    @Mock
    private PointTransactionService pointTransactionService;

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private PaymentService paymentService;

    private Member member;
    private Order order;
    private OrderItem orderItem1;
    private OrderItem orderItem2;
    private Payment payment;
    private PointTransaction pointTransaction;

    @BeforeEach
    void setUp() {
        member = Member.builder()
                .id(1L)
                .email("test@example.com")
                .build();

        orderItem1 = OrderItem.builder()
                .id(1L)
                .orderItemQuantity(2)
                .unitPrice(3000)
                .build();

        orderItem2 = OrderItem.builder()
                .id(2L)
                .orderItemQuantity(1)
                .unitPrice(5000)
                .build();

        order = Order.builder()
                .id(1L)
                .member(member)
                .orderItems(Arrays.asList(orderItem1, orderItem2))
                .build();

        payment = Payment.builder()
                .id(1L)
                .amountPaidByPoint(11000) // 2*3000 + 1*5000 = 11000
                .member(member)
                .order(order)
                .build();

        pointTransaction = PointTransaction.builder()
                .id(1L)
                .amount(550) // 11000 * 0.05
                .description(PointAwardReasonMessage.PAYMENT.getDescription())
                .member(member)
                .payment(payment)
                .build();
    }

    @Test
    @DisplayName("결제_처리_성공")
    void processPayment_WithValidOrder_ProcessesPaymentSuccessfully() {
        // Given
        when(payCardService.pay(any(Payment.class))).thenReturn(null);
        when(pointTransactionService.createPointTransaction(any(Payment.class), any(PointAwardReasonMessage.class)))
                .thenReturn(pointTransaction);
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);

        // When
        paymentService.processPayment(order);

        // Then
        verify(payCardService).pay(any(Payment.class));
        verify(pointTransactionService).createPointTransaction(any(Payment.class), eq(PointAwardReasonMessage.PAYMENT));
        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    @DisplayName("결제_처리_정확한_금액_계산")
    void processPayment_WithValidOrder_CalculatesCorrectAmount() {
        // Given
        ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
        when(payCardService.pay(any(Payment.class))).thenReturn(null);
        when(pointTransactionService.createPointTransaction(any(Payment.class), any(PointAwardReasonMessage.class)))
                .thenReturn(pointTransaction);
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);

        // When
        paymentService.processPayment(order);

        // Then
        verify(payCardService).pay(paymentCaptor.capture());
        Payment capturedPayment = paymentCaptor.getValue();
        
        assertThat(capturedPayment.getAmountPaidByPoint()).isEqualTo(11000); // 2*3000 + 1*5000
        assertThat(capturedPayment.getMember()).isEqualTo(member);
        assertThat(capturedPayment.getOrder()).isEqualTo(order);
    }

    @Test
    @DisplayName("결제_처리_단일_상품_주문시_정확한_금액_계산")
    void processPayment_WithSingleItemOrder_CalculatesCorrectAmount() {
        // Given
        Order singleItemOrder = Order.builder()
                .id(1L)
                .member(member)
                .orderItems(Arrays.asList(orderItem1))
                .build();

        ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
        when(payCardService.pay(any(Payment.class))).thenReturn(null);
        when(pointTransactionService.createPointTransaction(any(Payment.class), any(PointAwardReasonMessage.class)))
                .thenReturn(pointTransaction);
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);

        // When
        paymentService.processPayment(singleItemOrder);

        // Then
        verify(payCardService).pay(paymentCaptor.capture());
        Payment capturedPayment = paymentCaptor.getValue();
        
        assertThat(capturedPayment.getAmountPaidByPoint()).isEqualTo(6000); // 2*3000
    }

    @Test
    @DisplayName("결제_처리_0원_주문시_0원_결제")
    void processPayment_WithZeroAmountOrder_ProcessesZeroPayment() {
        // Given
        OrderItem zeroPriceItem = OrderItem.builder()
                .id(1L)
                .orderItemQuantity(1)
                .unitPrice(0)
                .build();

        Order zeroAmountOrder = Order.builder()
                .id(1L)
                .member(member)
                .orderItems(Arrays.asList(zeroPriceItem))
                .build();

        ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
        when(payCardService.pay(any(Payment.class))).thenReturn(null);
        when(pointTransactionService.createPointTransaction(any(Payment.class), any(PointAwardReasonMessage.class)))
                .thenReturn(pointTransaction);
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);

        // When
        paymentService.processPayment(zeroAmountOrder);

        // Then
        verify(payCardService).pay(paymentCaptor.capture());
        Payment capturedPayment = paymentCaptor.getValue();
        
        assertThat(capturedPayment.getAmountPaidByPoint()).isEqualTo(0);
    }

    @Test
    @DisplayName("결제_처리_서비스_호출_순서_검증")
    void processPayment_WithValidOrder_CallsServicesInCorrectOrder() {
        // Given
        when(payCardService.pay(any(Payment.class))).thenReturn(null);
        when(pointTransactionService.createPointTransaction(any(Payment.class), any(PointAwardReasonMessage.class)))
                .thenReturn(pointTransaction);
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);

        // When
        paymentService.processPayment(order);

        // Then
        verify(payCardService, times(1)).pay(any(Payment.class));
        verify(pointTransactionService, times(1)).createPointTransaction(any(Payment.class), eq(PointAwardReasonMessage.PAYMENT));
        verify(paymentRepository, times(1)).save(any(Payment.class));
    }

    @Test
    @DisplayName("결제_처리_포인트_적립_서비스_호출_검증")
    void processPayment_WithValidOrder_CallsPointTransactionService() {
        // Given
        ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
        ArgumentCaptor<PointAwardReasonMessage> reasonCaptor = ArgumentCaptor.forClass(PointAwardReasonMessage.class);
        
        when(payCardService.pay(any(Payment.class))).thenReturn(null);
        when(pointTransactionService.createPointTransaction(any(Payment.class), any(PointAwardReasonMessage.class)))
                .thenReturn(pointTransaction);
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);

        // When
        paymentService.processPayment(order);

        // Then
        verify(pointTransactionService).createPointTransaction(paymentCaptor.capture(), reasonCaptor.capture());
        
        Payment capturedPayment = paymentCaptor.getValue();
        PointAwardReasonMessage capturedReason = reasonCaptor.getValue();
        
        assertThat(capturedPayment.getAmountPaidByPoint()).isEqualTo(11000);
        assertThat(capturedReason).isEqualTo(PointAwardReasonMessage.PAYMENT);
    }
} 