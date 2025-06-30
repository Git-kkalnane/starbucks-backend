package git_kkalnane.backend.starbucks.auth.member.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import git_kkalnane.backend.starbucks._global.utils.Encryptor;
import git_kkalnane.backend.starbucks.auth.common.dto.request.LoginRequest;
import git_kkalnane.backend.starbucks.auth.common.exception.AuthErrorCode;
import git_kkalnane.backend.starbucks.auth.common.exception.AuthException;
import git_kkalnane.backend.starbucks.auth.common.jwt.JwtTokenProvider;
import git_kkalnane.backend.starbucks.auth.common.jwt.dto.JwtToken;
import git_kkalnane.backend.starbucks.auth.common.jwt.dto.TokenInfo;
import git_kkalnane.backend.starbucks.auth.member.domain.MemberRefreshToken;
import git_kkalnane.backend.starbucks.auth.member.dto.MemberLoginDto;
import git_kkalnane.backend.starbucks.auth.member.repository.MemberRefreshTokenRepository;
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
class MemberAuthServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private MemberRefreshTokenRepository memberRefreshTokenRepository;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    private Encryptor encryptor;
    private MemberAuthService memberAuthService;

    private LoginRequest validLoginRequest;
    private Member existingMember;
    private JwtToken jwtToken;
    private TokenInfo accessTokenInfo;
    private TokenInfo refreshTokenInfo;

    @BeforeEach
    void setUp() {
        encryptor = new Encryptor();
        memberAuthService = new MemberAuthService(memberRepository,
                memberRefreshTokenRepository, jwtTokenProvider, encryptor);

        String rawPassword = "password123";
        String hashedPassword = encryptor.encrypt(rawPassword);

        validLoginRequest = new LoginRequest("test@example.com", rawPassword);

        existingMember = Member.builder().name("홍길동").nickname("길동이")
                .email("test@example.com").password(hashedPassword).build();

        accessTokenInfo = TokenInfo.builder().token("access-token-123")
                .expiration(new Date(System.currentTimeMillis() + 3600000)).build();

        refreshTokenInfo = TokenInfo.builder().token("refresh-token-456")
                .expiration(new Date(System.currentTimeMillis() + 86400000))
                .build();

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
                when(jwtTokenProvider.createJwtToken("MEMBER",
                        existingMember.getId())).thenReturn(jwtToken);
                when(memberRefreshTokenRepository
                        .findByMemberId(existingMember.getId()))
                        .thenReturn(Optional.empty());
                when(memberRefreshTokenRepository
                        .save(any(MemberRefreshToken.class))).thenReturn(
                        MemberRefreshToken.builder()
                                .memberId(existingMember
                                        .getId())
                                .token(refreshTokenInfo
                                        .getToken())
                                .expiration(refreshTokenInfo
                                        .getExpiration())
                                .build());

                // when
                MemberLoginDto result = memberAuthService.login(validLoginRequest);

                // then
                assertThat(result).isNotNull();
                assertThat(result.accessToken())
                        .isEqualTo(accessTokenInfo.getToken());
                assertThat(result.refreshToken())
                        .isEqualTo(refreshTokenInfo.getToken());
                assertThat(result.name()).isEqualTo(existingMember.getName());
                assertThat(result.nickname())
                        .isEqualTo(existingMember.getNickname());
                assertThat(result.email()).isEqualTo(existingMember.getEmail());

                // verify
                verify(memberRepository, times(1))
                        .findMemberByEmail(validLoginRequest.email());
                verify(jwtTokenProvider, times(1)).createJwtToken("MEMBER",
                        existingMember.getId());
                verify(memberRefreshTokenRepository, times(1))
                        .findByMemberId(existingMember.getId());
                verify(memberRefreshTokenRepository, times(1))
                        .save(any(MemberRefreshToken.class));
            }

            @Test
            @DisplayName("기존 RefreshToken이 있을 때 업데이트한다")
            void login_UpdateExistingRefreshToken() {
                // given
                MemberRefreshToken existingMemberRefreshToken = MemberRefreshToken
                        .builder().memberId(existingMember.getId())
                        .token("old-refresh-token")
                        .expiration(new Date(
                                System.currentTimeMillis() - 1000))
                        .build();

                when(memberRepository.findMemberByEmail(validLoginRequest.email()))
                        .thenReturn(Optional.of(existingMember));
                when(jwtTokenProvider.createJwtToken("MEMBER",
                        existingMember.getId())).thenReturn(jwtToken);
                when(memberRefreshTokenRepository.findByMemberId(
                        existingMember.getId())).thenReturn(Optional
                        .of(existingMemberRefreshToken));

                // when
                MemberLoginDto result = memberAuthService.login(validLoginRequest);

                // then
                assertThat(result).isNotNull();
                assertThat(result.accessToken())
                        .isEqualTo(accessTokenInfo.getToken());
                assertThat(result.refreshToken())
                        .isEqualTo(refreshTokenInfo.getToken());

                // 기존 토큰이 업데이트되었는지 확인
                assertThat(existingMemberRefreshToken.getToken())
                        .isEqualTo(refreshTokenInfo.getToken());
                assertThat(existingMemberRefreshToken.getExpiration())
                        .isEqualTo(refreshTokenInfo.getExpiration());

                // verify
                verify(memberRepository, times(1))
                        .findMemberByEmail(validLoginRequest.email());
                verify(jwtTokenProvider, times(1)).createJwtToken("MEMBER",
                        existingMember.getId());
                verify(memberRefreshTokenRepository, times(1))
                        .findByMemberId(existingMember.getId());
                verify(memberRefreshTokenRepository, times(0))
                        .save(any(MemberRefreshToken.class));
            }
        }

        @Nested
        @DisplayName("예외 시나리오")
        class ExceptionTest {

            @Test
            @DisplayName("존재하지 않는 이메일로 로그인 시 EMAIL_INVALID_EXCEPTION이 발생한다")
            void login_EmailNotFound() {
                // given
                LoginRequest invalidEmailRequest = new LoginRequest(
                        "nonexistent@example.com", "password123");
                when(memberRepository
                        .findMemberByEmail(invalidEmailRequest.email()))
                        .thenReturn(Optional.empty());

                // when & then
                assertThatThrownBy(
                        () -> memberAuthService.login(invalidEmailRequest))
                        .isInstanceOf(AuthException.class)
                        .satisfies(exception -> {
                            AuthException authException =
                                    (AuthException) exception;
                            assertThat(authException
                                    .getErrorCode()).isEqualTo(
                                    AuthErrorCode.EMAIL_INVALID_EXCEPTION);
                        });

                // verify
                verify(memberRepository, times(1))
                        .findMemberByEmail(invalidEmailRequest.email());
                verify(jwtTokenProvider, times(0)).createJwtToken(anyString(),
                        anyLong());
            }

            @Test
            @DisplayName("잘못된 비밀번호로 로그인 시 PASSWORD_INVALID_EXCEPTION이 발생한다")
            void login_WrongPassword() {
                // given
                LoginRequest wrongPasswordRequest = new LoginRequest(
                        "test@example.com", "wrongpassword");
                when(memberRepository.findMemberByEmail(
                        wrongPasswordRequest.email())).thenReturn(
                        Optional.of(existingMember));

                // when & then
                assertThatThrownBy(
                        () -> memberAuthService.login(wrongPasswordRequest))
                        .isInstanceOf(AuthException.class)
                        .satisfies(exception -> {
                            AuthException authException =
                                    (AuthException) exception;
                            assertThat(authException
                                    .getErrorCode()).isEqualTo(
                                    AuthErrorCode.PASSWORD_INVALID_EXCEPTION);
                        });

                // verify
                verify(memberRepository, times(1))
                        .findMemberByEmail(wrongPasswordRequest.email());
                verify(jwtTokenProvider, times(0)).createJwtToken(anyString(),
                        anyLong());
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

                when(jwtTokenProvider.getMemberId("MEMBER", validToken))
                        .thenReturn(memberId.toString());

                // when
                Long result = memberAuthService
                        .verifyTokenIncludedInRequest(validToken);

                // then
                assertThat(result).isEqualTo(memberId);
                verify(jwtTokenProvider, times(1)).getMemberId("MEMBER",
                        validToken);
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

                when(jwtTokenProvider.getMemberId("MEMBER", invalidToken))
                        .thenThrow(new AuthException(
                                AuthErrorCode.INVALID_TOKEN));

                // when & then
                assertThatThrownBy(() -> memberAuthService
                        .verifyTokenIncludedInRequest(invalidToken))
                        .isInstanceOf(AuthException.class)
                        .satisfies(exception -> {
                            AuthException authException =
                                    (AuthException) exception;
                            assertThat(authException
                                    .getErrorCode()).isEqualTo(
                                    AuthErrorCode.INVALID_TOKEN);
                        });

                verify(jwtTokenProvider, times(1)).getMemberId("MEMBER",
                        invalidToken);
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

                MemberRefreshToken memberRefreshTokenInDB = MemberRefreshToken
                        .builder().memberId(memberId)
                        .token(plainRefreshToken)
                        .expiration(new Date(System.currentTimeMillis()
                                + 86400000))
                        .build();

                TokenInfo newAccessTokenInfo = TokenInfo.builder()
                        .token("new-access-token")
                        .expiration(new Date(System.currentTimeMillis()
                                + 3600000))
                        .build();

                when(memberRefreshTokenRepository.findByMemberId(memberId))
                        .thenReturn(Optional.of(memberRefreshTokenInDB));
                when(jwtTokenProvider.reissueAccessTokenIfRefreshTokenIsValid(
                        "MEMBER", plainRefreshToken))
                        .thenReturn(newAccessTokenInfo);

                // when
                TokenInfo result = memberAuthService
                        .reissueAccessToken(bearerRefreshToken, memberId);

                // then
                assertThat(result).isNotNull();
                assertThat(result.getToken())
                        .isEqualTo(newAccessTokenInfo.getToken());
                assertThat(result.getExpiration())
                        .isEqualTo(newAccessTokenInfo.getExpiration());

                // verify
                verify(memberRefreshTokenRepository, times(1))
                        .findByMemberId(memberId);
                verify(jwtTokenProvider, times(1))
                        .reissueAccessTokenIfRefreshTokenIsValid("MEMBER",
                                plainRefreshToken);
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

                when(memberRefreshTokenRepository.findByMemberId(memberId))
                        .thenReturn(Optional.empty());

                // when & then
                assertThatThrownBy(() -> memberAuthService
                        .reissueAccessToken(bearerRefreshToken, memberId))
                        .isInstanceOf(AuthException.class)
                        .satisfies(exception -> {
                            AuthException authException =
                                    (AuthException) exception;
                            assertThat(authException
                                    .getErrorCode()).isEqualTo(
                                    AuthErrorCode.TOKEN_NOT_FOUND_IN_DB);
                        });

                // verify
                verify(memberRefreshTokenRepository, times(1))
                        .findByMemberId(memberId);
                verify(jwtTokenProvider, times(0))
                        .reissueAccessTokenIfRefreshTokenIsValid(
                                anyString(), anyString());
            }

            @Test
            @DisplayName("요청의 리프레시 토큰과 DB의 토큰이 일치하지 않을 때 INVALID_TOKEN 예외가 발생한다")
            void reissueAccessToken_TokenMismatch() {
                // given
                Long memberId = 1L;
                String bearerRefreshToken = "Bearer request-refresh-token";
                String dbRefreshToken = "db-refresh-token";

                MemberRefreshToken memberRefreshTokenInDB = MemberRefreshToken
                        .builder().memberId(memberId).token(dbRefreshToken)
                        .expiration(new Date(System.currentTimeMillis()
                                + 86400000))
                        .build();

                when(memberRefreshTokenRepository.findByMemberId(memberId))
                        .thenReturn(Optional.of(memberRefreshTokenInDB));

                // when & then
                assertThatThrownBy(() -> memberAuthService
                        .reissueAccessToken(bearerRefreshToken, memberId))
                        .isInstanceOf(AuthException.class)
                        .satisfies(exception -> {
                            AuthException authException =
                                    (AuthException) exception;
                            assertThat(authException
                                    .getErrorCode()).isEqualTo(
                                    AuthErrorCode.INVALID_TOKEN);
                        });

                // verify
                verify(memberRefreshTokenRepository, times(1))
                        .findByMemberId(memberId);
                verify(jwtTokenProvider, times(0))
                        .reissueAccessTokenIfRefreshTokenIsValid(
                                anyString(), anyString());
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
                MemberRefreshToken memberRefreshToken = MemberRefreshToken.builder()
                        .memberId(memberId).token("valid-refresh-token")
                        .expiration(new Date(System.currentTimeMillis()
                                + 86400000))
                        .build();

                when(memberRefreshTokenRepository.findByMemberId(memberId))
                        .thenReturn(Optional.of(memberRefreshToken));

                // when
                memberAuthService.logout(memberId);

                // then
                assertThat(memberRefreshToken.getToken()).isEqualTo("");

                // 만료 시간이 현재 시간으로 설정되었는지 확인 (1초 이내)
                long currentTime = System.currentTimeMillis();
                assertThat(memberRefreshToken.getExpiration().getTime())
                        .isBetween(currentTime - 1000, currentTime + 1000);

                // verify
                verify(memberRefreshTokenRepository, times(1))
                        .findByMemberId(memberId);
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
                when(memberRefreshTokenRepository.findByMemberId(memberId))
                        .thenReturn(Optional.empty());

                // when & then
                assertThatThrownBy(() -> memberAuthService.logout(memberId))
                        .isInstanceOf(AuthException.class)
                        .satisfies(exception -> {
                            AuthException authException =
                                    (AuthException) exception;
                            assertThat(authException.getErrorCode())
                                    .isEqualTo(AuthErrorCode.TOKEN_NOT_FOUND_IN_DB);
                        });

                // verify
                verify(memberRefreshTokenRepository, times(1))
                        .findByMemberId(memberId);
            }
        }
    }
}
