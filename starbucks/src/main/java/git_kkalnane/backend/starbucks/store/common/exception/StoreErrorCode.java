package git_kkalnane.backend.starbucks.store.common.exception;

import git_kkalnane.backend.starbucks.global.error.core.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * 매장(Store) 관련 비즈니스 로직 처리 중 발생하는 오류 유형을 정의하는 열거형입니다.
 * 각 오류 코드마다 HTTP 상태 코드와 사용자에게 전달할 메시지를 포함합니다.
 *
 * @author Seongjun In
 * @version 1.0
 */
@RequiredArgsConstructor
public enum StoreErrorCode implements ErrorCode {

    /**
     * 요청된 ID에 해당하는 매장을 찾을 수 없을 때 사용됩니다. (HTTP 404 Not Found)
     */

    STORE_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 ID의 지점을 찾을 수 없습니다.");

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
