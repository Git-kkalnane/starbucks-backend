package git_kkalnane.backend.starbucks.cart.common.exception;

import git_kkalnane.backend.starbucks._global.error.core.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;
@Getter
public enum CartErrorCode implements ErrorCode {

    CART_ERROR_CODE(HttpStatus.BAD_REQUEST, "카트에러"),
    CART_NOT_FOUND(HttpStatus.NOT_FOUND, "카트가 존재하지 않습니다.");

    private final HttpStatus status;
    private final String message;

    CartErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }
}
