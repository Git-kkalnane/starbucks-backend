package git_kkalnane.backend.starbucks.item.service;

import git_kkalnane.backend.starbucks.item.domain.BeverageItem;
import git_kkalnane.backend.starbucks.item.domain.DessertItem;
import git_kkalnane.backend.starbucks.item.domain.ItemType;
import git_kkalnane.backend.starbucks.item.dto.response.ItemListResponse;
import git_kkalnane.backend.starbucks.item.repository.BeverageItemRepository;
import git_kkalnane.backend.starbucks.item.repository.DessertItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ItemServiceTest {

    @Mock
    private BeverageItemRepository beverageItemRepository;

    @Mock
    private DessertItemRepository dessertItemRepository;

    @InjectMocks
    private ItemService itemService;

    private BeverageItem mockCoffee1, mockBeverage1;
    private DessertItem mockDessert1, mockDessert2;

    @BeforeEach
    void setUp() {
        mockCoffee1 = BeverageItem.builder().id(1L).beverageItemNameKo("아메리카노").category(ItemType.COFFEE).build();
        mockBeverage1 = BeverageItem.builder().id(2L).beverageItemNameKo("자바 칩 프라푸치노").category(ItemType.BEVERAGE).build();
        mockDessert1 = DessertItem.builder().id(10L).dessertItemNameKo("딸기 케이크").build();
        mockDessert2 = DessertItem.builder().id(11L).dessertItemNameKo("블루베리 베이글").build();
    }

    @Test
    @DisplayName("getDrinkItems 호출 시, 모든 음료 목록을 페이지네이션하여 성공적으로 반환한다")
    void getDrinkItems_Success() {
        // Given
        List<BeverageItem> allBeverages = Arrays.asList(mockCoffee1, mockBeverage1);
        given(beverageItemRepository.findAll()).willReturn(allBeverages);
        int page = 0;
        int size = 5;

        // When
        ItemListResponse result = itemService.getDrinkItems(page, size);

        // Then
        // 1. 음료 리포지토리만 호출되었는지 검증
        verify(beverageItemRepository, times(1)).findAll();
        verify(dessertItemRepository, never()).findAll(); // 디저트 리포지토리는 호출되지 않아야 함

        // 2. 결과 검증
        assertThat(result).isNotNull();
        assertThat(result.getTotalCount()).isEqualTo(allBeverages.size());
        assertThat(result.getItems()).hasSize(allBeverages.size());
        // 모든 결과물의 타입이 COFFEE 또는 BEVERAGE인지 확인
        assertThat(result.getItems()).allMatch(item -> item.getType() == ItemType.COFFEE || item.getType() == ItemType.BEVERAGE);
    }

    @Test
    @DisplayName("getDessertItems 호출 시, 모든 디저트 목록을 페이지네이션하여 성공적으로 반환한다")
    void getDessertItems_Success() {
        // Given
        List<DessertItem> allDesserts = Arrays.asList(mockDessert1, mockDessert2);
        given(dessertItemRepository.findAll()).willReturn(allDesserts);
        int page = 0;
        int size = 5;

        // When
        ItemListResponse result = itemService.getDessertItems(page, size);

        // Then
        // 1. 디저트 리포지토리만 호출되었는지 검증
        verify(dessertItemRepository, times(1)).findAll();
        verify(beverageItemRepository, never()).findAll(); // 음료 리포지토리는 호출되지 않아야 함

        // 2. 결과 검증
        assertThat(result).isNotNull();
        assertThat(result.getTotalCount()).isEqualTo(allDesserts.size());
        assertThat(result.getItems()).hasSize(allDesserts.size());
        // 모든 결과물의 타입이 DESSERT인지 확인
        assertThat(result.getItems()).allMatch(item -> item.getType() == ItemType.DESSERT);
    }

    @Test
    @DisplayName("음료 아이템이 하나도 없을 때, getDrinkItems가 빈 목록을 반환한다")
    void getDrinkItems_WhenNoDrinksExist_ReturnsEmptyResponse() {
        // Given
        given(beverageItemRepository.findAll()).willReturn(Collections.emptyList());

        // When
        ItemListResponse result = itemService.getDrinkItems(0, 10);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTotalCount()).isEqualTo(0);
        assertThat(result.getItems()).isEmpty();
    }
}