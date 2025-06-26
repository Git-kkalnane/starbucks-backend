package git_kkalnane.backend.starbucks._global.error.core;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ValidationErrorResponse {

    private final int status;
    private final String message;
    private final ValidationErrors errors;

    public ValidationErrorResponse(ValidationErrors errors) {
        this.status = HttpStatus.BAD_REQUEST.value();
        this.message = "유효성 검사 실패";
        this.errors = errors;
    }
}
