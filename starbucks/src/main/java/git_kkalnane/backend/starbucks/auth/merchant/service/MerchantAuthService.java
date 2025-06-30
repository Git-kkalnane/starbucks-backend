package git_kkalnane.backend.starbucks.auth.merchant.service;

import git_kkalnane.backend.starbucks._global.utils.Encryptor;
import git_kkalnane.backend.starbucks.auth.common.exception.AuthErrorCode;
import git_kkalnane.backend.starbucks.auth.common.exception.AuthException;
import git_kkalnane.backend.starbucks.auth.common.jwt.JwtTokenProvider;
import git_kkalnane.backend.starbucks.auth.common.jwt.dto.JwtToken;
import git_kkalnane.backend.starbucks.auth.common.jwt.dto.TokenInfo;
import git_kkalnane.backend.starbucks.auth.merchant.domain.MerchantRefreshToken;
import git_kkalnane.backend.starbucks.auth.merchant.dto.MerchantLoginDto;
import git_kkalnane.backend.starbucks.auth.common.dto.request.LoginRequest;
import git_kkalnane.backend.starbucks.auth.merchant.repository.MerchantRefreshTokenRepository;
import git_kkalnane.backend.starbucks.merchant.domain.Merchant;
import git_kkalnane.backend.starbucks.merchant.repository.MerchantRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
@Transactional
public class MerchantAuthService {

    private final MerchantRepository merchantRepository;
    private final MerchantRefreshTokenRepository merchantRefreshTokenRepository;

    private final JwtTokenProvider jwtTokenProvider;
    private final Encryptor encryptor;

    /**
     * LoginRequest를 바탕으로 토큰을 발행한 뒤 토큰과 사용자 정보를 반환하는 메서드
     *
     * @param request {@link LoginRequest}
     * @return {@link MerchantLoginDto}
     */
    public MerchantLoginDto login(LoginRequest request) {
        // 로그인 요청에 포함된 이메일을 가진 멤버가 존재하는지 조회
        // 이 과정에서 이메일이 존재하지 않을 경우 예외가 발생한다.
        Merchant merchant = merchantRepository.findByEmail(request.email())
                .orElseThrow(() -> new AuthException(AuthErrorCode.EMAIL_INVALID_EXCEPTION));

        // 로그인 요청에 포함된 비밀번호가 DB에 저장된 비밀번호와 일치하는지 검증
        if (!encryptor.isMatch(request.password(), merchant.getPasswordHash())) {
            throw new AuthException(AuthErrorCode.PASSWORD_INVALID_EXCEPTION);
        }

        JwtToken tokens = jwtTokenProvider.createJwtToken("MERCHANT", merchant.getId());
        saveRefreshTokenToRepository(merchant.getId(), tokens.getRefreshTokenInfo());

        String accessToken = tokens.getAccessTokenInfo().getToken();
        String refreshToken = tokens.getRefreshTokenInfo().getToken();

        return MerchantLoginDto.of(accessToken, refreshToken, merchant.getMerchantName(), merchant.getEmail());
    }

    /**
     * 생성된 refreshToken을 refreshTokenRepository에 저장하는 메서드
     *
     * @param merchantId 매장 ID (식별자)
     * @param tokenInfo TokenInfo DTO 인스턴스
     */
    @Transactional
    public void saveRefreshTokenToRepository(Long merchantId, TokenInfo tokenInfo) {
        Optional<MerchantRefreshToken> maybeRefreshToken = merchantRefreshTokenRepository.findByMemberId(merchantId);

        // maybeRefreshToken의 값이 null일 경우 (DB에 해당 멤버의 토큰 존재 X) 새로 저장
        if (maybeRefreshToken.isEmpty()) {
            merchantRefreshTokenRepository.save(MerchantRefreshToken.builder()
                    .memberId(merchantId)
                    .token(tokenInfo.getToken())
                    .expiration(tokenInfo.getExpiration())
                    .build());
            return;
        }

        // maybeRefreshToken의 값이 null이 아닐 경우 (DB에 해당 멤버의 토큰 존재) 업데이트
        MerchantRefreshToken refreshToken = maybeRefreshToken.get();
        refreshToken.modifyToken(tokenInfo.getToken());
        refreshToken.modifyExpiration(tokenInfo.getExpiration());
    }

    /**
     * HTTP 요청의 헤더에 있는 AccessToken의 유효성을 검증하는 메서드이다.
     *
     * @param token Authorization 헤더에 있는 토큰
     * @return 토큰의 페이로드에 포함된 멤버 엔티티 식별자
     */
    public Long verifyTokenIncludedInRequest(String token) {
        return Long.parseLong(jwtTokenProvider.getMemberId("MERCHANT", token));
    }
}
