package git_kkalnane.backend.starbucks.auth.common.jwt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import git_kkalnane.backend.starbucks.auth.common.exception.AuthErrorCode;
import git_kkalnane.backend.starbucks.auth.common.exception.AuthException;
import git_kkalnane.backend.starbucks.auth.common.jwt.dto.JwtToken;
import git_kkalnane.backend.starbucks.auth.common.jwt.dto.TokenInfo;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class JwtTokenProviderTest {

    private static final String MEMBER_SECRET_KEY =
            "testMemberSecretKeyForJwtTokenProviderTestWithSufficientLength";
    private static final String MERCHANT_SECRET_KEY =
            "testMerchantSecretKeyForJwtTokenProviderTestWithSufficientLength";
    private static final Long MEMBER_ID = 1L;
    private static final Long MERCHANT_ID = 1L;
    private static final long ACCESS_TOKEN_EXPIRED = 3600000L; // 1시간
    private static final long REFRESH_TOKEN_EXPIRED = 86400000L; // 24시간

    private JwtTokenProvider jwtTokenProvider;
    private SecretKey memberSigningKey;
    private SecretKey merchantSigningKey;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider(MEMBER_SECRET_KEY, MERCHANT_SECRET_KEY);
        memberSigningKey = Keys.hmacShaKeyFor(MEMBER_SECRET_KEY.getBytes());
        merchantSigningKey = Keys.hmacShaKeyFor(MERCHANT_SECRET_KEY.getBytes());

        ReflectionTestUtils.setField(jwtTokenProvider, "ACCESS_TOKEN_EXPIRED", ACCESS_TOKEN_EXPIRED);
        ReflectionTestUtils.setField(jwtTokenProvider, "REFRESH_TOKEN_EXPIRED", REFRESH_TOKEN_EXPIRED);
    }

    @Nested
    @DisplayName("정상 시나리오")
    class SuccessTest {

        @Test
        @DisplayName("정상적인 MEMBER JWT 토큰 생성 시 성공적으로 토큰을 생성한다")
        void createJwtToken_Member_Success() {
            JwtToken result = jwtTokenProvider.createJwtToken("MEMBER", MEMBER_ID);

            assertThat(result).isNotNull();
            assertThat(result.getAccessTokenInfo()).isNotNull();
            assertThat(result.getRefreshTokenInfo()).isNotNull();
            assertThat(result.getAccessTokenInfo().getToken()).isNotNull();
            assertThat(result.getRefreshTokenInfo().getToken()).isNotNull();
        }

        @Test
        @DisplayName("정상적인 MERCHANT JWT 토큰 생성 시 성공적으로 토큰을 생성한다")
        void createJwtToken_Merchant_Success() {
            JwtToken result = jwtTokenProvider.createJwtToken("MERCHANT", MERCHANT_ID);

            assertThat(result).isNotNull();
            assertThat(result.getAccessTokenInfo()).isNotNull();
            assertThat(result.getRefreshTokenInfo()).isNotNull();
            assertThat(result.getAccessTokenInfo().getToken()).isNotNull();
            assertThat(result.getRefreshTokenInfo().getToken()).isNotNull();
        }

        @Test
        @DisplayName("다른 memberId로 토큰을 생성할 수 있다")
        void createJwtToken_DifferentMemberId() {
            Long differentMemberId = 999L;
            JwtToken result = jwtTokenProvider.createJwtToken("MEMBER", differentMemberId);

            assertThat(result).isNotNull();
            assertThat(result.getAccessTokenInfo()).isNotNull();
            assertThat(result.getRefreshTokenInfo()).isNotNull();
        }

        @Test
        @DisplayName("MEMBER 토큰의 만료 시간이 올바르게 설정된다")
        void createJwtToken_Member_ExpirationTime() {
            long beforeCreation = System.currentTimeMillis();
            JwtToken result = jwtTokenProvider.createJwtToken("MEMBER", MEMBER_ID);
            long afterCreation = System.currentTimeMillis();

            String accessToken = result.getAccessTokenInfo().getToken();
            String refreshToken = result.getRefreshTokenInfo().getToken();

            Jws<Claims> accessClaims =
                    Jwts.parser().verifyWith(memberSigningKey).build().parseSignedClaims(accessToken);
            Jws<Claims> refreshClaims =
                    Jwts.parser().verifyWith(memberSigningKey).build().parseSignedClaims(refreshToken);

            Date accessExpiration = accessClaims.getPayload().getExpiration();
            Date refreshExpiration = refreshClaims.getPayload().getExpiration();

            long accessExpirationTime = accessExpiration.getTime();
            assertThat(accessExpirationTime).isBetween(beforeCreation + ACCESS_TOKEN_EXPIRED - 1000,
                    afterCreation + ACCESS_TOKEN_EXPIRED + 1000);

            long refreshExpirationTime = refreshExpiration.getTime();
            assertThat(refreshExpirationTime).isBetween(beforeCreation + REFRESH_TOKEN_EXPIRED - 1000,
                    afterCreation + REFRESH_TOKEN_EXPIRED + 1000);
        }

        @Test
        @DisplayName("MERCHANT 토큰의 만료 시간이 올바르게 설정된다")
        void createJwtToken_Merchant_ExpirationTime() {
            long beforeCreation = System.currentTimeMillis();
            JwtToken result = jwtTokenProvider.createJwtToken("MERCHANT", MERCHANT_ID);
            long afterCreation = System.currentTimeMillis();

            String accessToken = result.getAccessTokenInfo().getToken();
            String refreshToken = result.getRefreshTokenInfo().getToken();

            Jws<Claims> accessClaims = Jwts.parser().verifyWith(merchantSigningKey).build()
                    .parseSignedClaims(accessToken);
            Jws<Claims> refreshClaims = Jwts.parser().verifyWith(merchantSigningKey).build()
                    .parseSignedClaims(refreshToken);

            Date accessExpiration = accessClaims.getPayload().getExpiration();
            Date refreshExpiration = refreshClaims.getPayload().getExpiration();

            long accessExpirationTime = accessExpiration.getTime();
            assertThat(accessExpirationTime).isBetween(beforeCreation + ACCESS_TOKEN_EXPIRED - 1000,
                    afterCreation + ACCESS_TOKEN_EXPIRED + 1000);

            long refreshExpirationTime = refreshExpiration.getTime();
            assertThat(refreshExpirationTime).isBetween(
                    beforeCreation + REFRESH_TOKEN_EXPIRED - 1000,
                    afterCreation + REFRESH_TOKEN_EXPIRED + 1000);
        }

        @Test
        @DisplayName("MEMBER 토큰에 memberId가 올바르게 포함된다")
        void createJwtToken_Member_ContainsId() {
            JwtToken result = jwtTokenProvider.createJwtToken("MEMBER", MEMBER_ID);
            String accessToken = result.getAccessTokenInfo().getToken();
            String refreshToken = result.getRefreshTokenInfo().getToken();

            Jws<Claims> accessClaims =
                    Jwts.parser().verifyWith(memberSigningKey).build().parseSignedClaims(accessToken);
            Jws<Claims> refreshClaims =
                    Jwts.parser().verifyWith(memberSigningKey).build().parseSignedClaims(refreshToken);

            Object accessMemberId = Long.valueOf((Integer) accessClaims.getPayload().get("id"));
            Object refreshMemberId = Long.valueOf((Integer) refreshClaims.getPayload().get("id"));

            assertThat(accessMemberId).isEqualTo(MEMBER_ID);
            assertThat(refreshMemberId).isEqualTo(MEMBER_ID);
        }

        @Test
        @DisplayName("MERCHANT 토큰에 merchantId가 올바르게 포함된다")
        void createJwtToken_Merchant_ContainsId() {
            JwtToken result = jwtTokenProvider.createJwtToken("MERCHANT", MERCHANT_ID);
            String accessToken = result.getAccessTokenInfo().getToken();
            String refreshToken = result.getRefreshTokenInfo().getToken();

            Jws<Claims> accessClaims = Jwts.parser().verifyWith(merchantSigningKey).build()
                    .parseSignedClaims(accessToken);
            Jws<Claims> refreshClaims = Jwts.parser().verifyWith(merchantSigningKey).build()
                    .parseSignedClaims(refreshToken);

            Object accessMerchantId = Long.valueOf((Integer) accessClaims.getPayload().get("id"));
            Object refreshMerchantId = Long.valueOf((Integer) refreshClaims.getPayload().get("id"));

            assertThat(accessMerchantId).isEqualTo(MERCHANT_ID);
            assertThat(refreshMerchantId).isEqualTo(MERCHANT_ID);
        }
    }

    @Nested
    @DisplayName("토큰 검증 시나리오")
    class TokenValidationTest {

        @Test
        @DisplayName("생성된 MEMBER 토큰이 서로 다른지 확인한다")
        void createJwtToken_Member_TokensAreDifferent() {
            long OTHER_MEMBER_ID = 2L;
            JwtToken result1 = jwtTokenProvider.createJwtToken("MEMBER", MEMBER_ID);
            JwtToken result2 = jwtTokenProvider.createJwtToken("MEMBER", OTHER_MEMBER_ID);

            assertThat(result1.getAccessTokenInfo().getToken())
                    .isNotEqualTo(result2.getAccessTokenInfo().getToken());
            assertThat(result1.getRefreshTokenInfo().getToken())
                    .isNotEqualTo(result2.getRefreshTokenInfo().getToken());
        }

        @Test
        @DisplayName("MEMBER와 MERCHANT 토큰이 서로 다른지 확인한다")
        void createJwtToken_MemberAndMerchant_TokensAreDifferent() {
            JwtToken memberToken = jwtTokenProvider.createJwtToken("MEMBER", 1L);
            JwtToken merchantToken = jwtTokenProvider.createJwtToken("MERCHANT", 1L);

            assertThat(memberToken.getAccessTokenInfo().getToken())
                    .isNotEqualTo(merchantToken.getAccessTokenInfo().getToken());
            assertThat(memberToken.getRefreshTokenInfo().getToken())
                    .isNotEqualTo(merchantToken.getRefreshTokenInfo().getToken());
        }

        @Test
        @DisplayName("토큰의 구조가 올바른지 확인한다")
        void createJwtToken_TokenStructure() {
            JwtToken result = jwtTokenProvider.createJwtToken("MEMBER", MEMBER_ID);
            String accessToken = result.getAccessTokenInfo().getToken();
            String refreshToken = result.getRefreshTokenInfo().getToken();

            assertThat(accessToken.split("\\.")).hasSize(3);
            assertThat(refreshToken.split("\\.")).hasSize(3);
        }
    }

    @Nested
    @DisplayName("reissueAccessTokenIfRefreshTokenIsValid 메서드 테스트")
    class ReissueAccessTokenTest {

        @Test
        @DisplayName("유효한 MEMBER 리프레시 토큰으로 액세스 토큰을 재발급한다")
        void reissueAccessToken_Member_ValidRefreshToken() {
            // given
            JwtToken originalToken = jwtTokenProvider.createJwtToken("MEMBER", MEMBER_ID);
            String refreshToken = originalToken.getRefreshTokenInfo().getToken();

            // when
            TokenInfo reissuedAccessToken = jwtTokenProvider
                    .reissueAccessTokenIfRefreshTokenIsValid("MEMBER", refreshToken);

            // then
            assertThat(reissuedAccessToken).isNotNull();
            assertThat(reissuedAccessToken.getToken()).isNotNull();
        }

        @Test
        @DisplayName("유효한 MERCHANT 리프레시 토큰으로 액세스 토큰을 재발급한다")
        void reissueAccessToken_Merchant_ValidRefreshToken() {
            // given
            JwtToken originalToken = jwtTokenProvider.createJwtToken("MERCHANT", MERCHANT_ID);
            String refreshToken = originalToken.getRefreshTokenInfo().getToken();

            // when
            TokenInfo reissuedAccessToken = jwtTokenProvider
                    .reissueAccessTokenIfRefreshTokenIsValid("MERCHANT", refreshToken);

            // then
            assertThat(reissuedAccessToken).isNotNull();
            assertThat(reissuedAccessToken.getToken()).isNotNull();
        }

        @Test
        @DisplayName("만료된 리프레시 토큰으로 재발급 시도 시 예외가 발생한다")
        void reissueAccessToken_ExpiredRefreshToken() {
            // given - 만료된 토큰 생성
            Map<String, Object> claims = new HashMap<>();
            claims.put("id", MEMBER_ID);

            Date issuedAt = new Date(System.currentTimeMillis() - 2000);
            Date expiredAt = new Date(System.currentTimeMillis() - 1000);

            String expiredRefreshToken = Jwts.builder().claims(claims).issuedAt(issuedAt)
                    .expiration(expiredAt).signWith(memberSigningKey).compact();

            // when & then
            assertThatThrownBy(() -> jwtTokenProvider
                    .reissueAccessTokenIfRefreshTokenIsValid("MEMBER", expiredRefreshToken))
                            .isInstanceOf(AuthException.class).satisfies(exception -> {
                                AuthException authException = (AuthException) exception;
                                assertThat(authException.getErrorCode())
                                        .isEqualTo(AuthErrorCode.EXPIRED_TOKEN);
                            });
        }
    }

    @Nested
    @DisplayName("getMemberId 메서드 테스트")
    class GetMemberIdTest {

        @Test
        @DisplayName("유효한 MEMBER Access 토큰에서 memberId를 추출한다")
        void getMemberId_Member_ValidAccessToken() {
            JwtToken jwtToken = jwtTokenProvider.createJwtToken("MEMBER", MEMBER_ID);
            String accessToken = jwtToken.getAccessTokenInfo().getToken();

            String result = jwtTokenProvider.getMemberId("MEMBER", accessToken);

            assertThat(result).isEqualTo(MEMBER_ID.toString());
        }

        @Test
        @DisplayName("유효한 MERCHANT Access 토큰에서 merchantId를 추출한다")
        void getMemberId_Merchant_ValidAccessToken() {
            JwtToken jwtToken = jwtTokenProvider.createJwtToken("MERCHANT", MERCHANT_ID);
            String accessToken = jwtToken.getAccessTokenInfo().getToken();

            String result = jwtTokenProvider.getMemberId("MERCHANT", accessToken);

            assertThat(result).isEqualTo(MERCHANT_ID.toString());
        }

        @Test
        @DisplayName("유효한 MEMBER Refresh 토큰에서 memberId를 추출한다")
        void getMemberId_Member_ValidRefreshToken() {
            JwtToken jwtToken = jwtTokenProvider.createJwtToken("MEMBER", MEMBER_ID);
            String refreshToken = jwtToken.getRefreshTokenInfo().getToken();

            String result = jwtTokenProvider.getMemberId("MEMBER", refreshToken);

            assertThat(result).isEqualTo(MEMBER_ID.toString());
        }

        @Test
        @DisplayName("다른 memberId로 생성된 토큰에서 올바른 memberId를 추출한다")
        void getMemberId_DifferentMemberId() {
            Long differentMemberId = 999L;
            JwtToken jwtToken = jwtTokenProvider.createJwtToken("MEMBER", differentMemberId);
            String accessToken = jwtToken.getAccessTokenInfo().getToken();

            String result = jwtTokenProvider.getMemberId("MEMBER", accessToken);

            assertThat(result).isEqualTo(differentMemberId.toString());
        }

        @Test
        @DisplayName("잘못된 형식의 토큰에 대해 MALFORMED_TOKEN 예외가 발생한다")
        void getMemberId_MalformedToken() {
            String malformedToken = "invalid.token.format";

            assertThatThrownBy(() -> jwtTokenProvider.getMemberId("MEMBER", malformedToken))
                    .isInstanceOf(AuthException.class).satisfies(exception -> {
                        AuthException authException = (AuthException) exception;
                        assertThat(authException.getErrorCode())
                                .isEqualTo(AuthErrorCode.MALFORMED_TOKEN);
                    });
        }

        @Test
        @DisplayName("만료된 토큰에 대해 EXPIRED_TOKEN 예외가 발생한다")
        void getMemberId_ExpiredToken() {
            // 만료된 토큰 생성 (과거 시간으로 설정)
            Map<String, Object> claims = new HashMap<>();
            claims.put("id", MEMBER_ID);

            Date issuedAt = new Date(System.currentTimeMillis() - 2000); // 2초 전
            Date expiredAt = new Date(System.currentTimeMillis() - 1000); // 1초 전 (이미 만료됨)

            String expiredToken = Jwts.builder().claims(claims).issuedAt(issuedAt)
                    .expiration(expiredAt).signWith(memberSigningKey).compact();

            assertThatThrownBy(() -> jwtTokenProvider.getMemberId("MEMBER", expiredToken))
                    .isInstanceOf(AuthException.class).satisfies(exception -> {
                        AuthException authException = (AuthException) exception;
                        assertThat(authException.getErrorCode())
                                .isEqualTo(AuthErrorCode.EXPIRED_TOKEN);
                    });
        }

        @Test
        @DisplayName("null 토큰에 대해 INVALID_TOKEN_PARAMETER 예외가 발생한다")
        void getMemberId_NullToken() {
            assertThatThrownBy(() -> jwtTokenProvider.getMemberId("MEMBER", null))
                    .isInstanceOf(AuthException.class).satisfies(exception -> {
                        AuthException authException = (AuthException) exception;
                        assertThat(authException.getErrorCode())
                                .isEqualTo(AuthErrorCode.INVALID_TOKEN_PARAMETER);
                    });
        }

        @Test
        @DisplayName("빈 문자열 토큰에 대해 INVALID_TOKEN_PARAMETER 예외가 발생한다")
        void getMemberId_EmptyToken() {
            assertThatThrownBy(() -> jwtTokenProvider.getMemberId("MEMBER", ""))
                    .isInstanceOf(AuthException.class).satisfies(exception -> {
                        AuthException authException = (AuthException) exception;
                        assertThat(authException.getErrorCode())
                                .isEqualTo(AuthErrorCode.INVALID_TOKEN_PARAMETER);
                    });
        }

        @Test
        @DisplayName("공백만 있는 토큰에 대해 INVALID_TOKEN_PARAMETER 예외가 발생한다")
        void getMemberId_BlankToken() {
            assertThatThrownBy(() -> jwtTokenProvider.getMemberId("MEMBER", "   "))
                    .isInstanceOf(AuthException.class).satisfies(exception -> {
                        AuthException authException = (AuthException) exception;
                        assertThat(authException.getErrorCode())
                                .isEqualTo(AuthErrorCode.INVALID_TOKEN_PARAMETER);
                    });
        }

        @Test
        @DisplayName("id 클레임이 없는 토큰에서도 예외 없이 처리된다")
        void getMemberId_TokenWithoutId() {
            // id 클레임이 없는 토큰 생성
            Map<String, Object> claims = new HashMap<>();
            // id 클레임을 추가하지 않음

            Date issuedAt = new Date(System.currentTimeMillis());
            Date expiredAt = new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRED);

            String tokenWithoutId = Jwts.builder().claims(claims).issuedAt(issuedAt)
                    .expiration(expiredAt).signWith(memberSigningKey).compact();

            String result = jwtTokenProvider.getMemberId("MEMBER", tokenWithoutId);

            // id가 없으면 null이 반환되므로 "null" 문자열이 됨
            assertThat(result).isEqualTo("null");
        }

        @Test
        @DisplayName("잘못된 시크릿 키로 검증 시 예외가 발생한다")
        void getMemberId_WrongSubject() {
            // MEMBER로 생성한 토큰을 MERCHANT로 검증
            JwtToken memberToken = jwtTokenProvider.createJwtToken("MEMBER", MEMBER_ID);
            String accessToken = memberToken.getAccessTokenInfo().getToken();

            // 다른 키로 검증하면 서명 검증 실패로 예외 발생
            assertThatThrownBy(() -> jwtTokenProvider.getMemberId("MERCHANT", accessToken))
                    .isInstanceOf(Exception.class); // AuthException 또는 다른 JWT 관련 예외
        }
    }
}

