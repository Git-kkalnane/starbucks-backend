package git_kkalnane.backend.starbucks.store.common.exception;

import git_kkalnane.backend.starbucks.global.error.core.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum StoreErrorCode implements ErrorCode {

    STORE_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 ID의 지점을 찾을 수 없습니다."),
    INVALID_CROWD_LEVEL(HttpStatus.BAD_REQUEST, "올바르지 않은 혼잡도 요청입니다.");

    private final HttpStatus status;
    private final String message;

    @Override
    public HttpStatus getStatus() {
        return status;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
