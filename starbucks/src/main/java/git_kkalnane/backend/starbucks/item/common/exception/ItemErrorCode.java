package git_kkalnane.backend.starbucks.item.common.exception;

import git_kkalnane.backend.starbucks._global.error.core.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ItemErrorCode implements ErrorCode {

    BEVERAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 음료입니다."),
    DESSERT_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 디저트입니다."),
    MENU_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 메뉴입니다.");

    private final HttpStatus status;
    private final String message;

    ItemErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }
}
