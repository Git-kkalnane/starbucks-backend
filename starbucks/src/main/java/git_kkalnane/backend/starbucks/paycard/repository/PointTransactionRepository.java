package git_kkalnane.backend.starbucks.paycard.repository;

import git_kkalnane.backend.starbucks.paycard.common.exception.PointTransactionErrorCode;
import git_kkalnane.backend.starbucks.paycard.common.exception.PointTransactionException;
import git_kkalnane.backend.starbucks.paycard.domain.PointTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PointTransactionRepository extends JpaRepository<PointTransaction, Long> {

    Optional<PointTransaction> findByMemberId(Long memberId);
    Optional<PointTransaction> findByPaymentId(Long paymentId);

    default PointTransaction getByMemberId(Long memberId) {
        return findByMemberId(memberId)
                .orElseThrow(() -> new PointTransactionException(PointTransactionErrorCode.POINT_TRANSACTION_NOT_FOUND));
    }

    default PointTransaction getByPaymentId(Long paymentId) {
        return findByMemberId(paymentId)
                .orElseThrow(() -> new PointTransactionException(PointTransactionErrorCode.POINT_TRANSACTION_NOT_FOUND));
    }
}
