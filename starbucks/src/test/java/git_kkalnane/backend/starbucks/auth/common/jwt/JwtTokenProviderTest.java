package git_kkalnane.backend.starbucks.auth.common.jwt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import git_kkalnane.backend.starbucks.auth.domain.AccessToken;
import git_kkalnane.backend.starbucks.auth.domain.RefreshToken;
import git_kkalnane.backend.starbucks.auth.repository.AccessTokenRepository;
import git_kkalnane.backend.starbucks.auth.repository.RefreshTokenRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import java.util.Date;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class JwtTokenProviderTest {

    private static final String SECRET_KEY =
            "testSecretKeyForJwtTokenProviderTestWithSufficientLength";
    private static final Long MEMBER_ID = 1L;
    private static final long ACCESS_TOKEN_EXPIRED = 3600000L; // 1시간
    private static final long REFRESH_TOKEN_EXPIRED = 86400000L; // 24시간
    @Mock
    private AccessTokenRepository accessTokenRepository;
    @Mock
    private RefreshTokenRepository refreshTokenRepository;
    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        // JwtTokenProvider 수동 생성
        jwtTokenProvider =
                new JwtTokenProvider(SECRET_KEY, accessTokenRepository, refreshTokenRepository);

        // private 필드 설정
        ReflectionTestUtils.setField(jwtTokenProvider, "ACCESS_TOKEN_EXPIRED", ACCESS_TOKEN_EXPIRED);
        ReflectionTestUtils.setField(jwtTokenProvider, "REFRESH_TOKEN_EXPIRED", REFRESH_TOKEN_EXPIRED);
    }

    @Nested
    @DisplayName("정상 시나리오")
    class SuccessTest {

        @Test
        @DisplayName("정상적인 JWT 토큰 생성 시 성공적으로 토큰을 생성한다")
        void createJwtToken_Success() {
            // given
            when(accessTokenRepository.save(any(AccessToken.class))).thenReturn(
                    AccessToken.builder().memberId(MEMBER_ID).token("test").expiration(new Date()).build());
            when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(
                    RefreshToken.builder().memberId(MEMBER_ID).token("test").expiration(new Date()).build());

            // when
            JwtToken result = jwtTokenProvider.createJwtToken(MEMBER_ID);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getTokenType()).isEqualTo("Bearer");
            assertThat(result.getAccessToken()).isNotNull();
            assertThat(result.getRefreshToken()).isNotNull();
            assertThat(result.getAccessToken()).startsWith("Bearer ");
            assertThat(result.getRefreshToken()).doesNotStartWith("Bearer ");

            // verify
            verify(accessTokenRepository, times(1)).save(any(AccessToken.class));
            verify(accessTokenRepository, times(1)).removeAccessTokenByMemberId(MEMBER_ID);
            verify(refreshTokenRepository, times(1)).save(any(RefreshToken.class));
            verify(refreshTokenRepository, times(1)).removeRefreshTokenByMemberId(MEMBER_ID);
        }

        @Test
        @DisplayName("다른 memberId로 토큰을 생성할 수 있다")
        void createJwtToken_DifferentMemberId() {
            // given
            Long differentMemberId = 999L;
            when(accessTokenRepository.save(any(AccessToken.class))).thenReturn(AccessToken.builder()
                    .memberId(differentMemberId).token("test").expiration(new Date()).build());
            when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(RefreshToken.builder()
                    .memberId(differentMemberId).token("test").expiration(new Date()).build());

            // when
            JwtToken result = jwtTokenProvider.createJwtToken(differentMemberId);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getAccessToken()).isNotNull();
            assertThat(result.getRefreshToken()).isNotNull();

            verify(accessTokenRepository, times(1)).removeAccessTokenByMemberId(differentMemberId);
            verify(refreshTokenRepository, times(1)).removeRefreshTokenByMemberId(differentMemberId);
        }

        @Test
        @DisplayName("토큰의 만료 시간이 올바르게 설정된다")
        void createJwtToken_ExpirationTime() {
            // given
            when(accessTokenRepository.save(any(AccessToken.class))).thenReturn(
                    AccessToken.builder().memberId(MEMBER_ID).token("test").expiration(new Date()).build());
            when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(
                    RefreshToken.builder().memberId(MEMBER_ID).token("test").expiration(new Date()).build());

            long beforeCreation = System.currentTimeMillis();

            // when
            JwtToken result = jwtTokenProvider.createJwtToken(MEMBER_ID);

            long afterCreation = System.currentTimeMillis();

            // then
            String accessToken = result.getAccessToken().substring(7);
            String refreshToken = result.getRefreshToken();

            // SecretKey 가져오기
            SecretKey signingKey =
                    (SecretKey) ReflectionTestUtils.getField(jwtTokenProvider, "signingKey");

            // 토큰 파싱하여 만료 시간 확인
            Jws<Claims> accessClaims =
                    Jwts.parser().verifyWith(signingKey).build().parseSignedClaims(accessToken);

            Jws<Claims> refreshClaims =
                    Jwts.parser().verifyWith(signingKey).build().parseSignedClaims(refreshToken);

            Date accessExpiration = accessClaims.getPayload().getExpiration();
            Date refreshExpiration = refreshClaims.getPayload().getExpiration();

            // Access Token 만료 시간 검증 (1시간)
            long accessExpirationTime = accessExpiration.getTime();
            assertThat(accessExpirationTime).isBetween(beforeCreation + ACCESS_TOKEN_EXPIRED - 1000,
                    afterCreation + ACCESS_TOKEN_EXPIRED + 1000);

            // Refresh Token 만료 시간 검증 (24시간)
            long refreshExpirationTime = refreshExpiration.getTime();
            assertThat(refreshExpirationTime).isBetween(beforeCreation + REFRESH_TOKEN_EXPIRED - 1000,
                    afterCreation + REFRESH_TOKEN_EXPIRED + 1000);
        }

        @Test
        @DisplayName("토큰에 memberId가 올바르게 포함된다")
        void createJwtToken_ContainsMemberId() {
            // given
            when(accessTokenRepository.save(any(AccessToken.class))).thenReturn(
                    AccessToken.builder().memberId(MEMBER_ID).token("test").expiration(new Date()).build());
            when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(
                    RefreshToken.builder().memberId(MEMBER_ID).token("test").expiration(new Date()).build());

            // when
            JwtToken result = jwtTokenProvider.createJwtToken(MEMBER_ID);

            // then
            String accessToken = result.getAccessToken().substring(7);
            String refreshToken = result.getRefreshToken();

            // SecretKey 가져오기
            SecretKey signingKey =
                    (SecretKey) ReflectionTestUtils.getField(jwtTokenProvider, "signingKey");

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
    @DisplayName("예외 시나리오")
    class ExceptionTest {

        @Test
        @DisplayName("AccessTokenRepository 저장 실패 시 예외가 발생한다")
        void createJwtToken_AccessTokenRepositoryFailure() {
            // given
            when(accessTokenRepository.save(any(AccessToken.class)))
                    .thenThrow(new RuntimeException("Database error"));

            // when & then
            assertThatThrownBy(() -> jwtTokenProvider.createJwtToken(MEMBER_ID))
                    .isInstanceOf(RuntimeException.class).hasMessage("Database error");

            verify(accessTokenRepository, times(1)).removeAccessTokenByMemberId(MEMBER_ID);
            verify(accessTokenRepository, times(1)).save(any(AccessToken.class));
        }

        @Test
        @DisplayName("RefreshTokenRepository 저장 실패 시 예외가 발생한다")
        void createJwtToken_RefreshTokenRepositoryFailure() {
            // given
            when(accessTokenRepository.save(any(AccessToken.class))).thenReturn(
                    AccessToken.builder().memberId(MEMBER_ID).token("test").expiration(new Date()).build());
            when(refreshTokenRepository.save(any(RefreshToken.class)))
                    .thenThrow(new RuntimeException("Database error"));

            // when & then
            assertThatThrownBy(() -> jwtTokenProvider.createJwtToken(MEMBER_ID))
                    .isInstanceOf(RuntimeException.class).hasMessage("Database error");

            verify(accessTokenRepository, times(1)).removeAccessTokenByMemberId(MEMBER_ID);
            verify(accessTokenRepository, times(1)).save(any(AccessToken.class));
            verify(refreshTokenRepository, times(1)).removeRefreshTokenByMemberId(MEMBER_ID);
            verify(refreshTokenRepository, times(1)).save(any(RefreshToken.class));
        }
    }

    @Nested
    @DisplayName("토큰 검증 시나리오")
    class TokenValidationTest {

        long OTHER_MEMBER_ID = 2L;

        @Test
        @DisplayName("생성된 토큰이 서로 다른지 확인한다")
        void createJwtToken_TokensAreDifferent() {
            // given
            when(accessTokenRepository.save(any(AccessToken.class))).thenReturn(
                    AccessToken.builder().memberId(MEMBER_ID).token("test").expiration(new Date()).build());
            when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(
                    RefreshToken.builder().memberId(OTHER_MEMBER_ID).token("test2").expiration(new Date()).build());

            // when
            JwtToken result1 = jwtTokenProvider.createJwtToken(MEMBER_ID);
            JwtToken result2 = jwtTokenProvider.createJwtToken(OTHER_MEMBER_ID);

            // then
            assertThat(result1.getAccessToken()).isNotEqualTo(result2.getAccessToken());
            assertThat(result1.getRefreshToken()).isNotEqualTo(result2.getRefreshToken());
        }

        @Test
        @DisplayName("토큰의 구조가 올바른지 확인한다")
        void createJwtToken_TokenStructure() {
            // given
            when(accessTokenRepository.save(any(AccessToken.class))).thenReturn(
                    AccessToken.builder().memberId(MEMBER_ID).token("test").expiration(new Date()).build());
            when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(
                    RefreshToken.builder().memberId(MEMBER_ID).token("test").expiration(new Date()).build());

            // when
            JwtToken result = jwtTokenProvider.createJwtToken(MEMBER_ID);

            // then
            String accessToken = result.getAccessToken();
            String refreshToken = result.getRefreshToken();

            // Access Token은 "Bearer "로 시작해야 함
            assertThat(accessToken).startsWith("Bearer ");
            assertThat(accessToken.split("\\.")).hasSize(3); // JWT는 3개의 부분으로 구성

            // Refresh Token은 "Bearer "로 시작하지 않아야 함
            assertThat(refreshToken).doesNotStartWith("Bearer ");
            assertThat(refreshToken.split("\\.")).hasSize(3); // JWT는 3개의 부분으로 구성
        }
    }
}
