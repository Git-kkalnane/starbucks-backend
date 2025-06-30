package git_kkalnane.backend.starbucks.auth.merchant.repository;

import git_kkalnane.backend.starbucks.auth.merchant.domain.MerchantRefreshToken;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MerchantRefreshTokenRepository extends JpaRepository<MerchantRefreshToken, Long> {
    Optional<MerchantRefreshToken> findByMemberId(Long memberId);
}
