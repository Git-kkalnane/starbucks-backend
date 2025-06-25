package git_kkalnane.backend.starbucks.auth.service;

import git_kkalnane.backend.starbucks._global.utils.Encryptor;
import git_kkalnane.backend.starbucks.auth.common.exception.AuthErrorCode;
import git_kkalnane.backend.starbucks.auth.common.exception.AuthException;
import git_kkalnane.backend.starbucks.auth.common.jwt.JwtTokenProvider;
import git_kkalnane.backend.starbucks.auth.common.jwt.dto.JwtToken;
import git_kkalnane.backend.starbucks.auth.common.jwt.dto.TokenInfo;
import git_kkalnane.backend.starbucks.auth.domain.AccessToken;
import git_kkalnane.backend.starbucks.auth.domain.RefreshToken;
import git_kkalnane.backend.starbucks.auth.dto.LoginDto;
import git_kkalnane.backend.starbucks.auth.dto.UserInfo;
import git_kkalnane.backend.starbucks.auth.dto.request.LoginRequest;
import git_kkalnane.backend.starbucks.auth.repository.AccessTokenRepository;
import git_kkalnane.backend.starbucks.auth.repository.RefreshTokenRepository;
import git_kkalnane.backend.starbucks.member.domain.Member;
import git_kkalnane.backend.starbucks.member.repository.MemberRepository;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final MemberRepository memberRepository;
    private final AccessTokenRepository accessTokenRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    private final JwtTokenProvider jwtTokenProvider;
    private final Encryptor encryptor;

    /**
     * LoginRequest를 바탕으로 토큰을 발행한 뒤 토큰과 사용자 정보를 반환하는 메서드
     *
     * @param request SignUpRequest 객체
     * @return 멤버의 이름을 담은 SignUpResponse 객체
     */
    public LoginDto login(LoginRequest request) {

        // 로그인 요청에 포함된 이메일을 가진 멤버가 존재하는지 조회
        // 이 과정에서 이메일이 존재하지 않을 경우 예외가 발생한다.
        Member member = memberRepository.findMemberByEmail(request.email())
                .orElseThrow(() -> new AuthException(AuthErrorCode.EMAIL_INVALID_EXCEPTION));

        // 로그인 요청에 포함된 비밀번호가 DB에 저장된 비밀번호와 일치하는지 검증
        if (!encryptor.isMatch(request.password(), member.getPassword())) {
            throw new AuthException(AuthErrorCode.PASSWORD_INVALID_EXCEPTION);
        }

        JwtToken tokens = jwtTokenProvider.createJwtToken(member.getId());
        saveAccessTokenToRepository(member.getId(), tokens.getAccessTokenInfo());
        saveRefreshTokenToRepository(member.getId(), tokens.getRefreshTokenInfo());

        UserInfo userInfo = UserInfo.of(member.getEmail(), member.getNickname());

        return LoginDto.of(tokens, userInfo);
    }

    /**
     * 생성된 accessToken을 accessTokenRepository에 저장하는 메서드
     *
     * @param memberId  사용자 ID (식별자)
     * @param tokenInfo TokenInfo DTO 인스턴스
     */
    @Transactional
    public void saveAccessTokenToRepository(Long memberId, TokenInfo tokenInfo) {
        Optional<AccessToken> maybeAccessToken = accessTokenRepository.findByMemberId(memberId);

        // maybeAccessToken의 값이 null일 경우 (DB에 해당 멤버의 토큰 존재 X) 새로 저장
        if (maybeAccessToken.isEmpty()) {
            accessTokenRepository.save(AccessToken.builder()
                    .memberId(memberId)
                    .token(tokenInfo.getToken())
                    .expiration(tokenInfo.getExpiration())
                    .build());
            return;
        }

        // maybeAccessToken의 값이 null이 아닐 경우 (DB에 해당 멤버의 토큰 존재) 업데이트
        AccessToken accessToken = maybeAccessToken.get();
        accessToken.modifyToken(tokenInfo.getToken());
        accessToken.modifyExpiration(tokenInfo.getExpiration());
    }

    /**
     * 생성된 refreshToken을 refreshTokenRepository에 저장하는 메서드
     *
     * @param memberId  사용자 ID (식별자)
     * @param tokenInfo TokenInfo DTO 인스턴스
     */
    @Transactional
    public void saveRefreshTokenToRepository(Long memberId, TokenInfo tokenInfo) {
        Optional<RefreshToken> maybeRefreshToken = refreshTokenRepository.findByMemberId(memberId);

        // maybeRefreshToken의 값이 null일 경우 (DB에 해당 멤버의 토큰 존재 X) 새로 저장
        if (maybeRefreshToken.isEmpty()) {
            refreshTokenRepository.save(RefreshToken.builder()
                    .memberId(memberId)
                    .token(tokenInfo.getToken())
                    .expiration(tokenInfo.getExpiration())
                    .build());
            return;
        }

        // maybeRefreshToken의 값이 null이 아닐 경우 (DB에 해당 멤버의 토큰 존재) 업데이트
        RefreshToken refreshToken = maybeRefreshToken.get();
        refreshToken.modifyToken(tokenInfo.getToken());
        refreshToken.modifyExpiration(tokenInfo.getExpiration());
    }

    /**
     * Request Header에 포함된 accessToken을 바탕으로 로그아웃을 수행하는 메서드
     *
     * @param memberId 로그아웃 사용자 ID (식별자)
     */
    @Transactional
    public void logout(Long memberId) {

        // 액세스 토큰에 대한 검증은 인터셉터에서 이루어진다.
        AccessToken accessToken = accessTokenRepository.findByMemberId(memberId).orElseThrow();
        RefreshToken refreshToken = refreshTokenRepository.findByMemberId(memberId)
                .orElseThrow(() -> new AuthException(AuthErrorCode.TOKEN_DOESNT_EXIST_IN_DB));

        // accessToken, refreshToken 엔티티의 토큰 값, 만료일시 초기화
        accessToken.modifyToken("");
        accessToken.modifyExpiration(new Date());

        refreshToken.modifyToken("");
        refreshToken.modifyExpiration(new Date());
    }

    /**
     * HTTP 요청의 헤더에 있는 AccessToken의 유효성을 검증하는 메서드이다.
     *
     * @param accessToken
     * @return bearerToken에 포함된 멤버 엔티티 식별자
     */
    public Long verifyTokenIncludedInRequest(String accessToken) {

        Long memberId = Long.parseLong(jwtTokenProvider.getMemberId(accessToken));

        // DB에 액세스 토큰이 존재하지 않으면 예외 발생
        AccessToken accessTokenObj = accessTokenRepository.findByMemberId(memberId)
                .orElseThrow(() -> new AuthException(AuthErrorCode.TOKEN_DOESNT_EXIST_IN_DB));

        // DB에 저장된 액세스 토큰값과 HTTP 요청에 포함된 토큰값이 일치하지 않으면 예외 발생
        if (!Objects.equals(accessTokenObj.getToken(), accessToken)) {
            throw new AuthException(AuthErrorCode.INVALID_TOKEN);
        }

        return accessTokenObj.getMemberId();
    }
}
