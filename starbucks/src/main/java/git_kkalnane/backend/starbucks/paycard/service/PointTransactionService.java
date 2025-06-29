package git_kkalnane.backend.starbucks.paycard.service;


import git_kkalnane.backend.starbucks.paycard.domain.PointAwardReasonMessage;
import git_kkalnane.backend.starbucks.paycard.domain.PointRewardRatio;
import git_kkalnane.backend.starbucks.paycard.domain.PointTransaction;
import git_kkalnane.backend.starbucks.paycard.domain.TransactionType;
import git_kkalnane.backend.starbucks.paycard.repository.PointTransactionRepository;
import git_kkalnane.backend.starbucks.payment.domain.Payment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PointTransactionService {
    private final PointTransactionRepository pointTransactionRepository;

    /**
     * 결제에 따른 포인트 적립을 생성합니다.
     *
     * @param payment 결제 정보
     * @param reasonMessage 포인트 적립 사유 메시지
     * @return 생성된 PointTransaction 객체
     */
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public PointTransaction createPointTransaction(Payment payment, PointAwardReasonMessage reasonMessage) {
        PointTransaction pointTransaction = PointTransaction.builder()
                .amount(PointRewardRatio.COMMON.getAppliedPointReward(payment.getAmountPaidByPoint())) // TODO: 적립 비율 별도 설정
                .description(reasonMessage.getDescription())
                .transactionType(TransactionType.PAYMENT)
                .member(payment.getMember())
                .payment(payment)
                .build();

        return pointTransactionRepository.save(pointTransaction);
    }
}
