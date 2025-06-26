package git_kkalnane.backend.starbucks.payment.service;


import git_kkalnane.backend.starbucks.order.domain.Order;
import git_kkalnane.backend.starbucks.paycard.domain.PointAwardReasonMessage;
import git_kkalnane.backend.starbucks.paycard.service.PayCardService;
import git_kkalnane.backend.starbucks.paycard.service.PointTransactionService;
import git_kkalnane.backend.starbucks.payment.domain.Payment;
import git_kkalnane.backend.starbucks.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final PayCardService payCardService;
    private final PointTransactionService pointTransactionService;
    private final PaymentRepository paymentRepository;

    /**
     * 결제를 처리합니다.
     *
     * @param order 결제할 주문 정보
     */
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void processPayment(Order order) {
        int amount = getAmountFromOrder(order);
        Payment payment = createPayment(order, amount);

        // 결제
        payCardService.pay(payment);

        // 결제 포인트 적립
        pointTransactionService.createPointTransaction(payment, PointAwardReasonMessage.PAYMENT);

        // 결제 정보 저장
        paymentRepository.save(payment);
    }

    /**
     * 주문에 대한 결제 정보를 생성합니다.
     *
     * @param order 결제할 주문 정보
     * @param amount 결제 금액
     * @return 생성된 Payment 객체
     */
    private Payment createPayment(Order order, int amount) {
        return Payment.builder()
                .amountPaidByPoint(amount)
                .member(order.getMember())
                .order(order)
                .build();
    }

    /**
     * 주문에서 결제 금액을 계산합니다.
     *
     * @param order 결제할 주문 정보
     * @return 주문의 총 결제 금액
     */
    private int getAmountFromOrder(Order order) {
        return order.getOrderItems().stream()
                .mapToInt(orderItem -> orderItem.getOrderItemQuantity() * orderItem.getUnitPrice())
                .sum();
    }
}
