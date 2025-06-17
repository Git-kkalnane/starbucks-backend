package git_kkalnane.backend.starbucks.store.common.success;

import git_kkalnane.backend.starbucks.global.success.SuccessCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum StoreSuccessCode implements SuccessCode {

    STORE_DETAIL_RETRIEVED(HttpStatus.OK, "지점 상세 정보 조회 성공"),
    CROWD_LEVEL_UPDATED(HttpStatus.OK, "지점 혼잡도 변경 성공");

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
