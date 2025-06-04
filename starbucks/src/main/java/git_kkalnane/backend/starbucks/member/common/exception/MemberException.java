package git_kkalnane.backend.starbucks.member.common.exception;

import git_kkalnane.backend.starbucks.global.error.core.BaseException;

public class MemberException extends BaseException {
    public MemberException(MemberErrorCode errorCode) {
        super(errorCode);
    }
}
