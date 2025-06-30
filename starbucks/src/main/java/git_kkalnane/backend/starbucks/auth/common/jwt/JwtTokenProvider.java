package git_kkalnane.backend.starbucks.auth.common.jwt;


import git_kkalnane.backend.starbucks._global.utils.GlobalLogger;
import git_kkalnane.backend.starbucks.auth.common.exception.AuthErrorCode;
import git_kkalnane.backend.starbucks.auth.common.exception.AuthException;
import git_kkalnane.backend.starbucks.auth.common.jwt.dto.JwtToken;
import git_kkalnane.backend.starbucks.auth.common.jwt.dto.TokenInfo;
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

/**
 * JWT 생성 및 갱신과 관련된 기능들을 담당하는 컴포넌트 클래스
 */
@Component
public class JwtTokenProvider {

    private final SecretKey memberSigningKey;
    private final SecretKey merchantSigningKey;

    @Value("${jwt.access-token-validity-in-milli-seconds}")
    private long ACCESS_TOKEN_EXPIRED;
    @Value("${jwt.refresh-token-validity-in-milli-seconds}")
    private long REFRESH_TOKEN_EXPIRED;

    public JwtTokenProvider(@Value("${jwt.member-secret}") String memberSecretKey,
                            @Value("${jwt.merchant-secret}") String merchantSecretKey) {
        memberSigningKey = Keys.hmacShaKeyFor(memberSecretKey.getBytes(StandardCharsets.UTF_8));
        merchantSigningKey = Keys.hmacShaKeyFor(merchantSecretKey.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * JWT 토큰을 생성하여 반환하는 메서드
     *
     * @param subject MEMBER 또는 MERCHANT
     * @param id      엔티티의 인덱스
     * @return {@link JwtToken}
     */
    public JwtToken createJwtToken(String subject, Long id) {
        // 리프레시 토큰과 액세스 토큰을 생성
        TokenInfo accessTokenInfo = null;
        TokenInfo refreshTokenInfo = null;

        if (Objects.equals(subject, "MEMBER")) {
            accessTokenInfo = generateToken(id, memberSigningKey, ACCESS_TOKEN_EXPIRED);
            refreshTokenInfo = generateToken(id, memberSigningKey, REFRESH_TOKEN_EXPIRED);
        } else if (Objects.equals(subject, "MERCHANT")) {
            accessTokenInfo = generateToken(id, merchantSigningKey, ACCESS_TOKEN_EXPIRED);
            refreshTokenInfo = generateToken(id, merchantSigningKey, REFRESH_TOKEN_EXPIRED);
        }

        return JwtToken.of(accessTokenInfo, refreshTokenInfo);
    }

    /**
     * JJWT 라이브러리를 이용해 토큰을 생성하여 Token 인스턴스를 반환하는 메서드
     *
     * @param id          멤버 테이블에 저장된 엔티티의 인덱스
     * @param expireMills 토큰의 만료 시간
     * @return {@link TokenInfo}
     */
    private TokenInfo generateToken(Long id, SecretKey secretKey, long expireMills) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", id);

        Date issuedAt = new Date(System.currentTimeMillis());
        Date expiredAt = new Date(System.currentTimeMillis() + expireMills);

        // 토큰 생성
        String token = Jwts.builder()
                .claims(claims)
                .issuedAt(issuedAt)
                .expiration(expiredAt)
                .signWith(secretKey)
                .compact();

        return TokenInfo.builder()
                .token(token)
                .expiration(expiredAt)
                .build();
    }

    /**
     * 매개변수로 주어진 리프레쉬 토큰을 이용해 액세스 토큰을 재발급하여 반환하는 메서드
     *
     * @param subject      MEMBER 또는 MERCHANT
     * @param refreshToken 리프레쉬 토큰
     * @return 재발급된 액세스 토큰
     */
    public TokenInfo reissueAccessTokenIfRefreshTokenIsValid(String subject, String refreshToken) {
        if (Objects.equals(subject, "MEMBER")) {
            String id = getMemberId("MEMBER", refreshToken);
            return generateToken(Long.parseLong(id), memberSigningKey, ACCESS_TOKEN_EXPIRED);
        }

        String id = getMemberId("MERCHANT", refreshToken);
        return generateToken(Long.parseLong(id), merchantSigningKey, ACCESS_TOKEN_EXPIRED);
    }

    /**
     * 매개변수로 주어진 토큰의 Payload에서 회원 엔티티의 인덱스를 추출하여 반환하는 메서드
     *
     * @param subject MEMBER 또는 MERCHANT
     * @param token   String 타입의 토큰
     * @return id - 회원 테이블에 저장된 엔티티의 인덱스
     */
    public String getMemberId(String subject, String token) {
        try {
            if (Objects.equals(subject, "MEMBER")) {
                Jws<Claims> claims = Jwts.parser()
                        .verifyWith(memberSigningKey)
                        .build()
                        .parseSignedClaims(token);

                return String.valueOf(claims.getPayload().get("id"));
            }

            Jws<Claims> claims = Jwts.parser()
                    .verifyWith(merchantSigningKey)
                    .build()
                    .parseSignedClaims(token);

            return String.valueOf(claims.getPayload().get("id"));
        } catch (MalformedJwtException e) {
            // JWT 토큰 형식이 잘못된 경우
            GlobalLogger.error(e.getMessage());
            throw new AuthException(AuthErrorCode.MALFORMED_TOKEN);
        } catch (ExpiredJwtException e) {
            // JWT 토큰이 만료된 경우
            throw new AuthException(AuthErrorCode.EXPIRED_TOKEN);
        } catch (UnsupportedJwtException e) {
            // 지원되지 않는 JWT 형식인 경우
            throw new AuthException(AuthErrorCode.UNSUPPORTED_TOKEN);
        } catch (IllegalArgumentException e) {
            // 잘못된 파라미터가 전달된 경우
            throw new AuthException(AuthErrorCode.INVALID_TOKEN_PARAMETER);
        }
    }
}