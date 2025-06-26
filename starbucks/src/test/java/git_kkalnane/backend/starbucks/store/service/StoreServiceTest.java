package git_kkalnane.backend.starbucks.store.service;

import git_kkalnane.backend.starbucks.store.common.exception.StoreException;
import git_kkalnane.backend.starbucks.store.domain.CrowdLevel;
import git_kkalnane.backend.starbucks.store.domain.Store;
import git_kkalnane.backend.starbucks.store.dto.response.StoreDetailsResponse;
import git_kkalnane.backend.starbucks.store.dto.response.StoreListResponse;
import git_kkalnane.backend.starbucks.store.repository.StoreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat; // AssertJ 임포트 통일
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class StoreServiceTest {

    @Mock
    private StoreRepository storeRepository;
    @InjectMocks
    private StoreService storeService;

    private Store mockStore1;
    private Store mockStore2;

    @BeforeEach
    void setUp() {
        mockStore1 = Store.builder()
                .id(1L)
                .name("강남역점")
                .address("서울 강남구")
                .phone("02-1234-5678")
                .openingHours("07:00-22:00")
                .hasDriveThrough(true)
                .seatingCapacity(100)
                .latitude(37.4979)
                .longitude(127.0276)
                .imageUrl("/images/stores/gangnam.jpg")
                .currentCrowdLevel(CrowdLevel.MEDIUM)
                .build();

        mockStore2 = Store.builder()
                .id(2L)
                .name("홍대입구역점")
                .address("서울 마포구")
                .imageUrl("/images/stores/hongdae.jpg")
                .build();
    }


    @Test
    @DisplayName("유효한 ID로 지점 상세 정보 조회 시 성공한다")
    void getStoreDetails_Success() {
        // Given
        Long storeId = 1L;
        given(storeRepository.findById(storeId)).willReturn(Optional.of(mockStore1));

        // When
        StoreDetailsResponse response = storeService.getStoreDetails(storeId);

        // Then
        verify(storeRepository).findById(storeId);
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(storeId);
        assertThat(response.getName()).isEqualTo("강남역점");
        assertThat(response.getCurrentCrowdLevel()).isEqualTo(CrowdLevel.MEDIUM);
    }


    @Test
    @DisplayName("지점 목록 조회 시, Pageable을 사용하여 페이징된 결과를 올바르게 반환한다")
    void getStoreList_Success() {
        // Given
        int page = 0;
        int size = 15;
        Pageable pageable = PageRequest.of(page, size, Sort.by("name"));

        List<Store> storeList = Arrays.asList(mockStore1, mockStore2);
        Page<Store> storePage = new PageImpl<>(storeList, pageable, storeList.size());

        given(storeRepository.findAll(any(Pageable.class))).willReturn(storePage);

        // When
        StoreListResponse result = storeService.getStoreList(pageable);

        // Then
        verify(storeRepository, times(1)).findAll(any(Pageable.class));
        assertThat(result).isNotNull();
        assertThat(result.getStores()).hasSize(2);
        assertThat(result.getCurrentPage()).isEqualTo(page);
        assertThat(result.getTotalCount()).isEqualTo(storeList.size());
        assertThat(result.getStores().get(0).getName()).isEqualTo("강남역점");
        assertThat(result.getStores().get(0).getAddress()).isEqualTo("서울 강남구");
    }

    @Test
    @DisplayName("존재하지 않는 ID로 지점 상세 조회 시, StoreException 예외가 발생한다")
    void getStoreDetails_Failure_NotFound() {
        // Given
        Long nonExistentId = 999L;
        given(storeRepository.findById(nonExistentId)).willReturn(Optional.empty());

        // When
        assertThrows(StoreException.class, () -> {
            storeService.getStoreDetails(nonExistentId);
        });

        // Then
        verify(storeRepository).findById(nonExistentId);
    }
}