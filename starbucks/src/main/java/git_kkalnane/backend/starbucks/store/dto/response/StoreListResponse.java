package git_kkalnane.backend.starbucks.store.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * 지점 목록 조회 API의 최종 응답을 위한 DTO.
 * 페이지네이션 정보와 실제 지점 목록 데이터를 포함합니다.
 *
 * @author Seongjun In
 * @version 1.0
 */
@Getter
@Builder
public class StoreListResponse {

    private final List<StoreSummaryResponse> stores;
    private final long totalCount;
    private final int currentPage;
    private final int totalPages;
    private final int pageSize;
}
