package git_kkalnane.backend.starbucks.store.common.success;

import git_kkalnane.backend.starbucks._global.success.SuccessCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * 매장(Store) 관련 API 요청 처리 성공을 나타내는 열거형입니다.
 * 각 성공 코드마다 HTTP 상태 코드와 메시지를 포함합니다.
 *
 * @author Seongjun In
 * @version 1.0
 */
@RequiredArgsConstructor
public enum StoreSuccessCode implements SuccessCode {

    /**
     * 지점 상세 정보를 성공적으로 조회했을 때 사용됩니다. (HTTP 200 OK)
     */
    STORE_DETAIL_RETRIEVED(HttpStatus.OK, "지점 상세 정보 조회 성공"),

    /**
     * 지점 목록 정보를 성공적으로 조회했을 때 사용됩니다. (HTTP 200 OK)
     */
    STORE_LIST_RETRIEVED(HttpStatus.OK, "지점 목록 조회 성공");

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
