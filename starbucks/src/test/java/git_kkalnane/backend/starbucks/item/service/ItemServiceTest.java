package git_kkalnane.backend.starbucks.item.service;

import git_kkalnane.backend.starbucks.item.common.exception.ItemException;
import git_kkalnane.backend.starbucks.item.domain.ItemStatus;
import git_kkalnane.backend.starbucks.item.domain.ItemType;
import git_kkalnane.backend.starbucks.item.domain.beverage.enums.BeverageShotOption;
import git_kkalnane.backend.starbucks.item.domain.beverage.BeverageItem;
import git_kkalnane.backend.starbucks.item.domain.beverage.enums.BeverageSizeOption;
import git_kkalnane.backend.starbucks.item.domain.beverage.enums.BeverageTemperatureOption;
import git_kkalnane.backend.starbucks.item.domain.dessert.DessertItem;
import git_kkalnane.backend.starbucks.item.dto.response.ItemDetailResponse;
import git_kkalnane.backend.starbucks.item.dto.response.ItemListResponse;
import git_kkalnane.backend.starbucks.item.repository.BeverageItemRepository;
import git_kkalnane.backend.starbucks.item.repository.DessertItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.ArrayList;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ItemService 테스트")
class ItemServiceTest {

    @Mock
    private BeverageItemRepository beverageItemRepository;

    @Mock
    private DessertItemRepository dessertItemRepository;

    @InjectMocks
    private ItemService itemService;

    private BeverageItem mockCoffee1, mockBeverage1;
    private DessertItem mockDessert1, mockDessert2;
    
    private final Long EXISTING_BEVERAGE_ID = 1L;
    private final Long NOT_EXISTING_BEVERAGE_ID = 99999L;
    private final String NOT_FOUND_MESSAGE = "404 NOT_FOUND \"해당하는 음료를 찾을 수 없습니다. ID: ";

    private BeverageItem createMockBeverageItem(long id, String name, ItemType type) {
        return BeverageItem.builder()
                .id(id)
                .beverageItemNameKo(name)
                .beverageItemNameEn(name)
                .description("테스트 음료 설명")
                .price(4500)
                .isCoffee(type == ItemType.COFFEE)
                .hotImageUrl("https://test.com/hot.jpg")
                .iceImageUrl("https://test.com/ice.jpg")
                .category(type)
                .status(ItemStatus.AVAILABLE)
                .shotOption(BeverageShotOption.SHOT)
                .supportedSizes(Set.of(BeverageSizeOption.TALL, BeverageSizeOption.GRANDE))
                .supportedTemperatures(Set.of(BeverageTemperatureOption.ICE, BeverageTemperatureOption.HOT))
                .beverageItemCategories(new ArrayList<>())
                .build();
    }

    @BeforeEach
    void setUp() {
        mockCoffee1 = createMockBeverageItem(1L, "아메리카노", ItemType.COFFEE);
        mockBeverage1 = createMockBeverageItem(2L, "자바 칩 프라푸치노", ItemType.BEVERAGE);
        mockDessert1 = DessertItem.builder().id(10L).dessertItemNameKo("딸기 케이크").build();
        mockDessert2 = DessertItem.builder().id(11L).dessertItemNameKo("블루베리 베이글").build();
    }

    @Test
    @DisplayName("getDrinkItems 호출 시, 모든 음료 목록을 페이지네이션하여 성공적으로 반환한다")
    void getBeverageItems_Success() {
        // Given
        Pageable pageable = PageRequest.of(0, 5);
        List<BeverageItem> allBeverages = Arrays.asList(mockCoffee1, mockBeverage1);
        Page<BeverageItem> beveragePage = new PageImpl<>(allBeverages, pageable, allBeverages.size());

        given(beverageItemRepository.findAll(any(Pageable.class))).willReturn(beveragePage);
        // When
        ItemListResponse result = itemService.getDrinkItems(pageable);

        // Then
        verify(beverageItemRepository, times(1)).findAll(any(Pageable.class));
        verify(dessertItemRepository, never()).findAll();

        assertThat(result).isNotNull();
        assertThat(result.getTotalCount()).isEqualTo(allBeverages.size());
        assertThat(result.getItems()).hasSize(allBeverages.size());
        assertThat(result.getItems()).allMatch(item -> item.getType() == ItemType.COFFEE || item.getType() == ItemType.BEVERAGE);
    }

    @Test
    @DisplayName("getDessertItems 호출 시, 모든 디저트 목록을 페이지네이션하여 성공적으로 반환한다")
    void getDessertItems_Success() {
        // Given
        Pageable pageable = PageRequest.of(0, 5);
        List<DessertItem> allDesserts = Arrays.asList(mockDessert1, mockDessert2);
        Page<DessertItem> dessertPage = new PageImpl<>(allDesserts, pageable, allDesserts.size());
        given(dessertItemRepository.findAll(any(Pageable.class))).willReturn(dessertPage);

        // When
        ItemListResponse result = itemService.getDessertItems(pageable);

        // Then
        verify(dessertItemRepository, times(1)).findAll(any(Pageable.class));
        verify(beverageItemRepository, never()).findAll(); // 음료 리포지토리는 호출되지 않아야 함

        assertThat(result).isNotNull();
        assertThat(result.getTotalCount()).isEqualTo(allDesserts.size());
        assertThat(result.getItems()).hasSize(allDesserts.size());
        assertThat(result.getItems()).allMatch(item -> item.getType() == ItemType.DESSERT);
    }

    @Test
    @DisplayName("음료 아이템이 하나도 없을 때, getDrinkItems가 빈 목록을 반환한다")
    void getDrinkItems_WhenNoDrinksExist_ReturnsEmptyResponse() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<BeverageItem> emptyPage = Page.empty(pageable);
        given(beverageItemRepository.findAll(any(Pageable.class))).willReturn(emptyPage);

        // When
        ItemListResponse result = itemService.getDrinkItems(pageable);

        // Then
        verify(beverageItemRepository, times(1)).findAll(any(Pageable.class));

        assertThat(result).isNotNull();
        assertThat(result.getTotalCount()).isEqualTo(0);
        assertThat(result.getItems()).isEmpty();
    }
    
    @Nested
    @DisplayName("getBeverageDetail() 테스트")
    class GetDrinkDetailTest {
        @Test
        @DisplayName("음료 상세 조회 성공")
        void getBeverageDetail_success() {
            // given
            BeverageItem mockItem = createMockBeverageItem(EXISTING_BEVERAGE_ID, "아이스 카페 아메리카노", ItemType.COFFEE);
            given(beverageItemRepository.findByIdWithDetails(EXISTING_BEVERAGE_ID))
                .willReturn(Optional.of(mockItem));

            // when
            ItemDetailResponse response = itemService.getBeverageDetail(EXISTING_BEVERAGE_ID);

            // then
            assertThat(response).isNotNull();
            assertThat(response.id()).isEqualTo(EXISTING_BEVERAGE_ID);
            assertThat(response.nameKo()).isEqualTo("아이스 카페 아메리카노");
            assertThat(response.nameEn()).isEqualTo("아이스 카페 아메리카노");
            assertThat(response.description()).isEqualTo("테스트 음료 설명");
            assertThat(response.price()).isEqualTo(4500);
            assertThat(response.isCoffee()).isTrue();
            assertThat(response.supportedSizes())
                .hasSize(2)
                .containsExactlyInAnyOrder(
                    BeverageSizeOption.TALL, 
                    BeverageSizeOption.GRANDE
                );
            assertThat(response.supportedTemperatures())
                .hasSize(2)
                .containsExactlyInAnyOrder(
                    BeverageTemperatureOption.HOT,
                    BeverageTemperatureOption.ICE
                );
            verify(beverageItemRepository, atLeastOnce()).findByIdWithDetails(EXISTING_BEVERAGE_ID);
        }

        @Test
        @DisplayName("음료 상세 조회 실패 - 존재하지 않는 ID")
        void getBeverageDetail_fail_notFound() {
            // given
            given(beverageItemRepository.findByIdWithDetails(NOT_EXISTING_BEVERAGE_ID))
                .willReturn(Optional.empty());

            // when & then
            // 기대하는 예외를 ItemException으로 수정
            assertThatThrownBy(() -> itemService.getBeverageDetail(NOT_EXISTING_BEVERAGE_ID))
                    .isInstanceOf(ItemException.class) // ResponseStatusException -> ItemException
                    .hasMessageContaining("존재하지 않는 음료입니다."); // 메시지 검증도 구체적으로 변경

            verify(beverageItemRepository, times(1)).findByIdWithDetails(NOT_EXISTING_BEVERAGE_ID);
        }

        @Test
        @DisplayName("음료 상세 조회 실패 - ID가 null인 경우")
        void getBeverageDetail_fail_nullId() {
            // given
            Long nullId = null;

            // when & then
            // 기대하는 예외를 ItemException으로 수정
            assertThatThrownBy(() -> itemService.getBeverageDetail(nullId))
                    .isInstanceOf(ItemException.class); // ResponseStatusException -> ItemException

            verify(beverageItemRepository, never()).findByIdWithDetails(anyLong());
        }

        @Test
        @DisplayName("음료 상세 조회 실패 - ID가 0 이하인 경우")
        void getBeverageDetail_fail_invalidId() {
            // given
            Long invalidId = 0L;
            given(beverageItemRepository.findByIdWithDetails(invalidId))
                    .willReturn(Optional.empty());

            // when & then
            // 기대하는 예외를 ItemException으로 수정
            assertThatThrownBy(() -> itemService.getBeverageDetail(invalidId))
                    .isInstanceOf(ItemException.class) // ResponseStatusException -> ItemException
                    .hasMessageContaining("존재하지 않는 음료입니다.");

            verify(beverageItemRepository, times(1)).findByIdWithDetails(invalidId);

            // Also test with negative ID
            Long negativeId = -1L;
            given(beverageItemRepository.findByIdWithDetails(negativeId))
                    .willReturn(Optional.empty());

            assertThatThrownBy(() -> itemService.getBeverageDetail(negativeId))
                    .isInstanceOf(ItemException.class) // ResponseStatusException -> ItemException
                    .hasMessageContaining("존재하지 않는 음료입니다.");

            verify(beverageItemRepository, times(1)).findByIdWithDetails(negativeId);
        }
    }
}