package git_kkalnane.backend.starbucks.paycard.repository;

import git_kkalnane.backend.starbucks.paycard.common.exception.PayCardErrorCode;
import git_kkalnane.backend.starbucks.paycard.common.exception.PayCardException;
import git_kkalnane.backend.starbucks.paycard.domain.PayCard;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PayCardRepository extends JpaRepository<PayCard, Long> {

    Optional<PayCard> findByMemberId(Long memberId);

    default PayCard getByMemberId(Long memberId) {
        return findByMemberId(memberId)
                .orElseThrow(() -> new PayCardException(PayCardErrorCode.PAY_CARD_NOT_FOUND));
    }
}
