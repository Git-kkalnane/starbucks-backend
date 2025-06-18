package git_kkalnane.backend.starbucks.store.common.exception;

import git_kkalnane.backend.starbucks.global.error.core.BaseException;

/**
 * 매장(Store) 관련 비즈니스 로직 처리 중 발생하는 예외를 나타냅니다.
 * {@link StoreErrorCode}를 사용하여 구체적인 오류 정보를 전달합니다.
 *
 * @author Seongjun In
 * @version 1.0
 */
public class StoreException extends BaseException {

    /**
     * 지정된 {@link StoreErrorCode}와 함께 {@code StoreException}의 새 인스턴스를 생성합니다.
     *
     * @param errorCode 이 예외와 관련된 매장 오류 코드
     */
    public StoreException(StoreErrorCode errorCode) {
        super(errorCode);
    }
}
