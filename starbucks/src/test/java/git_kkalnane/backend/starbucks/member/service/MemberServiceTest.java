package git_kkalnane.backend.starbucks.member.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import git_kkalnane.backend.starbucks._global.utils.Encryptor;
import git_kkalnane.backend.starbucks.member.common.exception.MemberErrorCode;
import git_kkalnane.backend.starbucks.member.common.exception.MemberException;
import git_kkalnane.backend.starbucks.member.domain.Member;
import git_kkalnane.backend.starbucks.member.dto.request.SignUpRequest;
import git_kkalnane.backend.starbucks.member.dto.response.SignUpResponse;
import git_kkalnane.backend.starbucks.member.repository.MemberRepository;
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
class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    private Encryptor encryptor;

    @InjectMocks
    private MemberService memberService;

    private SignUpRequest signUpRequest;
    private Member savedMember;

    @BeforeEach
    void setUp() {
        encryptor = new Encryptor();
        memberService = new MemberService(memberRepository, encryptor);

        signUpRequest = new SignUpRequest("홍길동", "나는야홍길동", "test@example.com", "password123");

        String encryptedPassword = encryptor.encrypt(signUpRequest.password());
        savedMember = Member.builder().name(signUpRequest.name()).nickname(signUpRequest.nickname())
                .email(signUpRequest.email()).password(encryptedPassword).build();
    }

    @Nested
    @DisplayName("성공 시나리오")
    class SuccessTest {
        @Test
        @DisplayName("정상적인 회원가입 요청 시 성공적으로 회원을 생성한다")
        void createMember_Success() {
            // given
            when(memberRepository.save(any(Member.class))).thenReturn(savedMember);

            // when
            SignUpResponse response = memberService.createMember(signUpRequest);

            // then
            assertThat(response).isNotNull();
            assertThat(response.name()).isEqualTo(signUpRequest.name());
            verify(memberRepository, times(1)).save(any(Member.class));
        }

        @Test
        @DisplayName("회원가입 시 비밀번호가 올바르게 암호화된다")
        void createMember_PasswordEncryption() {
            // given
            when(memberRepository.save(any(Member.class))).thenReturn(savedMember);

            // when
            memberService.createMember(signUpRequest);

            // then
            verify(memberRepository, times(1)).save(argThat(
                    member -> encryptor.isMatch(signUpRequest.password(), member.getPassword())));
        }

        @Test
        @DisplayName("회원가입 시 Member 엔티티가 올바른 정보로 생성된다")
        void createMember_CorrectMemberEntity() {
            // given
            when(memberRepository.save(any(Member.class))).thenReturn(savedMember);

            // when
            memberService.createMember(signUpRequest);

            // then
            verify(memberRepository, times(1))
                    .save(argThat(member -> member.getName().equals(signUpRequest.name())
                            && member.getNickname().equals(signUpRequest.nickname())
                            && member.getEmail().equals(signUpRequest.email())
                            && encryptor.isMatch(signUpRequest.password(), member.getPassword())));
        }

        @Test
        @DisplayName("여러 회원을 연속으로 생성할 때 각각 올바르게 처리된다")
        void createMember_MultipleMembers() {
            // given
            SignUpRequest request1 =
                    new SignUpRequest("김철수", "철수", "user1@example.com", "password1");
            SignUpRequest request2 =
                    new SignUpRequest("김영희", "영희", "user2@example.com", "password2");

            String encryptedPassword1 = encryptor.encrypt(request1.password());
            String encryptedPassword2 = encryptor.encrypt(request2.password());

            Member member1 = Member.builder().name(request1.name()).nickname(request1.nickname())
                    .email(request1.email()).password(encryptedPassword1).build();

            Member member2 = Member.builder().name(request2.name()).nickname(request2.nickname())
                    .email(request2.email()).password(encryptedPassword2).build();

            when(memberRepository.save(any(Member.class))).thenReturn(member1).thenReturn(member2);

            // when
            SignUpResponse response1 = memberService.createMember(request1);
            SignUpResponse response2 = memberService.createMember(request2);

            // then
            assertThat(response1.name()).isEqualTo("김철수");
            assertThat(response2.name()).isEqualTo("김영희");
            verify(memberRepository, times(2)).save(any(Member.class));
        }

        @Test
        @DisplayName("다른 비밀번호로 회원가입 시 다른 해시값이 생성된다")
        void createMember_DifferentPasswordsGenerateDifferentHashes() {
            // given
            SignUpRequest request1 =
                    new SignUpRequest("김철수", "철수", "user1@example.com", "password1");
            SignUpRequest request2 =
                    new SignUpRequest("김영희", "영희", "user2@example.com", "password2");

            when(memberRepository.save(any(Member.class))).thenReturn(savedMember);

            // when
            memberService.createMember(request1);
            memberService.createMember(request2);

            // then
            ArgumentCaptor<Member> memberCaptor = ArgumentCaptor.forClass(Member.class);
            verify(memberRepository, times(2)).save(memberCaptor.capture());

            List<Member> savedMembers = memberCaptor.getAllValues();

            // 각각의 비밀번호가 올바르게 암호화되었는지 확인
            assertThat(encryptor.isMatch(request1.password(), savedMembers.get(0).getPassword()))
                    .isTrue();
            assertThat(encryptor.isMatch(request2.password(), savedMembers.get(1).getPassword()))
                    .isTrue();

            // 두 해시값이 서로 다른지 확인
            assertThat(savedMembers.get(0).getPassword())
                    .isNotEqualTo(savedMembers.get(1).getPassword());
        }

        @Test
        @DisplayName("같은 비밀번호라도 매번 다른 해시값이 생성된다")
        void createMember_SamePasswordGeneratesDifferentHashes() {
            // given
            SignUpRequest request1 =
                    new SignUpRequest("김철수", "철수", "user1@example.com", "password123");
            SignUpRequest request2 =
                    new SignUpRequest("김영희", "영희", "user2@example.com", "password123");

            when(memberRepository.save(any(Member.class))).thenReturn(savedMember);

            // when
            memberService.createMember(request1);
            memberService.createMember(request2);

            // then
            ArgumentCaptor<Member> memberCaptor = ArgumentCaptor.forClass(Member.class);
            verify(memberRepository, times(2)).save(memberCaptor.capture());

            List<Member> savedMembers = memberCaptor.getAllValues();

            // 두 비밀번호가 모두 올바르게 암호화되었는지 확인
            assertThat(encryptor.isMatch("password123", savedMembers.get(0).getPassword()))
                    .isTrue();
            assertThat(encryptor.isMatch("password123", savedMembers.get(1).getPassword()))
                    .isTrue();

            // 같은 비밀번호이지만 해시값은 서로 다른지 확인 (BCrypt의 salt 특성)
            assertThat(savedMembers.get(0).getPassword())
                    .isNotEqualTo(savedMembers.get(1).getPassword());
        }
    }

    @Nested
    @DisplayName("예외 시나리오")
    class ExceptionTest {
        @Test
        @DisplayName("Repository save 실패 시 예외가 발생한다")
        void createMember_RepositorySaveFailure() {
            // given
            when(memberRepository.save(any(Member.class)))
                    .thenThrow(new RuntimeException("Database error"));

            // when & then
            assertThatThrownBy(() -> memberService.createMember(signUpRequest))
                    .isInstanceOf(RuntimeException.class).hasMessage("Database error");
        }

        @Test
        @DisplayName("중복된 이메일로 회원가입 시 MemberException이 발생한다")
        void createMember_DuplicatedEmail() {
            // given
            when(memberRepository.existsByEmail(signUpRequest.email())).thenReturn(true);

            // when & then
            assertThatThrownBy(() -> memberService.createMember(signUpRequest))
                    .isInstanceOf(MemberException.class).satisfies(exception -> {
                        MemberException memberException = (MemberException) exception;
                        assertThat(memberException.getErrorCode())
                                .isEqualTo(MemberErrorCode.EMAIL_ALREADY_EXISTS);
                    });

            // verify
            verify(memberRepository, times(1)).existsByEmail(signUpRequest.email());
            verify(memberRepository, times(0)).save(any(Member.class)); // save는 호출되지 않아야 함
        }

        @Test
        @DisplayName("이미 존재하는 이메일로 회원가입 시 예외 메시지가 올바르다")
        void createMember_DuplicatedEmail_CorrectMessage() {
            // given
            when(memberRepository.existsByEmail(signUpRequest.email())).thenReturn(true);

            // when & then
            assertThatThrownBy(() -> memberService.createMember(signUpRequest))
                    .isInstanceOf(MemberException.class).hasMessage("이미 존재하는 이메일입니다.");
        }

        @Test
        @DisplayName("서로 다른 이메일의 중복 검사가 독립적으로 작동한다")
        void createMember_DifferentEmailsValidation() {
            // given
            SignUpRequest request1 =
                    new SignUpRequest("김철수", "철수", "existing@example.com", "password1");
            SignUpRequest request2 = new SignUpRequest("김영희", "영희", "new@example.com", "password2");

            // existing@example.com은 이미 존재, new@example.com은 존재하지 않음
            when(memberRepository.existsByEmail("existing@example.com")).thenReturn(true);
            when(memberRepository.existsByEmail("new@example.com")).thenReturn(false);
            when(memberRepository.save(any(Member.class))).thenReturn(savedMember);

            // when & then
            // 첫 번째 요청은 예외 발생
            assertThatThrownBy(() -> memberService.createMember(request1))
                    .isInstanceOf(MemberException.class).satisfies(exception -> {
                        MemberException memberException = (MemberException) exception;
                        assertThat(memberException.getErrorCode())
                                .isEqualTo(MemberErrorCode.EMAIL_ALREADY_EXISTS);
                    });

            // 두 번째 요청은 성공
            SignUpResponse response = memberService.createMember(request2);
            assertThat(response).isNotNull();

            // verify
            verify(memberRepository, times(1)).existsByEmail("existing@example.com");
            verify(memberRepository, times(1)).existsByEmail("new@example.com");
            verify(memberRepository, times(1)).save(any(Member.class)); // 두 번째 요청에서만 save 호출
        }

        @Test
        @DisplayName("null 이메일로 회원가입 시도 시 existsByEmail이 호출된다")
        void createMember_NullEmail() {
            // given
            SignUpRequest nullEmailRequest = new SignUpRequest("홍길동", "길동", null, "password123");
            when(memberRepository.existsByEmail(null)).thenReturn(false); // null에 대해서는 false 반환
            when(memberRepository.save(any(Member.class))).thenReturn(savedMember);

            // when
            SignUpResponse response = memberService.createMember(nullEmailRequest);

            // then
            assertThat(response).isNotNull();
            verify(memberRepository, times(1)).existsByEmail(null);
            verify(memberRepository, times(1)).save(any(Member.class));
        }

        @Test
        @DisplayName("빈 문자열 이메일로 회원가입 시도 시 existsByEmail이 호출된다")
        void createMember_EmptyEmail() {
            // given
            SignUpRequest emptyEmailRequest = new SignUpRequest("홍길동", "길동", "", "password123");
            when(memberRepository.existsByEmail("")).thenReturn(false);
            when(memberRepository.save(any(Member.class))).thenReturn(savedMember);

            // when
            SignUpResponse response = memberService.createMember(emptyEmailRequest);

            // then
            assertThat(response).isNotNull();
            verify(memberRepository, times(1)).existsByEmail("");
            verify(memberRepository, times(1)).save(any(Member.class));
        }
    }
}
