package git_kkalnane.backend.starbucks.item.service;

import git_kkalnane.backend.starbucks.item.domain.BeverageItem;
import git_kkalnane.backend.starbucks.item.domain.DessertItem;
import git_kkalnane.backend.starbucks.item.domain.ItemStatus;
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

    // --- Mock 객체 선언 ---
    private BeverageItem mockCoffee1, mockCoffee2;
    private BeverageItem mockBeverage1;
    private DessertItem mockDessert1, mockDessert2;

    @BeforeEach
    void setUp() {
        // --- 1. Mock 데이터에 category 필드 추가 ---
        mockCoffee1 = BeverageItem.builder().id(1L).beverageItemNameKo("아메리카노").price(4500).hotImageUrl("c1_hot.jpg").category(ItemType.COFFEE).build();
        mockCoffee2 = BeverageItem.builder().id(3L).beverageItemNameKo("콜드 브루").price(5500).iceImageUrl("c2_ice.jpg").category(ItemType.COFFEE).build();
        mockBeverage1 = BeverageItem.builder().id(2L).beverageItemNameKo("자바 칩 프라푸치노").price(6000).iceImageUrl("b1_ice.jpg").category(ItemType.BEVERAGE).build();

        mockDessert1 = DessertItem.builder().id(10L).dessertItemNameKo("딸기 케이크").price(7000).imageUrl("d1.jpg").build();
        mockDessert2 = DessertItem.builder().id(11L).dessertItemNameKo("블루베리 베이글").price(3000).imageUrl("d2.jpg").build();
    }

    @Test
    @DisplayName("필터 없이 전체 목록 조회 시, 모든 아이템을 정렬하여 반환한다")
    void getOverallItems_Success_NoFilter() {
        // Given
        given(beverageItemRepository.findAll()).willReturn(Arrays.asList(mockCoffee1, mockCoffee2, mockBeverage1));
        given(dessertItemRepository.findAll()).willReturn(Arrays.asList(mockDessert1, mockDessert2));
        int totalSize = 5;

        // When
        ItemListResponse result = itemService.getOverallItems(null, 0, totalSize);

        // Then
        verify(beverageItemRepository, times(1)).findAll();
        verify(dessertItemRepository, times(1)).findAll();

        assertThat(result.getTotalCount()).isEqualTo(totalSize);
        assertThat(result.getItems()).hasSize(totalSize);
        // 정렬 확인 (한글 이름 기준): 딸기 -> 블루베리 -> 아메리카노 -> 자바칩 -> 콜드브루
        assertThat(result.getItems().get(0).getNameKo()).isEqualTo("딸기 케이크");
        assertThat(result.getItems().get(2).getNameKo()).isEqualTo("아메리카노");
        assertThat(result.getItems().get(4).getNameKo()).isEqualTo("콜드 브루");
    }

    // --- 2. DRINK 테스트를 COFFEE 와 BEVERAGE 로 분리 및 수정 ---

    @Test
    @DisplayName("아이템 타입을 COFFEE로 필터링 시, 커피만 반환한다")
    void getOverallItems_Success_FilterByCoffee() {
        // Given
        List<BeverageItem> coffeeList = Arrays.asList(mockCoffee1, mockCoffee2);
        // findByCategory 메서드가 호출될 것을 가정하고 Mocking
        given(beverageItemRepository.findByCategory(ItemType.COFFEE)).willReturn(coffeeList);

        // When
        ItemListResponse result = itemService.getOverallItems(ItemType.COFFEE, 0, 10);

        // Then
        verify(beverageItemRepository, times(1)).findByCategory(ItemType.COFFEE);
        verify(dessertItemRepository, times(0)).findAll(); // 디저트 리포지토리는 호출되지 않아야 함

        assertThat(result.getTotalCount()).isEqualTo(coffeeList.size());
        assertThat(result.getItems()).hasSize(coffeeList.size());
        // 모든 결과물의 타입이 COFFEE인지 확인
        assertThat(result.getItems()).allMatch(item -> item.getType() == ItemType.COFFEE);
    }

    @Test
    @DisplayName("아이템 타입을 BEVERAGE로 필터링 시, 커피가 아닌 음료만 반환한다")
    void getOverallItems_Success_FilterByBeverage() {
        // Given
        List<BeverageItem> beverageList = Collections.singletonList(mockBeverage1);
        given(beverageItemRepository.findByCategory(ItemType.BEVERAGE)).willReturn(beverageList);

        // When
        ItemListResponse result = itemService.getOverallItems(ItemType.BEVERAGE, 0, 10);

        // Then
        verify(beverageItemRepository, times(1)).findByCategory(ItemType.BEVERAGE);
        verify(dessertItemRepository, times(0)).findAll();

        assertThat(result.getTotalCount()).isEqualTo(beverageList.size());
        assertThat(result.getItems()).allMatch(item -> item.getType() == ItemType.BEVERAGE);
    }


    @Test
    @DisplayName("아이템 타입을 DESSERT로 필터링 시, 디저트만 반환한다")
    void getOverallItems_Success_FilterByDessert() {
        // Given
        List<DessertItem> dessertList = Arrays.asList(mockDessert1, mockDessert2);
        given(dessertItemRepository.findAll()).willReturn(dessertList);

        // When
        ItemListResponse result = itemService.getOverallItems(ItemType.DESSERT, 0, 10);

        // Then
        verify(beverageItemRepository, times(0)).findAll(); // 음료 리포지토리는 호출되지 않아야 함
        verify(dessertItemRepository, times(1)).findAll();

        assertThat(result.getTotalCount()).isEqualTo(dessertList.size());
        assertThat(result.getItems()).allMatch(item -> item.getType() == ItemType.DESSERT);
    }

    @Test
    @DisplayName("페이지네이션을 적용하여 첫 번째 페이지를 조회 시 올바른 아이템 수를 반환한다")
    void getOverallItems_Success_PaginationFirstPage() {
        // Given
        // --- 이 부분을 수정합니다 ---
        // 더 이상 존재하지 않는 mockBeverages/mockDesserts 대신, 개별 mock 객체로 리스트를 만듭니다.
        List<BeverageItem> allBeverages = Arrays.asList(mockCoffee1, mockCoffee2, mockBeverage1);
        List<DessertItem> allDesserts = Arrays.asList(mockDessert1, mockDessert2);

        given(beverageItemRepository.findAll()).willReturn(allBeverages);
        given(dessertItemRepository.findAll()).willReturn(allDesserts);
        // --------------------------

        int page = 0;
        int size = 2; // 한 페이지에 2개씩

        // When
        ItemListResponse result = itemService.getOverallItems(null, page, size);

        // Then
        verify(beverageItemRepository, times(1)).findAll();
        verify(dessertItemRepository, times(1)).findAll();

        assertThat(result).isNotNull();
        assertThat(result.getTotalCount()).isEqualTo(allBeverages.size() + allDesserts.size());
        assertThat(result.getItems()).hasSize(size);
        assertThat(result.getCurrentPage()).isEqualTo(page);
        assertThat(result.getPageSize()).isEqualTo(size);
        assertThat(result.getTotalPages()).isEqualTo((int) Math.ceil((double)(allBeverages.size() + allDesserts.size()) / size));

        // 정렬 순서에 따른 검증 (딸기 케이크, 블루베리 베이글)
        assertThat(result.getItems().get(0).getNameKo()).isEqualTo("딸기 케이크");
        assertThat(result.getItems().get(1).getNameKo()).isEqualTo("블루베리 베이글");
    }

    @Test
    @DisplayName("아이템이 하나도 존재하지 않을 때 빈 목록을 반환하며 조회에 성공한다")
    void getOverallItems_Success_NoItemsExist() {
        // Given
        given(beverageItemRepository.findAll()).willReturn(Collections.emptyList());
        given(dessertItemRepository.findAll()).willReturn(Collections.emptyList());

        // When
        ItemListResponse result = itemService.getOverallItems(null, 0, 10);

        // Then
        verify(beverageItemRepository, times(1)).findAll();
        verify(dessertItemRepository, times(1)).findAll();

        assertThat(result).isNotNull();
        assertThat(result.getTotalCount()).isEqualTo(0);
        assertThat(result.getItems()).isEmpty();
        assertThat(result.getCurrentPage()).isEqualTo(0);
        assertThat(result.getPageSize()).isEqualTo(10);
        assertThat(result.getTotalPages()).isEqualTo(0);
    }
}
