package git_kkalnane.backend.starbucks.merchant.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import git_kkalnane.backend.starbucks._global.utils.Encryptor;
import git_kkalnane.backend.starbucks.merchant.common.exception.MerchantErrorCode;
import git_kkalnane.backend.starbucks.merchant.common.exception.MerchantException;
import git_kkalnane.backend.starbucks.merchant.domain.Merchant;
import git_kkalnane.backend.starbucks.merchant.dto.request.SignUpRequest;
import git_kkalnane.backend.starbucks.merchant.repository.MerchantRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MerchantServiceTest {

    @Mock
    private MerchantRepository merchantRepository;

    private Encryptor encryptor;

    @InjectMocks
    private MerchantService merchantService;

    private SignUpRequest signUpRequest;
    private Merchant savedMerchant;

    @BeforeEach
    void setUp() {
        encryptor = new Encryptor();
        merchantService = new MerchantService(merchantRepository, encryptor);

        signUpRequest = new SignUpRequest("강남점", "gangnam@starbucks.co.kr", "password123");

        String encryptedPassword = encryptor.encrypt(signUpRequest.password());
        savedMerchant = Merchant.builder().merchantName(signUpRequest.name())
                .email(signUpRequest.email()).passwordHash(encryptedPassword).build();
    }

    @Nested
    @DisplayName("성공 시나리오")
    class SuccessTest {

        @Test
        @DisplayName("정상적인 매장 등록 요청 시 성공적으로 매장을 생성한다")
        void createMerchant_Success() {
            // given
            when(merchantRepository.save(any(Merchant.class))).thenReturn(savedMerchant);

            // when
            merchantService.createMerchant(signUpRequest);

            // then
            verify(merchantRepository, times(1)).save(any(Merchant.class));
            verify(merchantRepository, times(1)).existsByEmail(signUpRequest.email());
        }

        @Test
        @DisplayName("매장 등록 시 비밀번호가 올바르게 암호화된다")
        void createMerchant_PasswordEncryption() {
            // given
            when(merchantRepository.save(any(Merchant.class))).thenReturn(savedMerchant);

            // when
            merchantService.createMerchant(signUpRequest);

            // then
            verify(merchantRepository, times(1)).save(argThat(merchant -> encryptor
                    .isMatch(signUpRequest.password(), merchant.getPasswordHash())));
        }

        @Test
        @DisplayName("매장 등록 시 Merchant 엔티티가 올바른 정보로 생성된다")
        void createMerchant_CorrectMerchantEntity() {
            // given
            when(merchantRepository.save(any(Merchant.class))).thenReturn(savedMerchant);

            // when
            merchantService.createMerchant(signUpRequest);

            // then
            verify(merchantRepository, times(1)).save(argThat(merchant -> merchant.getMerchantName()
                    .equals(signUpRequest.name())
                    && merchant.getEmail().equals(signUpRequest.email())
                    && encryptor.isMatch(signUpRequest.password(), merchant.getPasswordHash())));
        }

        @Test
        @DisplayName("여러 매장을 연속으로 생성할 때 각각 올바르게 처리된다")
        void createMerchant_MultipleMerchants() {
            // given
            SignUpRequest request1 =
                    new SignUpRequest("강남점", "gangnam@starbucks.co.kr", "password1");
            SignUpRequest request2 =
                    new SignUpRequest("홍대점", "hongdae@starbucks.co.kr", "password2");

            String encryptedPassword1 = encryptor.encrypt(request1.password());
            String encryptedPassword2 = encryptor.encrypt(request2.password());

            Merchant merchant1 = Merchant.builder().merchantName(request1.name())
                    .email(request1.email()).passwordHash(encryptedPassword1).build();

            Merchant merchant2 = Merchant.builder().merchantName(request2.name())
                    .email(request2.email()).passwordHash(encryptedPassword2).build();

            when(merchantRepository.save(any(Merchant.class))).thenReturn(merchant1)
                    .thenReturn(merchant2);

            // when
            merchantService.createMerchant(request1);
            merchantService.createMerchant(request2);

            // then
            verify(merchantRepository, times(2)).save(any(Merchant.class));
            verify(merchantRepository, times(1)).existsByEmail(request1.email());
            verify(merchantRepository, times(1)).existsByEmail(request2.email());
        }

        @Test
        @DisplayName("다른 비밀번호로 매장 등록 시 다른 해시값이 생성된다")
        void createMerchant_DifferentPasswordsGenerateDifferentHashes() {
            // given
            SignUpRequest request1 =
                    new SignUpRequest("강남점", "gangnam@starbucks.co.kr", "password1");
            SignUpRequest request2 =
                    new SignUpRequest("홍대점", "hongdae@starbucks.co.kr", "password2");

            when(merchantRepository.save(any(Merchant.class))).thenReturn(savedMerchant);

            // when
            merchantService.createMerchant(request1);
            merchantService.createMerchant(request2);

            // then
            ArgumentCaptor<Merchant> merchantCaptor = ArgumentCaptor.forClass(Merchant.class);
            verify(merchantRepository, times(2)).save(merchantCaptor.capture());

            List<Merchant> savedMerchants = merchantCaptor.getAllValues();

            // 각각의 비밀번호가 올바르게 암호화되었는지 확인
            assertThat(
                    encryptor.isMatch(request1.password(), savedMerchants.get(0).getPasswordHash()))
                            .isTrue();
            assertThat(
                    encryptor.isMatch(request2.password(), savedMerchants.get(1).getPasswordHash()))
                            .isTrue();

            // 두 해시값이 서로 다른지 확인
            assertThat(savedMerchants.get(0).getPasswordHash())
                    .isNotEqualTo(savedMerchants.get(1).getPasswordHash());
        }

        @Test
        @DisplayName("같은 비밀번호라도 매번 다른 해시값이 생성된다")
        void createMerchant_SamePasswordGeneratesDifferentHashes() {
            // given
            SignUpRequest request1 =
                    new SignUpRequest("강남점", "gangnam@starbucks.co.kr", "password123");
            SignUpRequest request2 =
                    new SignUpRequest("홍대점", "hongdae@starbucks.co.kr", "password123");

            when(merchantRepository.save(any(Merchant.class))).thenReturn(savedMerchant);

            // when
            merchantService.createMerchant(request1);
            merchantService.createMerchant(request2);

            // then
            ArgumentCaptor<Merchant> merchantCaptor = ArgumentCaptor.forClass(Merchant.class);
            verify(merchantRepository, times(2)).save(merchantCaptor.capture());

            List<Merchant> savedMerchants = merchantCaptor.getAllValues();

            // 두 비밀번호가 모두 올바르게 암호화되었는지 확인
            assertThat(encryptor.isMatch("password123", savedMerchants.get(0).getPasswordHash()))
                    .isTrue();
            assertThat(encryptor.isMatch("password123", savedMerchants.get(1).getPasswordHash()))
                    .isTrue();

            // 같은 비밀번호이지만 해시값은 서로 다른지 확인 (BCrypt의 salt 특성)
            assertThat(savedMerchants.get(0).getPasswordHash())
                    .isNotEqualTo(savedMerchants.get(1).getPasswordHash());
        }
    }

    @Nested
    @DisplayName("예외 시나리오")
    class ExceptionTest {

        @Test
        @DisplayName("Repository save 실패 시 예외가 발생한다")
        void createMerchant_RepositorySaveFailure() {
            // given
            when(merchantRepository.save(any(Merchant.class)))
                    .thenThrow(new RuntimeException("Database error"));

            // when & then
            assertThatThrownBy(() -> merchantService.createMerchant(signUpRequest))
                    .isInstanceOf(RuntimeException.class).hasMessage("Database error");
        }

        @Test
        @DisplayName("중복된 이메일로 매장 등록 시 MerchantException이 발생한다")
        void createMerchant_DuplicatedEmail() {
            // given
            when(merchantRepository.existsByEmail(signUpRequest.email())).thenReturn(true);

            // when & then
            assertThatThrownBy(() -> merchantService.createMerchant(signUpRequest))
                    .isInstanceOf(MerchantException.class).satisfies(exception -> {
                        MerchantException merchantException = (MerchantException) exception;
                        assertThat(merchantException.getErrorCode())
                                .isEqualTo(MerchantErrorCode.EMAIL_ALREADY_EXISTS);
                    });

            // 예외 발생으로 인해 save가 호출되지 않아야 함
            verify(merchantRepository, times(0)).save(any(Merchant.class));
            verify(merchantRepository, times(1)).existsByEmail(signUpRequest.email());
        }

        @Test
        @DisplayName("이미 존재하는 이메일로 매장 등록 시 예외 메시지가 올바르다")
        void createMerchant_DuplicatedEmail_CorrectMessage() {
            // given
            when(merchantRepository.existsByEmail(signUpRequest.email())).thenReturn(true);

            // when & then
            assertThatThrownBy(() -> merchantService.createMerchant(signUpRequest))
                    .isInstanceOf(MerchantException.class)
                    .hasMessage("[MERCHANT ERROR] 이미 존재하는 이메일입니다.");
        }

        @Test
        @DisplayName("서로 다른 이메일의 중복 검사가 독립적으로 작동한다")
        void createMerchant_DifferentEmailsValidation() {
            // given
            SignUpRequest request1 =
                    new SignUpRequest("강남점", "existing@starbucks.co.kr", "password1");
            SignUpRequest request2 = new SignUpRequest("홍대점", "new@starbucks.co.kr", "password2");

            // existing@starbucks.co.kr은 이미 존재, new@starbucks.co.kr은 존재하지 않음
            when(merchantRepository.existsByEmail("existing@starbucks.co.kr")).thenReturn(true);
            when(merchantRepository.existsByEmail("new@starbucks.co.kr")).thenReturn(false);
            when(merchantRepository.save(any(Merchant.class))).thenReturn(savedMerchant);

            // when & then
            // 첫 번째 요청은 예외 발생
            assertThatThrownBy(() -> merchantService.createMerchant(request1))
                    .isInstanceOf(MerchantException.class).satisfies(exception -> {
                        MerchantException merchantException = (MerchantException) exception;
                        assertThat(merchantException.getErrorCode())
                                .isEqualTo(MerchantErrorCode.EMAIL_ALREADY_EXISTS);
                    });

            // 두 번째 요청은 정상 처리
            merchantService.createMerchant(request2);

            // verify
            verify(merchantRepository, times(1)).existsByEmail("existing@starbucks.co.kr");
            verify(merchantRepository, times(1)).existsByEmail("new@starbucks.co.kr");
            verify(merchantRepository, times(1)).save(any(Merchant.class)); // 두 번째 요청만 저장됨
        }

        @Test
        @DisplayName("null 이메일로 매장 등록 시도 시 existsByEmail이 호출된다")
        void createMerchant_NullEmail() {
            // given
            SignUpRequest nullEmailRequest = new SignUpRequest("강남점", null, "password123");

            // when
            try {
                merchantService.createMerchant(nullEmailRequest);
            } catch (Exception e) {
                // 예외가 발생할 수 있지만 existsByEmail 호출은 확인
            }

            // then
            verify(merchantRepository, times(1)).existsByEmail(null);
        }

        @Test
        @DisplayName("빈 문자열 이메일로 매장 등록 시도 시 existsByEmail이 호출된다")
        void createMerchant_EmptyEmail() {
            // given
            SignUpRequest emptyEmailRequest = new SignUpRequest("강남점", "", "password123");

            // when
            try {
                merchantService.createMerchant(emptyEmailRequest);
            } catch (Exception e) {
                // 예외가 발생할 수 있지만 existsByEmail 호출은 확인
            }

            // then
            verify(merchantRepository, times(1)).existsByEmail("");
        }
    }
}
