package git_kkalnane.backend.starbucks.auth.repository;

import git_kkalnane.backend.starbucks.auth.domain.AccessToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccessTokenRepository extends JpaRepository<AccessToken, Long> {
    void removeAccessTokenByMemberId(Long memberId);

    AccessToken findByMemberId(Long memberId);
}
