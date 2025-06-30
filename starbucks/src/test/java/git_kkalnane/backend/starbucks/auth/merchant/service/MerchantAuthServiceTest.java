package git_kkalnane.backend.starbucks.auth.merchant.service;

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
import git_kkalnane.backend.starbucks.auth.merchant.domain.MerchantRefreshToken;
import git_kkalnane.backend.starbucks.auth.merchant.dto.MerchantLoginDto;
import git_kkalnane.backend.starbucks.auth.merchant.repository.MerchantRefreshTokenRepository;
import git_kkalnane.backend.starbucks.merchant.domain.Merchant;
import git_kkalnane.backend.starbucks.merchant.repository.MerchantRepository;
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
class MerchantAuthServiceTest {

    @Mock
    private MerchantRepository merchantRepository;

    @Mock
    private MerchantRefreshTokenRepository merchantRefreshTokenRepository;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    private Encryptor encryptor;
    private MerchantAuthService merchantAuthService;

    private LoginRequest validLoginRequest;
    private Merchant existingMerchant;
    private JwtToken jwtToken;
    private TokenInfo accessTokenInfo;
    private TokenInfo refreshTokenInfo;

    @BeforeEach
    void setUp() {
        encryptor = new Encryptor();
        merchantAuthService = new MerchantAuthService(merchantRepository,
                merchantRefreshTokenRepository, jwtTokenProvider, encryptor);

        String rawPassword = "password123";
        String hashedPassword = encryptor.encrypt(rawPassword);

        validLoginRequest = new LoginRequest("merchant@starbucks.co.kr", rawPassword);

        existingMerchant = Merchant.builder().id(1L).merchantName("강남점")
                .email("merchant@starbucks.co.kr").passwordHash(hashedPassword).build();

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
                when(merchantRepository.findByEmail(validLoginRequest.email()))
                        .thenReturn(Optional.of(existingMerchant));
                when(jwtTokenProvider.createJwtToken("MERCHANT", existingMerchant.getId()))
                        .thenReturn(jwtToken);
                when(merchantRefreshTokenRepository.findByMemberId(existingMerchant.getId()))
                        .thenReturn(Optional.empty());
                when(merchantRefreshTokenRepository.save(any(MerchantRefreshToken.class)))
                        .thenReturn(
                                MerchantRefreshToken.builder().memberId(existingMerchant.getId())
                                        .token(refreshTokenInfo.getToken())
                                        .expiration(refreshTokenInfo.getExpiration()).build());

                // when
                MerchantLoginDto result = merchantAuthService.login(validLoginRequest);

                // then
                assertThat(result).isNotNull();
                assertThat(result.accessToken()).isEqualTo(accessTokenInfo.getToken());
                assertThat(result.refreshToken()).isEqualTo(refreshTokenInfo.getToken());
                assertThat(result.name()).isEqualTo(existingMerchant.getMerchantName());
                assertThat(result.email()).isEqualTo(existingMerchant.getEmail());

                // verify
                verify(merchantRepository, times(1)).findByEmail(validLoginRequest.email());
                verify(jwtTokenProvider, times(1)).createJwtToken("MERCHANT",
                        existingMerchant.getId());
                verify(merchantRefreshTokenRepository, times(1))
                        .findByMemberId(existingMerchant.getId());
                verify(merchantRefreshTokenRepository, times(1))
                        .save(any(MerchantRefreshToken.class));
            }

            @Test
            @DisplayName("기존 RefreshToken이 있을 때 업데이트한다")
            void login_UpdateExistingRefreshToken() {
                // given
                MerchantRefreshToken existingMerchantRefreshToken = MerchantRefreshToken.builder()
                        .memberId(existingMerchant.getId()).token("old-refresh-token")
                        .expiration(new Date(System.currentTimeMillis() - 1000)).build();

                when(merchantRepository.findByEmail(validLoginRequest.email()))
                        .thenReturn(Optional.of(existingMerchant));
                when(jwtTokenProvider.createJwtToken("MERCHANT", existingMerchant.getId()))
                        .thenReturn(jwtToken);
                when(merchantRefreshTokenRepository.findByMemberId(existingMerchant.getId()))
                        .thenReturn(Optional.of(existingMerchantRefreshToken));

                // when
                MerchantLoginDto result = merchantAuthService.login(validLoginRequest);

                // then
                assertThat(result).isNotNull();
                assertThat(result.accessToken()).isEqualTo(accessTokenInfo.getToken());
                assertThat(result.refreshToken()).isEqualTo(refreshTokenInfo.getToken());

                // 기존 토큰이 업데이트되었는지 확인
                assertThat(existingMerchantRefreshToken.getToken())
                        .isEqualTo(refreshTokenInfo.getToken());
                assertThat(existingMerchantRefreshToken.getExpiration())
                        .isEqualTo(refreshTokenInfo.getExpiration());

                // verify
                verify(merchantRepository, times(1)).findByEmail(validLoginRequest.email());
                verify(jwtTokenProvider, times(1)).createJwtToken("MERCHANT",
                        existingMerchant.getId());
                verify(merchantRefreshTokenRepository, times(1))
                        .findByMemberId(existingMerchant.getId());
                verify(merchantRefreshTokenRepository, times(0))
                        .save(any(MerchantRefreshToken.class));
            }

            @Test
            @DisplayName("다른 매장의 로그인 요청도 성공한다")
            void login_DifferentMerchant() {
                // given
                String anotherRawPassword = "another123";
                String anotherHashedPassword = encryptor.encrypt(anotherRawPassword);
                LoginRequest anotherRequest =
                        new LoginRequest("hongdae@starbucks.co.kr", anotherRawPassword);

                Merchant anotherMerchant = Merchant.builder().id(2L).merchantName("홍대점")
                        .email("hongdae@starbucks.co.kr").passwordHash(anotherHashedPassword)
                        .build();

                when(merchantRepository.findByEmail(anotherRequest.email()))
                        .thenReturn(Optional.of(anotherMerchant));
                when(jwtTokenProvider.createJwtToken("MERCHANT", anotherMerchant.getId()))
                        .thenReturn(jwtToken);
                when(merchantRefreshTokenRepository.findByMemberId(anotherMerchant.getId()))
                        .thenReturn(Optional.empty());

                // when
                MerchantLoginDto result = merchantAuthService.login(anotherRequest);

                // then
                assertThat(result).isNotNull();
                assertThat(result.name()).isEqualTo(anotherMerchant.getMerchantName());
                assertThat(result.email()).isEqualTo(anotherMerchant.getEmail());

                verify(merchantRepository, times(1)).findByEmail(anotherRequest.email());
                verify(jwtTokenProvider, times(1)).createJwtToken("MERCHANT",
                        anotherMerchant.getId());
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
                        new LoginRequest("nonexistent@starbucks.co.kr", "password123");
                when(merchantRepository.findByEmail(invalidEmailRequest.email()))
                        .thenReturn(Optional.empty());

                // when & then
                assertThatThrownBy(() -> merchantAuthService.login(invalidEmailRequest))
                        .isInstanceOf(AuthException.class).satisfies(exception -> {
                            AuthException authException = (AuthException) exception;
                            assertThat(authException.getErrorCode())
                                    .isEqualTo(AuthErrorCode.EMAIL_INVALID_EXCEPTION);
                        });

                verify(merchantRepository, times(1)).findByEmail(invalidEmailRequest.email());
                verify(jwtTokenProvider, times(0)).createJwtToken(anyString(), anyLong());
                verify(merchantRefreshTokenRepository, times(0)).findByMemberId(anyLong());
            }

            @Test
            @DisplayName("잘못된 비밀번호로 로그인 시 PASSWORD_INVALID_EXCEPTION이 발생한다")
            void login_WrongPassword() {
                // given
                LoginRequest wrongPasswordRequest =
                        new LoginRequest("merchant@starbucks.co.kr", "wrongpassword");
                when(merchantRepository.findByEmail(wrongPasswordRequest.email()))
                        .thenReturn(Optional.of(existingMerchant));

                // when & then
                assertThatThrownBy(() -> merchantAuthService.login(wrongPasswordRequest))
                        .isInstanceOf(AuthException.class).satisfies(exception -> {
                            AuthException authException = (AuthException) exception;
                            assertThat(authException.getErrorCode())
                                    .isEqualTo(AuthErrorCode.PASSWORD_INVALID_EXCEPTION);
                        });

                verify(merchantRepository, times(1)).findByEmail(wrongPasswordRequest.email());
                verify(jwtTokenProvider, times(0)).createJwtToken(anyString(), anyLong());
                verify(merchantRefreshTokenRepository, times(0)).findByMemberId(anyLong());
            }

            @Test
            @DisplayName("빈 비밀번호로 로그인 시 PASSWORD_INVALID_EXCEPTION이 발생한다")
            void login_EmptyPassword() {
                // given
                LoginRequest emptyPasswordRequest =
                        new LoginRequest("merchant@starbucks.co.kr", "");
                when(merchantRepository.findByEmail(emptyPasswordRequest.email()))
                        .thenReturn(Optional.of(existingMerchant));

                // when & then
                assertThatThrownBy(() -> merchantAuthService.login(emptyPasswordRequest))
                        .isInstanceOf(AuthException.class).satisfies(exception -> {
                            AuthException authException = (AuthException) exception;
                            assertThat(authException.getErrorCode())
                                    .isEqualTo(AuthErrorCode.PASSWORD_INVALID_EXCEPTION);
                        });

                verify(merchantRepository, times(1)).findByEmail(emptyPasswordRequest.email());
                verify(jwtTokenProvider, times(0)).createJwtToken(anyString(), anyLong());
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
                String validToken = "valid-jwt-token";
                Long expectedMerchantId = 1L;
                when(jwtTokenProvider.getMemberId("MERCHANT", validToken))
                        .thenReturn(expectedMerchantId.toString());

                // when
                Long result = merchantAuthService.verifyTokenIncludedInRequest(validToken);

                // then
                assertThat(result).isEqualTo(expectedMerchantId);
                verify(jwtTokenProvider, times(1)).getMemberId("MERCHANT", validToken);
            }

            @Test
            @DisplayName("다른 매장의 토큰 검증도 성공한다")
            void verifyTokenIncludedInRequest_DifferentMerchant() {
                // given
                String anotherValidToken = "another-valid-jwt-token";
                Long anotherMerchantId = 2L;
                when(jwtTokenProvider.getMemberId("MERCHANT", anotherValidToken))
                        .thenReturn(anotherMerchantId.toString());

                // when
                Long result = merchantAuthService.verifyTokenIncludedInRequest(anotherValidToken);

                // then
                assertThat(result).isEqualTo(anotherMerchantId);
                verify(jwtTokenProvider, times(1)).getMemberId("MERCHANT", anotherValidToken);
            }
        }

        @Nested
        @DisplayName("예외 시나리오")
        class ExceptionTest {

            @Test
            @DisplayName("JwtTokenProvider에서 예외가 발생할 때 예외가 전파된다")
            void verifyTokenIncludedInRequest_JwtTokenProviderException() {
                // given
                String invalidToken = "invalid-jwt-token";
                when(jwtTokenProvider.getMemberId("MERCHANT", invalidToken))
                        .thenThrow(new RuntimeException("토큰 파싱 실패"));

                // when & then
                assertThatThrownBy(
                        () -> merchantAuthService.verifyTokenIncludedInRequest(invalidToken))
                                .isInstanceOf(RuntimeException.class).hasMessage("토큰 파싱 실패");

                verify(jwtTokenProvider, times(1)).getMemberId("MERCHANT", invalidToken);
            }

            @Test
            @DisplayName("null 토큰으로 검증 시 예외가 발생한다")
            void verifyTokenIncludedInRequest_NullToken() {
                // given
                when(jwtTokenProvider.getMemberId("MERCHANT", null))
                        .thenThrow(new NullPointerException("토큰이 null입니다"));

                // when & then
                assertThatThrownBy(() -> merchantAuthService.verifyTokenIncludedInRequest(null))
                        .isInstanceOf(NullPointerException.class).hasMessage("토큰이 null입니다");

                verify(jwtTokenProvider, times(1)).getMemberId("MERCHANT", null);
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
            @DisplayName("기존 토큰이 없을 때 새로 저장한다")
            void saveRefreshToken_NewToken() {
                // given
                Long merchantId = 1L;
                when(merchantRefreshTokenRepository.findByMemberId(merchantId))
                        .thenReturn(Optional.empty());

                // when
                merchantAuthService.saveRefreshTokenToRepository(merchantId, refreshTokenInfo);

                // then
                verify(merchantRefreshTokenRepository, times(1)).findByMemberId(merchantId);
                verify(merchantRefreshTokenRepository, times(1))
                        .save(any(MerchantRefreshToken.class));
            }

            @Test
            @DisplayName("기존 토큰이 있을 때 업데이트한다")
            void saveRefreshToken_UpdateExistingToken() {
                // given
                Long merchantId = 1L;
                MerchantRefreshToken existingToken =
                        MerchantRefreshToken.builder().memberId(merchantId).token("old-token")
                                .expiration(new Date(System.currentTimeMillis() - 1000)).build();

                when(merchantRefreshTokenRepository.findByMemberId(merchantId))
                        .thenReturn(Optional.of(existingToken));

                // when
                merchantAuthService.saveRefreshTokenToRepository(merchantId, refreshTokenInfo);

                // then
                assertThat(existingToken.getToken()).isEqualTo(refreshTokenInfo.getToken());
                assertThat(existingToken.getExpiration())
                        .isEqualTo(refreshTokenInfo.getExpiration());

                verify(merchantRefreshTokenRepository, times(1)).findByMemberId(merchantId);
                verify(merchantRefreshTokenRepository, times(0))
                        .save(any(MerchantRefreshToken.class));
            }

            @Test
            @DisplayName("여러 매장의 토큰을 각각 관리한다")
            void saveRefreshToken_MultipleMerchants() {
                // given
                Long merchantId1 = 1L;
                Long merchantId2 = 2L;

                when(merchantRefreshTokenRepository.findByMemberId(merchantId1))
                        .thenReturn(Optional.empty());
                when(merchantRefreshTokenRepository.findByMemberId(merchantId2))
                        .thenReturn(Optional.empty());

                // when
                merchantAuthService.saveRefreshTokenToRepository(merchantId1, refreshTokenInfo);
                merchantAuthService.saveRefreshTokenToRepository(merchantId2, refreshTokenInfo);

                // then
                verify(merchantRefreshTokenRepository, times(1)).findByMemberId(merchantId1);
                verify(merchantRefreshTokenRepository, times(1)).findByMemberId(merchantId2);
                verify(merchantRefreshTokenRepository, times(2))
                        .save(any(MerchantRefreshToken.class));
            }
        }
    }
}
