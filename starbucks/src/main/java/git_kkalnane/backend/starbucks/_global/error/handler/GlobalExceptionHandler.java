package git_kkalnane.backend.starbucks._global.error.handler;

import git_kkalnane.backend.starbucks._global.error.core.BaseException;
import git_kkalnane.backend.starbucks._global.error.core.ErrorCode;
import git_kkalnane.backend.starbucks._global.error.core.ErrorResponse;
import git_kkalnane.backend.starbucks._global.error.core.ValidationErrorResponse;
import git_kkalnane.backend.starbucks._global.error.core.ValidationErrors;
import git_kkalnane.backend.starbucks._global.utils.GlobalLogger;
import git_kkalnane.backend.starbucks.store.common.exception.StoreErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
        ErrorCode errorCode;
        if ("storeId".equals(e.getName()) && Long.class.equals(e.getRequiredType())) {
            errorCode = StoreErrorCode.INVALID_STORE_ID_FORMAT;
        } else {
            errorCode = GlobalErrorCode.INVALID_PATH_VARIABLE_FORMAT;
        }
        return getErrorResponse(e, errorCode);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {

        ValidationErrors validationErrors = new ValidationErrors();
        e.getBindingResult().getFieldErrors().forEach(error ->
                validationErrors.addError(error.getField(), error.getDefaultMessage()));

        return getErrorResponse(e, new ValidationErrorResponse(validationErrors));
    }

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ErrorResponse> handleBaseException(BaseException e) {
        return getErrorResponse(e,e.getErrorCode());
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException e) {
        return getErrorResponse(e,GlobalErrorCode.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        return getErrorResponse(e,GlobalErrorCode.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ErrorResponse> handleMissingRequestHeaderException(MissingRequestHeaderException e){
        return getErrorResponse(e,GlobalErrorCode.MISSING_HEADER);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResourceException(NoResourceFoundException e) {
        return getErrorResponse(e,GlobalErrorCode.RESOURCE_NOT_FOUND);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse>
    handleMethodNotAllowedException(HttpRequestMethodNotSupportedException e) {
        return getErrorResponse(e,GlobalErrorCode.METHOD_NOT_ALLOWED);
    }

    private static ResponseEntity<ErrorResponse> getErrorResponse(Exception e, GlobalErrorCode errorCode) {
        GlobalLogger.error(e.toString());

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ErrorResponse.of(errorCode));
    }

    private static ResponseEntity<ErrorResponse> getErrorResponse(Exception e, ErrorCode errorCode) {
        GlobalLogger.error(e.toString());

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ErrorResponse.of(errorCode, e.getMessage()));
    }

    private static ResponseEntity<ValidationErrorResponse> getErrorResponse(Exception e, ValidationErrorResponse response) {
        GlobalLogger.error(e.toString());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }
}
