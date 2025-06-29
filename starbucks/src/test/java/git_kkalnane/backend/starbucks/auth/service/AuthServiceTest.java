package git_kkalnane.backend.starbucks.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import git_kkalnane.backend.starbucks._global.utils.Encryptor;
import git_kkalnane.backend.starbucks.auth.common.exception.AuthErrorCode;
import git_kkalnane.backend.starbucks.auth.common.exception.AuthException;
import git_kkalnane.backend.starbucks.auth.common.jwt.JwtTokenProvider;
import git_kkalnane.backend.starbucks.auth.common.jwt.dto.JwtToken;
import git_kkalnane.backend.starbucks.auth.common.jwt.dto.TokenInfo;
import git_kkalnane.backend.starbucks.auth.domain.RefreshToken;
import git_kkalnane.backend.starbucks.auth.dto.LoginDto;
import git_kkalnane.backend.starbucks.auth.dto.request.LoginRequest;
import git_kkalnane.backend.starbucks.auth.repository.RefreshTokenRepository;
import git_kkalnane.backend.starbucks.member.domain.Member;
import git_kkalnane.backend.starbucks.member.repository.MemberRepository;
import java.util.Date;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    private Encryptor encryptor;
    private AuthService authService;

    private LoginRequest validLoginRequest;
    private Member existingMember;
    private JwtToken jwtToken;
    private TokenInfo accessTokenInfo;
    private TokenInfo refreshTokenInfo;

    @BeforeEach
    void setUp() {
        encryptor = new Encryptor();
        authService = new AuthService(memberRepository, refreshTokenRepository, jwtTokenProvider,
                encryptor);

        String rawPassword = "password123";
        String hashedPassword = encryptor.encrypt(rawPassword);

        validLoginRequest = new LoginRequest("test@example.com", rawPassword);

        existingMember = Member.builder().name("홍길동").nickname("길동이").email("test@example.com")
                .password(hashedPassword).build();

        accessTokenInfo = TokenInfo.builder().token("access-token-123")
                .expiration(new Date(System.currentTimeMillis() + 3600000)).build();

        refreshTokenInfo = TokenInfo.builder().token("refresh-token-456")
                .expiration(new Date(System.currentTimeMillis() + 86400000)).build();

        jwtToken = JwtToken.of(accessTokenInfo, refreshTokenInfo);
    }

    @Nested
    @DisplayName("로그인 테스트")
    class LoginTest {

        @Nested
        @DisplayName("성공 시나리오")
        class SuccessTest {

            @Test
            @DisplayName("정상적인 로그인 요청 시 성공적으로 로그인한다")
            void login_Success() {
                // given
                when(memberRepository.findMemberByEmail(validLoginRequest.email()))
                        .thenReturn(Optional.of(existingMember));
                when(jwtTokenProvider.createJwtToken(existingMember.getId())).thenReturn(jwtToken);
                when(refreshTokenRepository.findByMemberId(existingMember.getId()))
                        .thenReturn(Optional.empty());
                when(refreshTokenRepository.save(any(RefreshToken.class)))
                        .thenReturn(RefreshToken.builder().memberId(existingMember.getId())
                                .token(refreshTokenInfo.getToken())
                                .expiration(refreshTokenInfo.getExpiration()).build());

                // when
                LoginDto result = authService.login(validLoginRequest);

                // then
                assertThat(result).isNotNull();
                assertThat(result.accessToken()).isEqualTo(accessTokenInfo.getToken());
                assertThat(result.refreshToken()).isEqualTo(refreshTokenInfo.getToken());
                assertThat(result.name()).isEqualTo(existingMember.getName());
                assertThat(result.nickname()).isEqualTo(existingMember.getNickname());
                assertThat(result.email()).isEqualTo(existingMember.getEmail());

                // verify
                verify(memberRepository, times(1)).findMemberByEmail(validLoginRequest.email());
                verify(jwtTokenProvider, times(1)).createJwtToken(existingMember.getId());
                verify(refreshTokenRepository, times(1)).findByMemberId(existingMember.getId());
                verify(refreshTokenRepository, times(1)).save(any(RefreshToken.class));
            }

            @Test
            @DisplayName("기존 RefreshToken이 있을 때 업데이트한다")
            void login_UpdateExistingRefreshToken() {
                // given
                RefreshToken existingRefreshToken = RefreshToken.builder()
                        .memberId(existingMember.getId()).token("old-refresh-token")
                        .expiration(new Date(System.currentTimeMillis() - 1000)).build();

                when(memberRepository.findMemberByEmail(validLoginRequest.email()))
                        .thenReturn(Optional.of(existingMember));
                when(jwtTokenProvider.createJwtToken(existingMember.getId())).thenReturn(jwtToken);
                when(refreshTokenRepository.findByMemberId(existingMember.getId()))
                        .thenReturn(Optional.of(existingRefreshToken));

                // when
                LoginDto result = authService.login(validLoginRequest);

                // then
                assertThat(result).isNotNull();
                assertThat(result.accessToken()).isEqualTo(accessTokenInfo.getToken());
                assertThat(result.refreshToken()).isEqualTo(refreshTokenInfo.getToken());

                // 기존 토큰이 업데이트되었는지 확인
                assertThat(existingRefreshToken.getToken()).isEqualTo(refreshTokenInfo.getToken());
                assertThat(existingRefreshToken.getExpiration())
                        .isEqualTo(refreshTokenInfo.getExpiration());

                // verify
                verify(memberRepository, times(1)).findMemberByEmail(validLoginRequest.email());
                verify(jwtTokenProvider, times(1)).createJwtToken(existingMember.getId());
                verify(refreshTokenRepository, times(1)).findByMemberId(existingMember.getId());
                verify(refreshTokenRepository, times(0)).save(any(RefreshToken.class));
            }

            @Test
            @DisplayName("다른 회원으로 로그인 시 올바른 정보가 반환된다")
            void login_DifferentMember() {
                // given
                Member anotherMember =
                        Member.builder().name("김철수").nickname("철수이").email("chulsoo@example.com")
                                .password(encryptor.encrypt("chulsoo123")).build();

                LoginRequest anotherRequest = new LoginRequest("chulsoo@example.com", "chulsoo123");

                TokenInfo anotherAccessTokenInfo = TokenInfo.builder().token("another-access-token")
                        .expiration(new Date(System.currentTimeMillis() + 3600000)).build();
                TokenInfo anotherRefreshTokenInfo = TokenInfo.builder()
                        .token("another-refresh-token")
                        .expiration(new Date(System.currentTimeMillis() + 86400000)).build();
                JwtToken anotherJwtToken =
                        JwtToken.of(anotherAccessTokenInfo, anotherRefreshTokenInfo);

                when(memberRepository.findMemberByEmail(anotherRequest.email()))
                        .thenReturn(Optional.of(anotherMember));
                when(jwtTokenProvider.createJwtToken(anotherMember.getId()))
                        .thenReturn(anotherJwtToken);
                when(refreshTokenRepository.findByMemberId(anotherMember.getId()))
                        .thenReturn(Optional.empty());
                when(refreshTokenRepository.save(any(RefreshToken.class)))
                        .thenReturn(RefreshToken.builder().memberId(anotherMember.getId())
                                .token(anotherRefreshTokenInfo.getToken())
                                .expiration(anotherRefreshTokenInfo.getExpiration()).build());

                // when
                LoginDto result = authService.login(anotherRequest);

                // then
                assertThat(result).isNotNull();
                assertThat(result.accessToken()).isEqualTo(anotherAccessTokenInfo.getToken());
                assertThat(result.refreshToken()).isEqualTo(anotherRefreshTokenInfo.getToken());
                assertThat(result.name()).isEqualTo(anotherMember.getName());
                assertThat(result.nickname()).isEqualTo(anotherMember.getNickname());
                assertThat(result.email()).isEqualTo(anotherMember.getEmail());
            }
        }

        @Nested
        @DisplayName("예외 시나리오")
        class ExceptionTest {

            @Test
            @DisplayName("존재하지 않는 이메일로 로그인 시 EMAIL_INVALID_EXCEPTION이 발생한다")
            void login_EmailNotFound() {
                // given
                LoginRequest invalidEmailRequest =
                        new LoginRequest("nonexistent@example.com", "password123");
                when(memberRepository.findMemberByEmail(invalidEmailRequest.email()))
                        .thenReturn(Optional.empty());

                // when & then
                assertThatThrownBy(() -> authService.login(invalidEmailRequest))
                        .isInstanceOf(AuthException.class).satisfies(exception -> {
                            AuthException authException = (AuthException) exception;
                            assertThat(authException.getErrorCode())
                                    .isEqualTo(AuthErrorCode.EMAIL_INVALID_EXCEPTION);
                        });

                // verify
                verify(memberRepository, times(1)).findMemberByEmail(invalidEmailRequest.email());
                verify(jwtTokenProvider, times(0)).createJwtToken(any());
            }

            @Test
            @DisplayName("잘못된 비밀번호로 로그인 시 PASSWORD_INVALID_EXCEPTION이 발생한다")
            void login_WrongPassword() {
                // given
                LoginRequest wrongPasswordRequest =
                        new LoginRequest("test@example.com", "wrongpassword");
                when(memberRepository.findMemberByEmail(wrongPasswordRequest.email()))
                        .thenReturn(Optional.of(existingMember));

                // when & then
                assertThatThrownBy(() -> authService.login(wrongPasswordRequest))
                        .isInstanceOf(AuthException.class).satisfies(exception -> {
                            AuthException authException = (AuthException) exception;
                            assertThat(authException.getErrorCode())
                                    .isEqualTo(AuthErrorCode.PASSWORD_INVALID_EXCEPTION);
                        });

                // verify
                verify(memberRepository, times(1)).findMemberByEmail(wrongPasswordRequest.email());
                verify(jwtTokenProvider, times(0)).createJwtToken(any());
            }

            @Test
            @DisplayName("null 이메일로 로그인 시 EMAIL_INVALID_EXCEPTION이 발생한다")
            void login_NullEmail() {
                // given
                LoginRequest nullEmailRequest = new LoginRequest(null, "password123");
                when(memberRepository.findMemberByEmail(null)).thenReturn(Optional.empty());

                // when & then
                assertThatThrownBy(() -> authService.login(nullEmailRequest))
                        .isInstanceOf(AuthException.class).satisfies(exception -> {
                            AuthException authException = (AuthException) exception;
                            assertThat(authException.getErrorCode())
                                    .isEqualTo(AuthErrorCode.EMAIL_INVALID_EXCEPTION);
                        });
            }

            @Test
            @DisplayName("null 비밀번호로 로그인 시 PASSWORD_INVALID_EXCEPTION이 발생한다")
            void login_NullPassword() {
                // given
                LoginRequest nullPasswordRequest = new LoginRequest("test@example.com", null);
                when(memberRepository.findMemberByEmail(nullPasswordRequest.email()))
                        .thenReturn(Optional.of(existingMember));

                // when & then
                assertThatThrownBy(() -> authService.login(nullPasswordRequest))
                        .isInstanceOf(AuthException.class).satisfies(exception -> {
                            AuthException authException = (AuthException) exception;
                            assertThat(authException.getErrorCode())
                                    .isEqualTo(AuthErrorCode.PASSWORD_INVALID_EXCEPTION);
                        });
            }
        }
    }

    @Nested
    @DisplayName("RefreshToken 저장 테스트")
    class SaveRefreshTokenTest {

        @Nested
        @DisplayName("성공 시나리오")
        class SuccessTest {

            @Test
            @DisplayName("새로운 RefreshToken을 저장할 수 있다")
            void saveRefreshToken_NewToken() {
                // given
                Long memberId = 1L;
                TokenInfo tokenInfo = TokenInfo.builder().token("new-refresh-token")
                        .expiration(new Date(System.currentTimeMillis() + 86400000)).build();

                when(refreshTokenRepository.findByMemberId(memberId)).thenReturn(Optional.empty());
                when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(
                        RefreshToken.builder().memberId(memberId).token(tokenInfo.getToken())
                                .expiration(tokenInfo.getExpiration()).build());

                // when
                authService.saveRefreshTokenToRepository(memberId, tokenInfo);

                // then
                verify(refreshTokenRepository, times(1)).findByMemberId(memberId);
                verify(refreshTokenRepository, times(1)).save(any(RefreshToken.class));
            }

            @Test
            @DisplayName("기존 RefreshToken을 업데이트할 수 있다")
            void saveRefreshToken_UpdateExistingToken() {
                // given
                Long memberId = 1L;
                TokenInfo newTokenInfo = TokenInfo.builder().token("updated-refresh-token")
                        .expiration(new Date(System.currentTimeMillis() + 86400000)).build();

                RefreshToken existingToken =
                        RefreshToken.builder().memberId(memberId).token("old-refresh-token")
                                .expiration(new Date(System.currentTimeMillis() - 1000)).build();

                when(refreshTokenRepository.findByMemberId(memberId))
                        .thenReturn(Optional.of(existingToken));

                // when
                authService.saveRefreshTokenToRepository(memberId, newTokenInfo);

                // then
                verify(refreshTokenRepository, times(1)).findByMemberId(memberId);
                verify(refreshTokenRepository, times(0)).save(any(RefreshToken.class));

                // 기존 토큰이 업데이트되었는지 확인
                assertThat(existingToken.getToken()).isEqualTo(newTokenInfo.getToken());
                assertThat(existingToken.getExpiration()).isEqualTo(newTokenInfo.getExpiration());
            }
        }
    }

    @Nested
    @DisplayName("로그아웃 테스트")
    class LogoutTest {

        @Nested
        @DisplayName("성공 시나리오")
        class SuccessTest {

            @Test
            @DisplayName("정상적인 로그아웃 시 토큰이 초기화된다")
            void logout_Success() {
                // given
                Long memberId = 1L;
                RefreshToken refreshToken = RefreshToken.builder().memberId(memberId)
                        .token("valid-refresh-token")
                        .expiration(new Date(System.currentTimeMillis() + 86400000)).build();

                when(refreshTokenRepository.findByMemberId(memberId))
                        .thenReturn(Optional.of(refreshToken));

                // when
                authService.logout(memberId);

                // then
                assertThat(refreshToken.getToken()).isEqualTo("");

                // 만료 시간이 현재 시간으로 설정되었는지 확인 (1초 이내)
                long currentTime = System.currentTimeMillis();
                assertThat(refreshToken.getExpiration().getTime()).isBetween(currentTime - 1000,
                        currentTime + 1000);

                // verify
                verify(refreshTokenRepository, times(1)).findByMemberId(memberId);
            }

            @Test
            @DisplayName("다른 memberId로 로그아웃할 수 있다")
            void logout_DifferentMemberId() {
                // given
                Long differentMemberId = 999L;
                RefreshToken refreshToken = RefreshToken.builder().memberId(differentMemberId)
                        .token("different-refresh-token")
                        .expiration(new Date(System.currentTimeMillis() + 86400000)).build();

                when(refreshTokenRepository.findByMemberId(differentMemberId))
                        .thenReturn(Optional.of(refreshToken));

                // when
                authService.logout(differentMemberId);

                // then
                assertThat(refreshToken.getToken()).isEqualTo("");

                long currentTime = System.currentTimeMillis();
                assertThat(refreshToken.getExpiration().getTime()).isBetween(currentTime - 1000,
                        currentTime + 1000);

                // verify
                verify(refreshTokenRepository, times(1)).findByMemberId(differentMemberId);
            }
        }

        @Nested
        @DisplayName("예외 시나리오")
        class ExceptionTest {

            @Test
            @DisplayName("RefreshToken이 존재하지 않을 때 TOKEN_NOT_FOUND_IN_DB 예외가 발생한다")
            void logout_RefreshTokenNotFound() {
                // given
                Long memberId = 1L;
                when(refreshTokenRepository.findByMemberId(memberId)).thenReturn(Optional.empty());

                // when & then
                assertThatThrownBy(() -> authService.logout(memberId))
                        .isInstanceOf(AuthException.class).satisfies(exception -> {
                            AuthException authException = (AuthException) exception;
                            assertThat(authException.getErrorCode())
                                    .isEqualTo(AuthErrorCode.TOKEN_NOT_FOUND_IN_DB);
                        });

                // verify
                verify(refreshTokenRepository, times(1)).findByMemberId(memberId);
            }
        }
    }

    @Nested
    @DisplayName("토큰 검증 테스트")
    class VerifyTokenTest {

        @Nested
        @DisplayName("성공 시나리오")
        class SuccessTest {

            @Test
            @DisplayName("유효한 토큰으로 요청 검증 시 성공한다")
            void verifyTokenIncludedInRequest_Success() {
                // given
                String validToken = "valid-access-token";
                Long memberId = 1L;

                when(jwtTokenProvider.getMemberId(validToken)).thenReturn(memberId.toString());

                // when
                Long result = authService.verifyTokenIncludedInRequest(validToken);

                // then
                assertThat(result).isEqualTo(memberId);

                // verify
                verify(jwtTokenProvider, times(1)).getMemberId(validToken);
            }

            @Test
            @DisplayName("다른 memberId의 유효한 토큰으로 요청 검증 시 성공한다")
            void verifyTokenIncludedInRequest_DifferentMemberId() {
                // given
                String validToken = "different-access-token";
                Long differentMemberId = 999L;

                when(jwtTokenProvider.getMemberId(validToken))
                        .thenReturn(differentMemberId.toString());

                // when
                Long result = authService.verifyTokenIncludedInRequest(validToken);

                // then
                assertThat(result).isEqualTo(differentMemberId);

                // verify
                verify(jwtTokenProvider, times(1)).getMemberId(validToken);
            }
        }

        @Nested
        @DisplayName("예외 시나리오")
        class ExceptionTest {

            @Test
            @DisplayName("JwtTokenProvider에서 예외가 발생할 때 예외가 전파된다")
            void verifyTokenIncludedInRequest_JwtTokenProviderException() {
                // given
                String invalidToken = "invalid-token";

                when(jwtTokenProvider.getMemberId(invalidToken))
                        .thenThrow(new AuthException(AuthErrorCode.INVALID_TOKEN));

                // when & then
                assertThatThrownBy(() -> authService.verifyTokenIncludedInRequest(invalidToken))
                        .isInstanceOf(AuthException.class).satisfies(exception -> {
                            AuthException authException = (AuthException) exception;
                            assertThat(authException.getErrorCode())
                                    .isEqualTo(AuthErrorCode.INVALID_TOKEN);
                        });

                verify(jwtTokenProvider, times(1)).getMemberId(invalidToken);
            }

            @Test
            @DisplayName("memberId가 숫자가 아닐 때 NumberFormatException이 발생한다")
            void verifyTokenIncludedInRequest_InvalidMemberId() {
                // given
                String invalidToken = "token-with-invalid-member-id";

                when(jwtTokenProvider.getMemberId(invalidToken)).thenReturn("invalid-member-id");

                // when & then
                assertThatThrownBy(() -> authService.verifyTokenIncludedInRequest(invalidToken))
                        .isInstanceOf(NumberFormatException.class);

                verify(jwtTokenProvider, times(1)).getMemberId(invalidToken);
            }
        }
    }

    @Nested
    @DisplayName("액세스 토큰 재발급 테스트")
    class ReissueAccessTokenTest {

        @Nested
        @DisplayName("성공 시나리오")
        class SuccessTest {

            @Test
            @DisplayName("유효한 리프레시 토큰으로 액세스 토큰을 재발급할 수 있다")
            void reissueAccessToken_Success() {
                // given
                Long memberId = 1L;
                String bearerRefreshToken = "Bearer refresh-token-456";
                String plainRefreshToken = "refresh-token-456";

                RefreshToken refreshTokenInDB = RefreshToken.builder().memberId(memberId)
                        .token(plainRefreshToken)
                        .expiration(new Date(System.currentTimeMillis() + 86400000)).build();

                TokenInfo newAccessTokenInfo = TokenInfo.builder().token("new-access-token")
                        .expiration(new Date(System.currentTimeMillis() + 3600000)).build();

                when(refreshTokenRepository.findByMemberId(memberId))
                        .thenReturn(Optional.of(refreshTokenInDB));
                when(jwtTokenProvider.reissueAccessTokenIfRefreshTokenIsValid(plainRefreshToken))
                        .thenReturn(newAccessTokenInfo);

                // when
                TokenInfo result = authService.reissueAccessToken(bearerRefreshToken, memberId);

                // then
                assertThat(result).isNotNull();
                assertThat(result.getToken()).isEqualTo(newAccessTokenInfo.getToken());
                assertThat(result.getExpiration()).isEqualTo(newAccessTokenInfo.getExpiration());

                // verify
                verify(refreshTokenRepository, times(1)).findByMemberId(memberId);
                verify(jwtTokenProvider, times(1))
                        .reissueAccessTokenIfRefreshTokenIsValid(plainRefreshToken);
            }

            @Test
            @DisplayName("다른 사용자의 유효한 리프레시 토큰으로 액세스 토큰을 재발급할 수 있다")
            void reissueAccessToken_DifferentUser() {
                // given
                Long anotherMemberId = 999L;
                String bearerRefreshToken = "Bearer another-refresh-token";
                String plainRefreshToken = "another-refresh-token";

                RefreshToken anotherRefreshTokenInDB = RefreshToken.builder()
                        .memberId(anotherMemberId).token(plainRefreshToken)
                        .expiration(new Date(System.currentTimeMillis() + 86400000)).build();

                TokenInfo anotherAccessTokenInfo =
                        TokenInfo.builder().token("another-new-access-token")
                                .expiration(new Date(System.currentTimeMillis() + 3600000)).build();

                when(refreshTokenRepository.findByMemberId(anotherMemberId))
                        .thenReturn(Optional.of(anotherRefreshTokenInDB));
                when(jwtTokenProvider.reissueAccessTokenIfRefreshTokenIsValid(plainRefreshToken))
                        .thenReturn(anotherAccessTokenInfo);

                // when
                TokenInfo result =
                        authService.reissueAccessToken(bearerRefreshToken, anotherMemberId);

                // then
                assertThat(result).isNotNull();
                assertThat(result.getToken()).isEqualTo(anotherAccessTokenInfo.getToken());
                assertThat(result.getExpiration())
                        .isEqualTo(anotherAccessTokenInfo.getExpiration());
            }
        }

        @Nested
        @DisplayName("예외 시나리오")
        class ExceptionTest {

            @Test
            @DisplayName("DB에 리프레시 토큰이 존재하지 않을 때 TOKEN_NOT_FOUND_IN_DB 예외가 발생한다")
            void reissueAccessToken_RefreshTokenNotFoundInDB() {
                // given
                Long memberId = 1L;
                String bearerRefreshToken = "Bearer non-existent-refresh-token";

                when(refreshTokenRepository.findByMemberId(memberId)).thenReturn(Optional.empty());

                // when & then
                assertThatThrownBy(
                        () -> authService.reissueAccessToken(bearerRefreshToken, memberId))
                                .isInstanceOf(AuthException.class).satisfies(exception -> {
                                    AuthException authException = (AuthException) exception;
                                    assertThat(authException.getErrorCode())
                                            .isEqualTo(AuthErrorCode.TOKEN_NOT_FOUND_IN_DB);
                                });

                // verify
                verify(refreshTokenRepository, times(1)).findByMemberId(memberId);
                verify(jwtTokenProvider, times(0)).reissueAccessTokenIfRefreshTokenIsValid(any());
            }

            @Test
            @DisplayName("요청의 리프레시 토큰과 DB의 토큰이 일치하지 않을 때 INVALID_TOKEN 예외가 발생한다")
            void reissueAccessToken_TokenMismatch() {
                // given
                Long memberId = 1L;
                String bearerRefreshToken = "Bearer different-refresh-token";
                String dbRefreshToken = "db-refresh-token";

                RefreshToken refreshTokenInDB = RefreshToken.builder().memberId(memberId)
                        .token(dbRefreshToken)
                        .expiration(new Date(System.currentTimeMillis() + 86400000)).build();

                when(refreshTokenRepository.findByMemberId(memberId))
                        .thenReturn(Optional.of(refreshTokenInDB));

                // when & then
                assertThatThrownBy(
                        () -> authService.reissueAccessToken(bearerRefreshToken, memberId))
                                .isInstanceOf(AuthException.class).satisfies(exception -> {
                                    AuthException authException = (AuthException) exception;
                                    assertThat(authException.getErrorCode())
                                            .isEqualTo(AuthErrorCode.INVALID_TOKEN);
                                });

                // verify
                verify(refreshTokenRepository, times(1)).findByMemberId(memberId);
                verify(jwtTokenProvider, times(0)).reissueAccessTokenIfRefreshTokenIsValid(any());
            }

            @Test
            @DisplayName("Bearer prefix가 없는 토큰으로 요청 시 MISSING_PREFIX 예외가 발생한다")
            void reissueAccessToken_MissingBearerPrefix() {
                // given
                Long memberId = 1L;
                String refreshTokenWithoutBearer = "refresh-token-without-bearer";

                RefreshToken refreshTokenInDB = RefreshToken.builder().memberId(memberId)
                        .token("some-token")
                        .expiration(new Date(System.currentTimeMillis() + 86400000)).build();

                when(refreshTokenRepository.findByMemberId(memberId))
                        .thenReturn(Optional.of(refreshTokenInDB));

                // when & then
                assertThatThrownBy(
                        () -> authService.reissueAccessToken(refreshTokenWithoutBearer, memberId))
                                .isInstanceOf(AuthException.class).satisfies(exception -> {
                                    AuthException authException = (AuthException) exception;
                                    assertThat(authException.getErrorCode())
                                            .isEqualTo(AuthErrorCode.MISSING_PREFIX);
                                });

                // verify
                verify(refreshTokenRepository, times(1)).findByMemberId(memberId);
                verify(jwtTokenProvider, times(0)).reissueAccessTokenIfRefreshTokenIsValid(any());
            }
        }
    }
}
