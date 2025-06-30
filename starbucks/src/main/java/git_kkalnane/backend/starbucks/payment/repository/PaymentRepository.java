package git_kkalnane.backend.starbucks.payment.repository;

import git_kkalnane.backend.starbucks.payment.domain.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
}
