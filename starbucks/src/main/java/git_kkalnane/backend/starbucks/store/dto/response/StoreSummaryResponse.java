package git_kkalnane.backend.starbucks.store.dto.response;

import git_kkalnane.backend.starbucks.store.domain.Store;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 지점 목록 조회 시 반환될 각 지점의 요약 정보를 담는 DTO
 *
 * @author Seongjun In
 * @version 1.0
 */
@Getter
@RequiredArgsConstructor
public class StoreSummaryResponse {
   private final String name;
   private final String address;
   private final String imageUrl;

    /**
     * Store 엔티티를 StoreSummaryResponse DTO로 변환합니다.
     * @param store 원본 Store 엔티티
     * @return 변환된 StoreSummaryResponse DTO
     */
    public static StoreSummaryResponse from(Store store) {
        return new StoreSummaryResponse(
                store.getName(),
                store.getAddress(),
                store.getImageUrl()
        );
    }
}
