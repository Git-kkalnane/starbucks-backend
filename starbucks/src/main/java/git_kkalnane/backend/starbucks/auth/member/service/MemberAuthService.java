package git_kkalnane.backend.starbucks.auth.member.service;

import git_kkalnane.backend.starbucks._global.utils.Encryptor;
import git_kkalnane.backend.starbucks.auth.common.exception.AuthErrorCode;
import git_kkalnane.backend.starbucks.auth.common.exception.AuthException;
import git_kkalnane.backend.starbucks.auth.common.jwt.JwtTokenProvider;
import git_kkalnane.backend.starbucks.auth.common.jwt.dto.JwtToken;
import git_kkalnane.backend.starbucks.auth.common.jwt.dto.TokenInfo;
import git_kkalnane.backend.starbucks.auth.common.jwt.utils.TokenParser;
import git_kkalnane.backend.starbucks.auth.member.domain.MemberRefreshToken;
import git_kkalnane.backend.starbucks.auth.member.dto.MemberLoginDto;
import git_kkalnane.backend.starbucks.auth.common.dto.request.LoginRequest;
import git_kkalnane.backend.starbucks.auth.member.repository.MemberRefreshTokenRepository;
import git_kkalnane.backend.starbucks.member.domain.Member;
import git_kkalnane.backend.starbucks.member.repository.MemberRepository;
import java.util.Date;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberAuthService {

    private final MemberRepository memberRepository;
    private final MemberRefreshTokenRepository memberRefreshTokenRepository;

    private final JwtTokenProvider jwtTokenProvider;
    private final Encryptor encryptor;

    /**
     * LoginRequest를 바탕으로 토큰을 발행한 뒤 토큰과 사용자 정보를 반환하는 메서드
     *
     * @param request {@link LoginRequest}
     * @return {@link MemberLoginDto}
     */
    public MemberLoginDto login(LoginRequest request) {
        // 로그인 요청에 포함된 이메일을 가진 멤버가 존재하는지 조회
        // 이 과정에서 이메일이 존재하지 않을 경우 예외가 발생한다.
        Member member = memberRepository.findMemberByEmail(request.email())
                .orElseThrow(() -> new AuthException(AuthErrorCode.EMAIL_INVALID_EXCEPTION));

        // 로그인 요청에 포함된 비밀번호가 DB에 저장된 비밀번호와 일치하는지 검증
        if (!encryptor.isMatch(request.password(), member.getPassword())) {
            throw new AuthException(AuthErrorCode.PASSWORD_INVALID_EXCEPTION);
        }

        JwtToken tokens = jwtTokenProvider.createJwtToken("MEMBER", member.getId());
        saveRefreshTokenToRepository(member.getId(), tokens.getRefreshTokenInfo());

        String accessToken = tokens.getAccessTokenInfo().getToken();
        String refreshToken = tokens.getRefreshTokenInfo().getToken();

        return MemberLoginDto.of(accessToken, refreshToken, member.getName(), member.getNickname(), member.getEmail());
    }

    /**
     * 생성된 refreshToken을 refreshTokenRepository에 저장하는 메서드
     *
     * @param memberId  사용자 ID (식별자)
     * @param tokenInfo TokenInfo DTO 인스턴스
     */
    @Transactional
    public void saveRefreshTokenToRepository(Long memberId, TokenInfo tokenInfo) {
        Optional<MemberRefreshToken> maybeRefreshToken = memberRefreshTokenRepository.findByMemberId(memberId);

        // maybeRefreshToken의 값이 null일 경우 (DB에 해당 멤버의 토큰 존재 X) 새로 저장
        if (maybeRefreshToken.isEmpty()) {
            memberRefreshTokenRepository.save(MemberRefreshToken.builder()
                    .memberId(memberId)
                    .token(tokenInfo.getToken())
                    .expiration(tokenInfo.getExpiration())
                    .build());
            return;
        }

        // maybeRefreshToken의 값이 null이 아닐 경우 (DB에 해당 멤버의 토큰 존재) 업데이트
        MemberRefreshToken memberRefreshToken = maybeRefreshToken.get();
        memberRefreshToken.modifyToken(tokenInfo.getToken());
        memberRefreshToken.modifyExpiration(tokenInfo.getExpiration());
    }

    /**
     * Request Header에 포함된 accessToken을 바탕으로 로그아웃을 수행하는 메서드
     *
     * @param memberId 로그아웃 사용자 ID (식별자)
     */
    @Transactional
    public void logout(Long memberId) {
        MemberRefreshToken memberRefreshToken = memberRefreshTokenRepository.findByMemberId(memberId)
                .orElseThrow(() -> new AuthException(AuthErrorCode.TOKEN_NOT_FOUND_IN_DB));

        memberRefreshToken.modifyToken("");
        memberRefreshToken.modifyExpiration(new Date());
    }

    /**
     * HTTP 요청의 헤더에 있는 AccessToken의 유효성을 검증하는 메서드이다.
     *
     * @param token Authorization 헤더에 있는 토큰
     * @return 토큰의 페이로드에 포함된 멤버 엔티티 식별자
     */
    public Long verifyTokenIncludedInRequest(String token) {
        return Long.parseLong(jwtTokenProvider.getMemberId("MEMBER", token));
    }

    /**
     * 매개변수로 들어온 리프레쉬 토큰의 유효셩을 검증한 뒤 새로운 액세스 토큰을 생성하여 반환하는 메서드
     *
     * @param refreshToken 리프레쉬 토큰
     * @param memberId 사용자 ID (식별자)
     * @return 재발급된 액세스 토큰
     */
    public TokenInfo reissueAccessToken(String refreshToken, Long memberId) {
        // DB에 있는 해당 멤버의 토큰을 조회, 만약 존재하지 않을 경우 예외 발생
        MemberRefreshToken memberRefreshTokenInDB = memberRefreshTokenRepository.findByMemberId(memberId)
                .orElseThrow(() -> new AuthException(AuthErrorCode.TOKEN_NOT_FOUND_IN_DB));

        String plainToken = TokenParser.removeBearerTokenPrefix(refreshToken);

        // DB에 있는 토큰과 HTTP 요청 헤더에 있는 토큰이 일치하지 않으면 예외 발생
        if (!memberRefreshTokenInDB.validateToken(plainToken)) {
            throw new AuthException(AuthErrorCode.INVALID_TOKEN);
        }

        return jwtTokenProvider.reissueAccessTokenIfRefreshTokenIsValid("MEMBER", plainToken);
    }
}
