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
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * JWT 생성 및 갱신과 관련된 기능들을 담당하는 컴포넌트 클래스
 */
@Component
public class JwtTokenProvider {

    private final SecretKey signingKey;

    @Value("${jwt.access-token-validity-in-milli-seconds}")
    private long ACCESS_TOKEN_EXPIRED;
    @Value("${jwt.refresh-token-validity-in-milli-seconds}")
    private long REFRESH_TOKEN_EXPIRED;

    public JwtTokenProvider(@Value("${jwt.secret}") String keyParam) {
        signingKey = Keys.hmacShaKeyFor(keyParam.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * JWT 토큰을 생성하여 반환하는 메서드
     *
     * @param memberId 멤버 테이블에 저장된 엔티티의 인덱스
     * @return JwtToken 객체
     */
    public JwtToken createJwtToken(Long memberId) {
        // 리프레시 토큰과 액세스 토큰을 생성
        TokenInfo accessTokenInfo = generateToken(memberId, ACCESS_TOKEN_EXPIRED);
        TokenInfo refreshTokenInfo = generateToken(memberId, REFRESH_TOKEN_EXPIRED);

        return JwtToken.of(accessTokenInfo, refreshTokenInfo);
    }

    /**
     * JJWT 라이브러리를 이용해 토큰을 생성하여 Token 인스턴스를 반환하는 메서드
     *
     * @param memberId    멤버 테이블에 저장된 엔티티의 인덱스
     * @param expireMills 토큰의 만료 시간
     * @return TokenInfo 인스턴스
     */
    private TokenInfo generateToken(Long memberId, long expireMills) {
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

        return TokenInfo.builder()
                .token(token)
                .expiration(expiredAt)
                .build();
    }

    // 액세스 토큰 재발급 기능 구현 시 수정할 예정
//    public String TokenIfValid(String refreshToken) {
//        String memberId = getMemberId(refreshToken);
//
//        return generateToken(Long.parseLong(memberId));
//    }

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