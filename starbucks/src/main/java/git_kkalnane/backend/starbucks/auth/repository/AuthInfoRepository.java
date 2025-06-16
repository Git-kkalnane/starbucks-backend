package git_kkalnane.backend.starbucks.auth.repository;

import git_kkalnane.backend.starbucks.auth.common.exception.AuthErrorCode;
import git_kkalnane.backend.starbucks.auth.common.exception.AuthException;
import git_kkalnane.backend.starbucks.auth.domain.AuthInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AuthInfoRepository extends JpaRepository<AuthInfo, Long> {

    Optional<AuthInfo> findByMemberId(Long memberId);
    boolean existsAuthInfoByMemberId(Long memberId);

    Optional<AuthInfo> findByRefreshToken(String refreshToken);

    default AuthInfo getByMemberId(Long memberId){
        return this.findByMemberId(memberId)
                .orElseThrow(()->new AuthException(AuthErrorCode.UNAUTHORIZED_ACCESS, memberId));
    }

    default AuthInfo getByRefreshToken(String refreshToken){
        return this.findByRefreshToken(refreshToken)
                .orElseThrow(()-> new AuthException(AuthErrorCode.INVALID_TOKEN));
    }
}
