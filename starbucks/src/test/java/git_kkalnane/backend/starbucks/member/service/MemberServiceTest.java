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
import git_kkalnane.backend.starbucks.member.dto.request.UpdateNicknameRequest;
import git_kkalnane.backend.starbucks.member.dto.request.UpdatePasswordRequest;
import git_kkalnane.backend.starbucks.member.dto.response.MemberDetailInfo;
import git_kkalnane.backend.starbucks.member.dto.response.SignUpResponse;
import git_kkalnane.backend.starbucks.member.event.MemberSignedUpEvent;
import git_kkalnane.backend.starbucks.member.repository.MemberRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    private Encryptor encryptor;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private MemberService memberService;

    private SignUpRequest signUpRequest;
    private Member savedMember;

    @BeforeEach
    void setUp() {
        encryptor = new Encryptor();
        memberService = new MemberService(memberRepository, encryptor, eventPublisher);

        signUpRequest = new SignUpRequest("홍길동", "나는야홍길동", "test@example.com", "password123");

        String encryptedPassword = encryptor.encrypt(signUpRequest.password());
        savedMember = Member.builder()
                .name(signUpRequest.name())
                .nickname(signUpRequest.nickname())
                .email(signUpRequest.email())
                .password(encryptedPassword)
                .build();
    }

    @Nested
    @DisplayName("회원가입 테스트")
    class SignUpTest {

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
                verify(eventPublisher, times(1)).publishEvent(any(MemberSignedUpEvent.class));
            }

            @Test
            @DisplayName("회원가입 시 비밀번호가 올바르게 암호화된다")
            void createMember_PasswordEncryption() {
                // given
                when(memberRepository.save(any(Member.class))).thenReturn(savedMember);

                // when
                memberService.createMember(signUpRequest);

                // then
                verify(memberRepository, times(1))
                        .save(argThat(member -> encryptor.isMatch(signUpRequest.password(), member.getPassword())));
            }

            @Test
            @DisplayName("회원가입 시 Member 엔티티가 올바른 정보로 생성된다")
            void createMember_CorrectMemberEntity() {
                // given
                when(memberRepository.save(any(Member.class))).thenReturn(savedMember);

                // when
                memberService.createMember(signUpRequest);

                // then
                verify(memberRepository, times(1)).save(argThat(member -> member
                        .getName().equals(signUpRequest.name())
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

                Member member1 = Member.builder()
                        .name(request1.name())
                        .nickname(request1.nickname())
                        .email(request1.email())
                        .password(encryptedPassword1)
                        .build();

                Member member2 = Member.builder()
                        .name(request2.name())
                        .nickname(request2.nickname())
                        .email(request2.email())
                        .password(encryptedPassword2)
                        .build();

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
                assertThat(encryptor.isMatch(request1.password(), savedMembers.get(0).getPassword())).isTrue();
                assertThat(encryptor.isMatch(request2.password(), savedMembers.get(1).getPassword())).isTrue();

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
                assertThat(encryptor.isMatch("password123", savedMembers.get(0).getPassword())).isTrue();
                assertThat(encryptor.isMatch("password123", savedMembers.get(1).getPassword())).isTrue();

                // 같은 비밀번호이지만 해시값은 서로 다른지 확인 (BCrypt의 salt 특성)
                assertThat(savedMembers.get(0).getPassword()).isNotEqualTo(savedMembers.get(1).getPassword());
            }

            @Test
            @DisplayName("회원가입 시 MemberSignedUpEvent가 올바른 정보로 발행된다")
            void createMember_EventPublished() {
                // given
                when(memberRepository.save(any(Member.class)))
                        .thenReturn(savedMember);

                // when
                memberService.createMember(signUpRequest);

                // then
                ArgumentCaptor<MemberSignedUpEvent> eventCaptor =
                        ArgumentCaptor.forClass(MemberSignedUpEvent.class);
                verify(eventPublisher, times(1))
                        .publishEvent(eventCaptor.capture());

                MemberSignedUpEvent capturedEvent = eventCaptor.getValue();
                assertThat(capturedEvent).isNotNull();
                assertThat(capturedEvent.getSource()).isEqualTo(memberService);
                assertThat(capturedEvent.getMember()).isEqualTo(savedMember);
            }
        }

        @Nested
        @DisplayName("예외 시나리오")
        class ExceptionTest {
            @Test
            @DisplayName("Repository save 실패 시 예외가 발생한다")
            void createMember_RepositorySaveFailure() {
                // given
                when(memberRepository.save(any(Member.class))).thenThrow(new RuntimeException("Database error"));

                // when & then
                assertThatThrownBy(() -> memberService.createMember(signUpRequest))
                        .isInstanceOf(RuntimeException.class)
                        .hasMessage("Database error");
            }

            @Test
            @DisplayName("중복된 이메일로 회원가입 시 MemberException이 발생한다")
            void createMember_DuplicatedEmail() {
                // given
                when(memberRepository.existsByEmail(signUpRequest.email())).thenReturn(true);

                // when & then
                assertThatThrownBy(() -> memberService.createMember(signUpRequest))
                        .isInstanceOf(MemberException.class)
                        .satisfies(exception -> {
                            MemberException memberException =
                                    (MemberException) exception;
                            assertThat(memberException.getErrorCode())
                                    .isEqualTo(MemberErrorCode.EMAIL_ALREADY_EXISTS);
                        });

                // verify
                verify(memberRepository, times(1)).existsByEmail(signUpRequest.email());
                verify(memberRepository, times(0)).save(any(Member.class)); // save는 호출되지
                // 않아야 함
            }

            @Test
            @DisplayName("이미 존재하는 이메일로 회원가입 시 예외 메시지가 올바르다")
            void createMember_DuplicatedEmail_CorrectMessage() {
                // given
                when(memberRepository.existsByEmail(signUpRequest.email()))
                        .thenReturn(true);

                // when & then
                assertThatThrownBy(() -> memberService.createMember(signUpRequest))
                        .isInstanceOf(MemberException.class)
                        .hasMessage("이미 존재하는 이메일입니다.");
            }

            @Test
            @DisplayName("서로 다른 이메일의 중복 검사가 독립적으로 작동한다")
            void createMember_DifferentEmailsValidation() {
                // given
                SignUpRequest request1 =
                        new SignUpRequest("김철수", "철수", "existing@example.com", "password1");
                SignUpRequest request2 =
                        new SignUpRequest("김영희", "영희", "new@example.com", "password2");

                // existing@example.com은 이미 존재, new@example.com은 존재하지 않음
                when(memberRepository.existsByEmail("existing@example.com")).thenReturn(true);
                when(memberRepository.existsByEmail("new@example.com")).thenReturn(false);
                when(memberRepository.save(any(Member.class))).thenReturn(savedMember);

                // when & then
                // 첫 번째 요청은 예외 발생
                assertThatThrownBy(() -> memberService.createMember(request1))
                        .isInstanceOf(MemberException.class)
                        .satisfies(exception -> {
                            MemberException memberException =
                                    (MemberException) exception;
                            assertThat(memberException.getErrorCode())
                                    .isEqualTo(MemberErrorCode.EMAIL_ALREADY_EXISTS);
                        });

                // 두 번째 요청은 성공
                SignUpResponse response = memberService.createMember(request2);
                assertThat(response).isNotNull();

                // verify
                verify(memberRepository, times(1)).existsByEmail("existing@example.com");
                verify(memberRepository, times(1)).existsByEmail("new@example.com");
                verify(memberRepository, times(1)).save(any(Member.class)); // 두 번째 요청에서만
                // save 호출
            }

            @Test
            @DisplayName("null 이메일로 회원가입 시도 시 existsByEmail이 호출된다")
            void createMember_NullEmail() {
                // given
                SignUpRequest nullEmailRequest =
                        new SignUpRequest("홍길동", "길동", null, "password123");
                when(memberRepository.existsByEmail(null)).thenReturn(false); // null에 대해서는
                // false 반환
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
                SignUpRequest emptyEmailRequest =
                        new SignUpRequest("홍길동", "길동", "", "password123");
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

    @Nested
    @DisplayName("회원 정보 조회 테스트")
    class GetMemberDetailInfoTest {

        @Nested
        @DisplayName("성공 시나리오")
        class SuccessTest {

            @Test
            @DisplayName("정상적인 memberId로 회원 정보를 조회할 수 있다")
            void getMemberDetailInfo_Success() {
                // given
                Long memberId = 1L;
                Member member = Member.builder().name("홍길동").nickname("길동이")
                        .email("hong@example.com")
                        .password("encrypted_password").build();

                when(memberRepository.findById(memberId))
                        .thenReturn(Optional.of(member));

                // when
                MemberDetailInfo result =
                        memberService.getMemberDetailInfo(memberId);

                // then
                assertThat(result).isNotNull();
                assertThat(result.name()).isEqualTo("홍길동");
                assertThat(result.nickname()).isEqualTo("길동이");
                assertThat(result.email()).isEqualTo("hong@example.com");

                verify(memberRepository, times(1)).findById(memberId);
            }

            @Test
            @DisplayName("다른 memberId로 회원 정보를 조회할 수 있다")
            void getMemberDetailInfo_DifferentMember() {
                // given
                Long memberId = 2L;
                Member member = Member.builder().name("김철수").nickname("철수짱")
                        .email("chulsoo@example.com")
                        .password("encrypted_password2").build();

                when(memberRepository.findById(memberId))
                        .thenReturn(Optional.of(member));

                // when
                MemberDetailInfo result =
                        memberService.getMemberDetailInfo(memberId);

                // then
                assertThat(result).isNotNull();
                assertThat(result.name()).isEqualTo("김철수");
                assertThat(result.nickname()).isEqualTo("철수짱");
                assertThat(result.email()).isEqualTo("chulsoo@example.com");

                verify(memberRepository, times(1)).findById(memberId);
            }
        }

        @Nested
        @DisplayName("예외 시나리오")
        class ExceptionTest {

            @Test
            @DisplayName("존재하지 않는 memberId로 조회 시 MEMBER_NOT_FOUND 예외가 발생한다")
            void getMemberDetailInfo_MemberNotFound() {
                // given
                Long nonExistentMemberId = 999L;
                when(memberRepository.findById(nonExistentMemberId))
                        .thenReturn(Optional.empty());

                // when & then
                assertThatThrownBy(() -> memberService
                        .getMemberDetailInfo(nonExistentMemberId))
                        .isInstanceOf(MemberException.class)
                        .satisfies(exception -> {
                            MemberException memberException =
                                    (MemberException) exception;
                            assertThat(memberException
                                    .getErrorCode()).isEqualTo(
                                    MemberErrorCode.MEMBER_NOT_FOUND);
                        });

                verify(memberRepository, times(1)).findById(nonExistentMemberId);
            }

            @Test
            @DisplayName("null memberId로 조회 시 예외가 발생한다")
            void getMemberDetailInfo_NullMemberId() {
                // given
                Long nullMemberId = null;
                when(memberRepository.findById(nullMemberId))
                        .thenReturn(Optional.empty());

                // when & then
                assertThatThrownBy(() -> memberService
                        .getMemberDetailInfo(nullMemberId))
                        .isInstanceOf(MemberException.class)
                        .satisfies(exception -> {
                            MemberException memberException =
                                    (MemberException) exception;
                            assertThat(memberException
                                    .getErrorCode()).isEqualTo(
                                    MemberErrorCode.MEMBER_NOT_FOUND);
                        });
            }
        }
    }

    @Nested
    @DisplayName("닉네임 변경 테스트")
    class UpdateNicknameTest {

        @Nested
        @DisplayName("성공 시나리오")
        class SuccessTest {

            @Test
            @DisplayName("정상적인 요청으로 닉네임을 변경할 수 있다")
            void updateNickname_Success() {
                // given
                Long memberId = 1L;
                String originalNickname = "기존닉네임";
                String newNickname = "새로운닉네임";

                Member member = Member.builder().name("홍길동")
                        .nickname(originalNickname)
                        .email("hong@example.com")
                        .password("encrypted_password").build();

                UpdateNicknameRequest request =
                        new UpdateNicknameRequest(newNickname);

                when(memberRepository.findById(memberId))
                        .thenReturn(Optional.of(member));

                // when
                memberService.updateNickname(memberId, request);

                // then
                assertThat(member.getNickname()).isEqualTo(newNickname);
                verify(memberRepository, times(1)).findById(memberId);
            }

            @Test
            @DisplayName("다른 회원의 닉네임을 변경할 수 있다")
            void updateNickname_DifferentMember() {
                // given
                Long memberId = 2L;
                String newNickname = "철수의새닉네임";

                Member member = Member.builder().name("김철수").nickname("철수")
                        .email("chulsoo@example.com")
                        .password("encrypted_password").build();

                UpdateNicknameRequest request =
                        new UpdateNicknameRequest(newNickname);

                when(memberRepository.findById(memberId))
                        .thenReturn(Optional.of(member));

                // when
                memberService.updateNickname(memberId, request);

                // then
                assertThat(member.getNickname()).isEqualTo(newNickname);
                verify(memberRepository, times(1)).findById(memberId);
            }

            @Test
            @DisplayName("여러 번 닉네임 변경이 정상적으로 작동한다")
            void updateNickname_MultipleUpdates() {
                // given
                Long memberId = 1L;

                Member member = Member.builder().name("홍길동").nickname("원래닉네임")
                        .email("hong@example.com")
                        .password("encrypted_password").build();

                UpdateNicknameRequest request1 = new UpdateNicknameRequest("첫번째변경");
                UpdateNicknameRequest request2 = new UpdateNicknameRequest("두번째변경");

                when(memberRepository.findById(memberId))
                        .thenReturn(Optional.of(member));

                // when
                memberService.updateNickname(memberId, request1);
                memberService.updateNickname(memberId, request2);

                // then
                assertThat(member.getNickname()).isEqualTo("두번째변경");
                verify(memberRepository, times(2)).findById(memberId);
            }
        }

        @Nested
        @DisplayName("예외 시나리오")
        class ExceptionTest {

            @Test
            @DisplayName("존재하지 않는 memberId로 닉네임 변경 시 MEMBER_NOT_FOUND 예외가 발생한다")
            void updateNickname_MemberNotFound() {
                // given
                Long nonExistentMemberId = 999L;
                UpdateNicknameRequest request = new UpdateNicknameRequest("새닉네임");

                when(memberRepository.findById(nonExistentMemberId))
                        .thenReturn(Optional.empty());

                // when & then
                assertThatThrownBy(() -> memberService
                        .updateNickname(nonExistentMemberId, request))
                        .isInstanceOf(MemberException.class)
                        .satisfies(exception -> {
                            MemberException memberException =
                                    (MemberException) exception;
                            assertThat(memberException
                                    .getErrorCode()).isEqualTo(
                                    MemberErrorCode.MEMBER_NOT_FOUND);
                        });

                verify(memberRepository, times(1)).findById(nonExistentMemberId);
            }

            @Test
            @DisplayName("null memberId로 닉네임 변경 시 예외가 발생한다")
            void updateNickname_NullMemberId() {
                // given
                Long nullMemberId = null;
                UpdateNicknameRequest request = new UpdateNicknameRequest("새닉네임");

                when(memberRepository.findById(nullMemberId))
                        .thenReturn(Optional.empty());

                // when & then
                assertThatThrownBy(() -> memberService.updateNickname(nullMemberId,
                        request)).isInstanceOf(MemberException.class)
                        .satisfies(exception -> {
                            MemberException memberException =
                                    (MemberException) exception;
                            assertThat(memberException
                                    .getErrorCode()).isEqualTo(
                                    MemberErrorCode.MEMBER_NOT_FOUND);
                        });
            }
        }
    }

    @Nested
    @DisplayName("비밀번호 변경 테스트")
    class UpdatePasswordTest {

        @Nested
        @DisplayName("성공 시나리오")
        class SuccessTest {

            @Test
            @DisplayName("정상적인 요청으로 비밀번호를 변경할 수 있다")
            void updatePassword_Success() {
                // given
                Long memberId = 1L;
                String originalPassword = "originalPassword123";
                String newPassword = "newPassword456";

                Member member = Member.builder().name("홍길동").nickname("길동이")
                        .email("hong@example.com")
                        .password(encryptor.encrypt(originalPassword))
                        .build();

                UpdatePasswordRequest request =
                        new UpdatePasswordRequest(newPassword);

                when(memberRepository.findById(memberId))
                        .thenReturn(Optional.of(member));

                // when
                memberService.updatePassword(memberId, request);

                // then
                assertThat(encryptor.isMatch(newPassword, member.getPassword()))
                        .isTrue();
                assertThat(encryptor.isMatch(originalPassword,
                        member.getPassword())).isFalse();
                verify(memberRepository, times(1)).findById(memberId);
            }

            @Test
            @DisplayName("다른 회원의 비밀번호를 변경할 수 있다")
            void updatePassword_DifferentMember() {
                // given
                Long memberId = 2L;
                String newPassword = "chulsooNewPassword789";

                Member member = Member.builder().name("김철수").nickname("철수")
                        .email("chulsoo@example.com")
                        .password(encryptor.encrypt("oldPassword")).build();

                UpdatePasswordRequest request =
                        new UpdatePasswordRequest(newPassword);

                when(memberRepository.findById(memberId))
                        .thenReturn(Optional.of(member));

                // when
                memberService.updatePassword(memberId, request);

                // then
                assertThat(encryptor.isMatch(newPassword, member.getPassword()))
                        .isTrue();
                verify(memberRepository, times(1)).findById(memberId);
            }

            @Test
            @DisplayName("비밀번호가 올바르게 암호화되어 저장된다")
            void updatePassword_PasswordEncryption() {
                // given
                Long memberId = 1L;
                String newPassword = "encryptionTest123";

                Member member = Member.builder().name("홍길동").nickname("길동이")
                        .email("hong@example.com")
                        .password("oldEncryptedPassword").build();

                UpdatePasswordRequest request =
                        new UpdatePasswordRequest(newPassword);

                when(memberRepository.findById(memberId))
                        .thenReturn(Optional.of(member));

                // when
                memberService.updatePassword(memberId, request);

                // then
                // 평문 비밀번호가 그대로 저장되지 않았는지 확인
                assertThat(member.getPassword()).isNotEqualTo(newPassword);
                // 암호화된 비밀번호로 검증이 가능한지 확인
                assertThat(encryptor.isMatch(newPassword, member.getPassword()))
                        .isTrue();
            }

            @Test
            @DisplayName("여러 번 비밀번호 변경이 정상적으로 작동한다")
            void updatePassword_MultipleUpdates() {
                // given
                Long memberId = 1L;

                Member member = Member.builder().name("홍길동").nickname("길동이")
                        .email("hong@example.com")
                        .password(encryptor.encrypt("originalPassword"))
                        .build();

                UpdatePasswordRequest request1 =
                        new UpdatePasswordRequest("firstChange123");
                UpdatePasswordRequest request2 =
                        new UpdatePasswordRequest("secondChange456");

                when(memberRepository.findById(memberId))
                        .thenReturn(Optional.of(member));

                // when
                memberService.updatePassword(memberId, request1);
                memberService.updatePassword(memberId, request2);

                // then
                assertThat(encryptor.isMatch("secondChange456",
                        member.getPassword())).isTrue();
                assertThat(encryptor.isMatch("firstChange123",
                        member.getPassword())).isFalse();
                assertThat(encryptor.isMatch("originalPassword",
                        member.getPassword())).isFalse();
                verify(memberRepository, times(2)).findById(memberId);
            }

            @Test
            @DisplayName("같은 비밀번호로 변경해도 다른 해시값이 생성된다")
            void updatePassword_SamePasswordDifferentHash() {
                // given
                Long memberId = 1L;
                String samePassword = "samePassword123";

                Member member = Member.builder().name("홍길동").nickname("길동이")
                        .email("hong@example.com")
                        .password(encryptor.encrypt("oldPassword")).build();

                UpdatePasswordRequest request =
                        new UpdatePasswordRequest(samePassword);

                when(memberRepository.findById(memberId))
                        .thenReturn(Optional.of(member));

                // when
                String firstHash = encryptor.encrypt(samePassword);
                memberService.updatePassword(memberId, request);
                String secondHash = member.getPassword();

                // then
                assertThat(encryptor.isMatch(samePassword, firstHash)).isTrue();
                assertThat(encryptor.isMatch(samePassword, secondHash)).isTrue();
                // BCrypt의 salt 특성으로 인해 같은 비밀번호라도 해시값이 다름
                assertThat(firstHash).isNotEqualTo(secondHash);
            }
        }

        @Nested
        @DisplayName("예외 시나리오")
        class ExceptionTest {

            @Test
            @DisplayName("존재하지 않는 memberId로 비밀번호 변경 시 MEMBER_NOT_FOUND 예외가 발생한다")
            void updatePassword_MemberNotFound() {
                // given
                Long nonExistentMemberId = 999L;
                UpdatePasswordRequest request =
                        new UpdatePasswordRequest("newPassword123");

                when(memberRepository.findById(nonExistentMemberId))
                        .thenReturn(Optional.empty());

                // when & then
                assertThatThrownBy(() -> memberService
                        .updatePassword(nonExistentMemberId, request))
                        .isInstanceOf(MemberException.class)
                        .satisfies(exception -> {
                            MemberException memberException =
                                    (MemberException) exception;
                            assertThat(memberException
                                    .getErrorCode()).isEqualTo(
                                    MemberErrorCode.MEMBER_NOT_FOUND);
                        });

                verify(memberRepository, times(1)).findById(nonExistentMemberId);
            }

            @Test
            @DisplayName("null memberId로 비밀번호 변경 시 예외가 발생한다")
            void updatePassword_NullMemberId() {
                // given
                Long nullMemberId = null;
                UpdatePasswordRequest request =
                        new UpdatePasswordRequest("newPassword123");

                when(memberRepository.findById(nullMemberId))
                        .thenReturn(Optional.empty());

                // when & then
                assertThatThrownBy(() -> memberService.updatePassword(nullMemberId,
                        request)).isInstanceOf(MemberException.class)
                        .satisfies(exception -> {
                            MemberException memberException =
                                    (MemberException) exception;
                            assertThat(memberException
                                    .getErrorCode()).isEqualTo(
                                    MemberErrorCode.MEMBER_NOT_FOUND);
                        });
            }
        }
    }
}
