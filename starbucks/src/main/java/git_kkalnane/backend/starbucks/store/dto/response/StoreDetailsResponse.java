package git_kkalnane.backend.starbucks.store.dto.response;

import git_kkalnane.backend.starbucks.store.domain.CrowdLevel;
import git_kkalnane.backend.starbucks.store.domain.Store;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
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
