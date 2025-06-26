package git_kkalnane.backend.starbucks.paycard.service;


import git_kkalnane.backend.starbucks.paycard.domain.PayCard;
import git_kkalnane.backend.starbucks.paycard.repository.PayCardRepository;
import git_kkalnane.backend.starbucks.payment.domain.Payment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PayCardService {
    private final PayCardRepository payCardRepository;

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public PayCard pay(Payment payment) {
        PayCard payCard = payCardRepository.getByMemberId(payment.getMember().getId());

        payCard.decreaseCardAmount(payment.getAmountPaidByPoint());

        return payCard;
    }
}
