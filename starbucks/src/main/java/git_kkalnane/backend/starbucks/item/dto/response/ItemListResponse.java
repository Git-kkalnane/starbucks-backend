package git_kkalnane.backend.starbucks.item.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * 아이템 목록 전체를 담아 반환하는 DTO입니다.
 * 전체 아이템 리스트와 총 개수 정보를 포함합니다.
 *
 * @author Seongjun In
 * @version 1.0
 */
@Getter
@Builder
public class ItemListResponse {
    /**
     * 조회된 아이템 요약 정보 리스트
     */
    private List<ItemSummaryResponse> items;
    /**
     * 전체 아이템의 총 개수
     */
    private long totalCount;
}
