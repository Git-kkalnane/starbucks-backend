package git_kkalnane.backend.starbucks.member.common.success;

import git_kkalnane.backend.starbucks._global.success.SuccessCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum MemberSuccessCode implements SuccessCode {
    SIGN_UP_COMPLETED(HttpStatus.CREATED, "회원가입이 성공적으로 완료 되었습니다."),
    GET_MEMBER_DETAIL_INFO_COMPLETE(HttpStatus.OK,"멤버 프로필 조회에 성공했습니다."),
    MEMBER_NICKNAME_UPDATE_COMPLETE(HttpStatus.OK,"닉네임이 성공적으로 업데이트되었습니다."),
    MEMBER_PASSWORD_UPDATE_COMPLETE(HttpStatus.OK, "비밀번호가 성공적으로 업데이트 되었습니다."),
    MEMBER_DELETE_COMPLETE(HttpStatus.OK,"회원 탈퇴에 성공하였습니다.");

    private final HttpStatus status;
    private final String rawMessage;

    @Override
    public HttpStatus getStatus() {
        return status;
    }

    @Override
    public String getMessage() {
        return  rawMessage;
    }
}
