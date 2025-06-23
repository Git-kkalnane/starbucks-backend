package git_kkalnane.backend.starbucks.auth.common.jwt;


import git_kkalnane.backend.starbucks._global.utils.GlobalLogger;
import git_kkalnane.backend.starbucks.auth.common.exception.AuthErrorCode;
import git_kkalnane.backend.starbucks.auth.common.exception.AuthException;
import git_kkalnane.backend.starbucks.auth.domain.AccessToken;
import git_kkalnane.backend.starbucks.auth.domain.RefreshToken;
import git_kkalnane.backend.starbucks.auth.repository.AccessTokenRepository;
import git_kkalnane.backend.starbucks.auth.repository.RefreshTokenRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {

    public static final String ACCESS_PREFIX_STRING = "Bearer ";
    public static final String ACCESS_HEADER_STRING = "Authorization";
    private final AccessTokenRepository accessTokenRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private SecretKey signingKey;
    @Value("${jwt.access-token-validity-in-milli-seconds}")
    private long ACCESS_TOKEN_EXPIRED;
    @Value("${jwt.refresh-token-validity-in-milli-seconds}")
    private long REFRESH_TOKEN_EXPIRED;

    public JwtTokenProvider(@Value("${jwt.secret}") String keyParam,
                            AccessTokenRepository accessTokenRepository,
                            RefreshTokenRepository refreshTokenRepository) {

        signingKey = Keys.hmacShaKeyFor(keyParam.getBytes(StandardCharsets.UTF_8));

        this.accessTokenRepository = accessTokenRepository;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    /**
     * JWT 토큰을 생성하여 반환하는 메서드
     *
     * @param memberId 멤버 테이블에 저장된 엔티티의 인덱스
     * @return JwtToken 객체
     */
    public JwtToken createJwtToken(Long memberId) {
        // 리프레시 토큰과 액세스 토큰을 생성
        String accessToken = createAccessToken(memberId);
        String refreshToken = createRefreshToken(memberId);

        return JwtToken.of(accessToken, refreshToken);
    }

    /**
     * accessToken을 생성하는 메서드. 외부에서는 refreshToken을 이용해 accessToken 재발급 요청이 들어왔을 때 호출된다.
     *
     * @param memberId 멤버 테이블에 저장된 엔티티의 인덱스
     * @return String 타입의 accessToken
     */
    private String createAccessToken(Long memberId) {
        return generateToken("accessToken", memberId, ACCESS_TOKEN_EXPIRED);
    }

    /**
     * refreshToken을 생성하는 메서드. 외부에서 호출해선 안된다.
     *
     * @param memberId 멤버 테이블에 저장된 엔티티의 인덱스
     * @return String 타입의 refreshToken
     */
    private String createRefreshToken(Long memberId) {
        return generateToken("refreshToken", memberId, REFRESH_TOKEN_EXPIRED);
    }

    /**
     * @param type        토큰의 종류 (accessToken, refreshToken)
     * @param memberId    멤버 테이블에 저장된 엔티티의 인덱스
     * @param expireMills 토큰의 만료 시간
     * @return String 타입의 토큰
     */
    private String generateToken(String type, Long memberId, long expireMills) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("memberId", memberId);

        Date issuedAt = new Date(System.currentTimeMillis());
        Date expiredAt = new Date(System.currentTimeMillis() + expireMills);

        // 토큰 생성
        String token = Jwts.builder()
                .claims(claims)
                .issuedAt(issuedAt)
                .expiration(expiredAt)
                .signWith(signingKey)
                .compact();

        // 액세스 토큰일 경우 토큰 앞에 Bearer 추가
        if (Objects.equals(type, "accessToken")) {
            token = ACCESS_PREFIX_STRING + token;
        }

        // 토큰을 리파지토리에 저장
        saveTokenToRepository(type, memberId, token, expiredAt);

        return token;
    }

    /**
     * 생성된 토큰을 리파지토리에 저장하는 메서드
     *
     * @param type      토큰의 종류 (accessToken, refreshToken)
     * @param memberId  사용자 ID (식별자)
     * @param token     생성된 토큰
     * @param expiredAt 토큰 만료시점
     */
    private void saveTokenToRepository(String type, Long memberId, String token, Date expiredAt) {
        if (Objects.equals(type, "accessToken")) {
            accessTokenRepository.removeAccessTokenByMemberId(memberId);
            accessTokenRepository.save(AccessToken.builder()
                    .memberId(memberId)
                    .token(token)
                    .expiration(expiredAt)
                    .build());
            return;
        }

        refreshTokenRepository.removeRefreshTokenByMemberId(memberId);
        refreshTokenRepository.save(RefreshToken.builder()
                .memberId(memberId)
                .token(token)
                .expiration(expiredAt)
                .build());
    }

    public String createAccessTokenIfValid(String refreshToken) {
        String memberId = getMemberId(refreshToken);
        return createAccessToken(Long.parseLong(memberId));
    }

    /**
     * 매개변수로 주어진 토큰의 Payload에서 회원 엔티티의 인덱스를 추출하여 반환하는 메서드
     *
     * @param token String 타입의 토큰
     * @return memberId - 회원 테이블에 저장된 엔티티의 인덱스
     */
    public String getMemberId(String token) {
        try {
            Jws<Claims> claims = Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token);

            return String.valueOf(claims.getPayload().get("memberId"));
        } catch (MalformedJwtException | ExpiredJwtException | UnsupportedJwtException | IllegalArgumentException e) {
            GlobalLogger.error(e.getMessage());
            throw new AuthException(AuthErrorCode.INVALID_TOKEN);
        }
    }
}