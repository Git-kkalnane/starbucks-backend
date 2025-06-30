package git_kkalnane.backend.starbucks.merchant.repository;

import git_kkalnane.backend.starbucks.merchant.domain.Merchant;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MerchantRepository extends JpaRepository<Merchant, Long> {
    Optional<Merchant> findByEmail(String email);

    boolean existsByEmail(String email);
}
