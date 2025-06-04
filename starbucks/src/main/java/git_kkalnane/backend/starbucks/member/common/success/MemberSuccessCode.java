package git_kkalnane.backend.starbucks.member.common.success;

import git_kkalnane.backend.starbucks.global.success.SuccessCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum MemberSuccessCode implements SuccessCode {
    MEMBER_FETCH_COMPLETE(HttpStatus.OK,"멤버 프로필 조회에 성공했습니다."),
    MEMBER_UPDATE_COMPLETE(HttpStatus.OK,"프로필이 성공적으로 업데이트되었습니다."),
    MEMBER_DELETE_COMPLETE(HttpStatus.OK,"회원 탈퇴에 성공하였습니다."),
    ;


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
