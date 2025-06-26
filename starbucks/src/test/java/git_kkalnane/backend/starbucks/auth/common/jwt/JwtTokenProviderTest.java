package git_kkalnane.backend.starbucks.auth.common.jwt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import git_kkalnane.backend.starbucks.auth.common.exception.AuthException;
import git_kkalnane.backend.starbucks.auth.common.exception.AuthErrorCode;
import git_kkalnane.backend.starbucks.auth.common.jwt.dto.JwtToken;
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

  private static final String SECRET_KEY =
      "testSecretKeyForJwtTokenProviderTestWithSufficientLength";
  private static final Long MEMBER_ID = 1L;
  private static final long ACCESS_TOKEN_EXPIRED = 3600000L; // 1시간
  private static final long REFRESH_TOKEN_EXPIRED = 86400000L; // 24시간

  private JwtTokenProvider jwtTokenProvider;
  private SecretKey signingKey;

  @BeforeEach
  void setUp() {
    jwtTokenProvider = new JwtTokenProvider(SECRET_KEY);
    signingKey = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());

    ReflectionTestUtils.setField(jwtTokenProvider, "ACCESS_TOKEN_EXPIRED", ACCESS_TOKEN_EXPIRED);
    ReflectionTestUtils.setField(jwtTokenProvider, "REFRESH_TOKEN_EXPIRED", REFRESH_TOKEN_EXPIRED);
  }

  @Nested
  @DisplayName("정상 시나리오")
  class SuccessTest {

    @Test
    @DisplayName("정상적인 JWT 토큰 생성 시 성공적으로 토큰을 생성한다")
    void createJwtToken_Success() {
      JwtToken result = jwtTokenProvider.createJwtToken(MEMBER_ID);

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
      JwtToken result = jwtTokenProvider.createJwtToken(differentMemberId);

      assertThat(result).isNotNull();
      assertThat(result.getAccessTokenInfo()).isNotNull();
      assertThat(result.getRefreshTokenInfo()).isNotNull();
    }

    @Test
    @DisplayName("토큰의 만료 시간이 올바르게 설정된다")
    void createJwtToken_ExpirationTime() {
      long beforeCreation = System.currentTimeMillis();
      JwtToken result = jwtTokenProvider.createJwtToken(MEMBER_ID);
      long afterCreation = System.currentTimeMillis();

      String accessToken = result.getAccessTokenInfo().getToken();
      String refreshToken = result.getRefreshTokenInfo().getToken();

      Jws<Claims> accessClaims =
          Jwts.parser().verifyWith(signingKey).build().parseSignedClaims(accessToken);
      Jws<Claims> refreshClaims =
          Jwts.parser().verifyWith(signingKey).build().parseSignedClaims(refreshToken);

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
    @DisplayName("토큰에 memberId가 올바르게 포함된다")
    void createJwtToken_ContainsMemberId() {
      JwtToken result = jwtTokenProvider.createJwtToken(MEMBER_ID);
      String accessToken = result.getAccessTokenInfo().getToken();
      String refreshToken = result.getRefreshTokenInfo().getToken();

      Jws<Claims> accessClaims =
          Jwts.parser().verifyWith(signingKey).build().parseSignedClaims(accessToken);
      Jws<Claims> refreshClaims =
          Jwts.parser().verifyWith(signingKey).build().parseSignedClaims(refreshToken);

      Object accessMemberId = Long.valueOf((Integer) accessClaims.getPayload().get("memberId"));
      Object refreshMemberId = Long.valueOf((Integer) refreshClaims.getPayload().get("memberId"));

      assertThat(accessMemberId).isEqualTo(MEMBER_ID);
      assertThat(refreshMemberId).isEqualTo(MEMBER_ID);
    }
  }

  @Nested
  @DisplayName("토큰 검증 시나리오")
  class TokenValidationTest {

    @Test
    @DisplayName("생성된 토큰이 서로 다른지 확인한다")
    void createJwtToken_TokensAreDifferent() {
      long OTHER_MEMBER_ID = 2L;
      JwtToken result1 = jwtTokenProvider.createJwtToken(MEMBER_ID);
      JwtToken result2 = jwtTokenProvider.createJwtToken(OTHER_MEMBER_ID);

      assertThat(result1.getAccessTokenInfo().getToken())
          .isNotEqualTo(result2.getAccessTokenInfo().getToken());
      assertThat(result1.getRefreshTokenInfo().getToken())
          .isNotEqualTo(result2.getRefreshTokenInfo().getToken());
    }

    @Test
    @DisplayName("토큰의 구조가 올바른지 확인한다")
    void createJwtToken_TokenStructure() {
      JwtToken result = jwtTokenProvider.createJwtToken(MEMBER_ID);
      String accessToken = result.getAccessTokenInfo().getToken();
      String refreshToken = result.getRefreshTokenInfo().getToken();

      assertThat(accessToken.split("\\.")).hasSize(3);
      assertThat(refreshToken.split("\\.")).hasSize(3);
    }
  }

  @Nested
  @DisplayName("getMemberId 메서드 테스트")
  class GetMemberIdTest {

    @Test
    @DisplayName("유효한 Access 토큰에서 memberId를 추출한다")
    void getMemberId_ValidAccessToken() {
      JwtToken jwtToken = jwtTokenProvider.createJwtToken(MEMBER_ID);
      String accessToken = jwtToken.getAccessTokenInfo().getToken();

      String result = jwtTokenProvider.getMemberId(accessToken);

      assertThat(result).isEqualTo(MEMBER_ID.toString());
    }

    @Test
    @DisplayName("유효한 Refresh 토큰에서 memberId를 추출한다")
    void getMemberId_ValidRefreshToken() {
      JwtToken jwtToken = jwtTokenProvider.createJwtToken(MEMBER_ID);
      String refreshToken = jwtToken.getRefreshTokenInfo().getToken();

      String result = jwtTokenProvider.getMemberId(refreshToken);

      assertThat(result).isEqualTo(MEMBER_ID.toString());
    }

    @Test
    @DisplayName("다른 memberId로 생성된 토큰에서 올바른 memberId를 추출한다")
    void getMemberId_DifferentMemberId() {
      Long differentMemberId = 999L;
      JwtToken jwtToken = jwtTokenProvider.createJwtToken(differentMemberId);
      String accessToken = jwtToken.getAccessTokenInfo().getToken();

      String result = jwtTokenProvider.getMemberId(accessToken);

      assertThat(result).isEqualTo(differentMemberId.toString());
    }

    @Test
    @DisplayName("잘못된 형식의 토큰에 대해 MALFORMED_TOKEN 예외가 발생한다")
    void getMemberId_MalformedToken() {
      String malformedToken = "invalid.token.format";

      assertThatThrownBy(() -> jwtTokenProvider.getMemberId(malformedToken))
          .isInstanceOf(AuthException.class).satisfies(exception -> {
            AuthException authException = (AuthException) exception;
            assertThat(authException.getErrorCode()).isEqualTo(AuthErrorCode.MALFORMED_TOKEN);
          });
    }

    @Test
    @DisplayName("만료된 토큰에 대해 EXPIRED_TOKEN 예외가 발생한다")
    void getMemberId_ExpiredToken() {
      // 만료된 토큰 생성 (과거 시간으로 설정)
      Map<String, Object> claims = new HashMap<>();
      claims.put("memberId", MEMBER_ID);

      Date issuedAt = new Date(System.currentTimeMillis() - 2000); // 2초 전
      Date expiredAt = new Date(System.currentTimeMillis() - 1000); // 1초 전 (이미 만료됨)

      String expiredToken = Jwts.builder().claims(claims).issuedAt(issuedAt).expiration(expiredAt)
          .signWith(signingKey).compact();

      assertThatThrownBy(() -> jwtTokenProvider.getMemberId(expiredToken))
          .isInstanceOf(AuthException.class).satisfies(exception -> {
            AuthException authException = (AuthException) exception;
            assertThat(authException.getErrorCode()).isEqualTo(AuthErrorCode.EXPIRED_TOKEN);
          });
    }

    @Test
    @DisplayName("null 토큰에 대해 INVALID_TOKEN_PARAMETER 예외가 발생한다")
    void getMemberId_NullToken() {
      assertThatThrownBy(() -> jwtTokenProvider.getMemberId(null)).isInstanceOf(AuthException.class)
          .satisfies(exception -> {
            AuthException authException = (AuthException) exception;
            assertThat(authException.getErrorCode())
                .isEqualTo(AuthErrorCode.INVALID_TOKEN_PARAMETER);
          });
    }

    @Test
    @DisplayName("빈 문자열 토큰에 대해 INVALID_TOKEN_PARAMETER 예외가 발생한다")
    void getMemberId_EmptyToken() {
      assertThatThrownBy(() -> jwtTokenProvider.getMemberId("")).isInstanceOf(AuthException.class)
          .satisfies(exception -> {
            AuthException authException = (AuthException) exception;
            assertThat(authException.getErrorCode())
                .isEqualTo(AuthErrorCode.INVALID_TOKEN_PARAMETER);
          });
    }

    @Test
    @DisplayName("공백만 있는 토큰에 대해 INVALID_TOKEN_PARAMETER 예외가 발생한다")
    void getMemberId_BlankToken() {
      assertThatThrownBy(() -> jwtTokenProvider.getMemberId("   "))
          .isInstanceOf(AuthException.class).satisfies(exception -> {
            AuthException authException = (AuthException) exception;
            assertThat(authException.getErrorCode())
                .isEqualTo(AuthErrorCode.INVALID_TOKEN_PARAMETER);
          });
    }

    @Test
    @DisplayName("memberId가 없는 토큰에서도 예외 없이 처리된다")
    void getMemberId_TokenWithoutMemberId() {
      // memberId 클레임이 없는 토큰 생성
      Map<String, Object> claims = new HashMap<>();
      // memberId 클레임을 추가하지 않음

      Date issuedAt = new Date(System.currentTimeMillis());
      Date expiredAt = new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRED);

      String tokenWithoutMemberId = Jwts.builder().claims(claims).issuedAt(issuedAt)
          .expiration(expiredAt).signWith(signingKey).compact();

      String result = jwtTokenProvider.getMemberId(tokenWithoutMemberId);

      // memberId가 없으면 null이 반환되므로 "null" 문자열이 됨
      assertThat(result).isEqualTo("null");
    }
  }
}

