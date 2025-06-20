package git_kkalnane.backend.starbucks.store.service;

import git_kkalnane.backend.starbucks.store.domain.CrowdLevel;
import git_kkalnane.backend.starbucks.store.domain.Store;
import git_kkalnane.backend.starbucks.store.dto.response.StoreDetailsResponse;
import git_kkalnane.backend.starbucks.store.repository.StoreRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class StoreServiceTest {

    @Mock
    private StoreRepository storeRepository;
    @InjectMocks
    private StoreService storeService;

    @Test
    @DisplayName("유효한 ID 매장 상세 정보 조회 시 성공")
    void getStoreDetails() {
        // Given
        Long Id = 1L;
        Store mockStore = Store.builder()
                .id(Id)
                .name("테스트 스타벅스")
                .address("테스트 주소")
                .phone("123-4567-8901")
                .openingHours("09:00-22:00")
                .hasDriveThrough(false)
                .seatingCapacity(50)
                .latitude(37.0)
                .longitude(127.0)
                .imageUrl("http://test.com/image.jpg")
                .currentCrowdLevel(CrowdLevel.LOW)
                .build();

        given(storeRepository.findById(Id)).willReturn(Optional.of(mockStore));

        // When
        StoreDetailsResponse response = storeService.getStoreDetails(Id);

        // Then
        verify(storeRepository).findById(Id);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(Id);
        assertThat(response.getName()).isEqualTo("테스트 스타벅스");
        assertThat(response.getAddress()).isEqualTo("테스트 주소");
        assertThat(response.getCurrentCrowdLevel()).isEqualTo(CrowdLevel.LOW);
    }
    }
