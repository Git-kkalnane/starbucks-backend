package git_kkalnane.backend.starbucks.store.dto.response;

import git_kkalnane.backend.starbucks.store.domain.CrowdLevel;
import git_kkalnane.backend.starbucks.store.domain.Store;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 클라이언트에게 반환될 매장의 상세 정보를 담는 DTO
 *
 * @author Seongjun In
 * @version 1.0
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class StoreDetailsResponse {
    private Long id;
    private String name;
    private String address;
    private String phone;
    private String openingHours;
    private boolean hasDriveThrough;
    private int seatingCapacity;
    private Double latitude;
    private Double longitude;
    private String imageUrl;
    private CrowdLevel currentCrowdLevel;

    /**
     * Store 엔티티를 StoreDetailsResponse DTO로 변환합니다.
     *
     * @param store 원본 Store 엔티티
     * @return 변환된 StoreDetailsResponse DTO
     */
    public static StoreDetailsResponse from(Store store) {
        return new StoreDetailsResponse(
                store.getId(),
                store.getName(),
                store.getAddress(),
                store.getPhone(),
                store.getOpeningHours(),
                store.isHasDriveThrough(),
                store.getSeatingCapacity(),
                store.getLatitude(),
                store.getLongitude(),
                store.getImageUrl(),
                store.getCurrentCrowdLevel()
        );
    }
}
