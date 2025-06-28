package git_kkalnane.backend.starbucks.item.common.success;

import git_kkalnane.backend.starbucks._global.success.SuccessCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * 아이템(Item) 관련 API 요청 처리 성공을 나타내는 열거형입니다.
 * 각 성공 코드마다 HTTP 상태 코드와 메시지를 포함합니다.
 *
 * @author Seongjun In
 * @version 1.0
 */
@RequiredArgsConstructor
public enum ItemSuccessCode implements SuccessCode {

    /**
     * 음료 목록을 성공적으로 조회했을 때 사용됩니다.
     */
    DRINKS_LIST_RETRIEVED(HttpStatus.OK, "음료 목록 조회 성공"),
    /**
     * 디저트 목록을 성공적으로 조회했을 때 사용됩니다.
     */
    DESSERT_LIST_RETRIEVED(HttpStatus.OK, "디저트 목록 조회 성공"),

    /**
     * 음료 상세 정보를 성공적으로 조회했을 때 사용됩니다.
     */
    DRINK_DETAIL_RETRIEVED(HttpStatus.OK, "음료 상세 정보 조회 성공");

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
