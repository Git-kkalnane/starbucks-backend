package git_kkalnane.backend.starbucks.auth.repository;

import git_kkalnane.backend.starbucks.auth.domain.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    void removeRefreshTokenByMemberId(Long memberId);
}
