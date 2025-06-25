package git_kkalnane.backend.starbucks.cart.common.success;

import git_kkalnane.backend.starbucks._global.success.SuccessCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum CartSuccessCode implements SuccessCode {

    CART_SUCCESS_CODE(HttpStatus.OK, "카트에 성공적으로 추가되었습니다."),
    CART_SUCCESS_MODIFIED(HttpStatus.OK, "카트가 성공적으로 수정되었습니다."),
    CART_SUCCESS_DELETED(HttpStatus.OK, "아이템이 성공적으로 삭제되었습니다.");

    private final HttpStatus status;
    private final String message;

    CartSuccessCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }
}
