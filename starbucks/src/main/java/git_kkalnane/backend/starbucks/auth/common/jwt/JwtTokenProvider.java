package git_kkalnane.backend.starbucks.auth.common.jwt;


import git_kkalnane.backend.starbucks.auth.common.exception.AuthErrorCode;
import git_kkalnane.backend.starbucks.auth.common.exception.AuthException;
import git_kkalnane.backend.starbucks.global.utils.GlobalLogger;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtTokenProvider {
    private final SecretKey secretKey;
    private final long REFRESH_TOKEN_EXPIRED;
    private final long ACCESS_TOKEN_EXPIRED;

    public JwtTokenProvider(String secretKey, long refreshTokenExpired, long accessTokenExpired) {
        this.REFRESH_TOKEN_EXPIRED = refreshTokenExpired;
        this.ACCESS_TOKEN_EXPIRED = accessTokenExpired;
        this.secretKey = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    public JwtToken createToken(String authId) {
        // 리프레시 토큰과 액세스 토큰을 생성
        String refreshToken = createRefreshToken(authId);
        String accessToken = createAccessToken(authId);

        return JwtToken.of(accessToken, refreshToken);
    }


    public String createRefreshToken(String authId) {
        return generateToken(authId, REFRESH_TOKEN_EXPIRED);
    }

    public String createAccessToken(String authId) {
        return generateToken(authId, ACCESS_TOKEN_EXPIRED);
    }

    public String createAccessTokenIfValid(String refreshToken) {
        String memberId = getMemberId(refreshToken);
        return createAccessToken(memberId);
    }

    private String generateToken(String memberId, long expireMills) {
        Date issuedAt = new Date(System.currentTimeMillis());
        Date expiredAt = new Date(System.currentTimeMillis() + expireMills);

        Map<String, Object> claims = new HashMap<>();

        claims.put("memberId", memberId);

        // 액세스 토큰 생성
        return Jwts.builder()
                .claims(claims)
                .issuedAt(issuedAt)
                .expiration(expiredAt)
                .signWith(secretKey)
                .compact();
    }

    public String getMemberId(String token) {
        try {
            Jws<Claims> claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token);

            return (String) claims.getPayload().get("memberId");
        } catch (MalformedJwtException | ExpiredJwtException | UnsupportedJwtException | IllegalArgumentException e) {
            GlobalLogger.error(e.getMessage());
            throw new AuthException(AuthErrorCode.INVALID_TOKEN);
        }
    }
}