package git_kkalnane.backend.starbucks.item.service;

import git_kkalnane.backend.starbucks.item.domain.BeverageItem;
import git_kkalnane.backend.starbucks.item.domain.DessertItem;
import git_kkalnane.backend.starbucks.item.domain.ItemStatus;
import git_kkalnane.backend.starbucks.item.domain.ItemType;
import git_kkalnane.backend.starbucks.item.dto.response.ItemListResponse;
import git_kkalnane.backend.starbucks.item.repository.BeverageItemRepository;
import git_kkalnane.backend.starbucks.item.repository.DessertItemRepository;
import org.junit.jupiter.api.BeforeEach; // BeforeEach 임포트
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

    private List<BeverageItem> mockBeverages;
    private List<DessertItem> mockDesserts;

    @BeforeEach
    void setUp() {
        mockBeverages = Arrays.asList(
                BeverageItem.builder().id(1L).beverageItemNameKo("아메리카노").beverageItemNameEn("Americano").price(4500).imageUrl("b1.jpg").status(ItemStatus.AVAILABLE).build(),
                BeverageItem.builder().id(2L).beverageItemNameKo("카페 라떼").beverageItemNameEn("Cafe Latte").price(5000).imageUrl("b2.jpg").status(ItemStatus.AVAILABLE).build(),
                BeverageItem.builder().id(3L).beverageItemNameKo("콜드 브루").beverageItemNameEn("Cold Brew").price(5500).imageUrl("b3.jpg").status(ItemStatus.AVAILABLE).build()
        );
        mockDesserts = Arrays.asList(
                DessertItem.builder().id(10L).dessertItemNameKo("딸기 케이크").dessertItemNameEn("Strawberry Cake").price(7000).imageUrl("d1.jpg").status(ItemStatus.AVAILABLE).build(),
                DessertItem.builder().id(11L).dessertItemNameKo("블루베리 베이글").dessertItemNameEn("Blueberry Bagel").price(3000).imageUrl("d2.jpg").status(ItemStatus.AVAILABLE).build(),
                DessertItem.builder().id(12L).dessertItemNameKo("초콜릿 머핀").dessertItemNameEn("Chocolate Muffin").price(3500).imageUrl("d3.jpg").status(ItemStatus.AVAILABLE).build()
        );
    }

    @Test
    @DisplayName("음료와 디저트 아이템이 모두 존재하고, 필터 및 페이지네이션 없이 전체 목록 조회에 성공한다")
    void getOverallItems_Success_NoFilterNoPagination() {
        // Given
        given(beverageItemRepository.findAll()).willReturn(mockBeverages);
        given(dessertItemRepository.findAll()).willReturn(mockDesserts);

        // When
        ItemListResponse result = itemService.getOverallItems(null, 0, mockBeverages.size() + mockDesserts.size());

        // Then
        verify(beverageItemRepository, times(1)).findAll();
        verify(dessertItemRepository, times(1)).findAll();

        assertThat(result).isNotNull();
        assertThat(result.getTotalCount()).isEqualTo(mockBeverages.size() + mockDesserts.size());
        assertThat(result.getItems()).hasSize(mockBeverages.size() + mockDesserts.size());
        assertThat(result.getCurrentPage()).isEqualTo(0);
        assertThat(result.getPageSize()).isEqualTo(mockBeverages.size() + mockDesserts.size());
        assertThat(result.getTotalPages()).isEqualTo(1);

        assertThat(result.getItems().get(0).getNameKo()).isEqualTo("딸기 케이크");
        assertThat(result.getItems().get(1).getNameKo()).isEqualTo("블루베리 베이글");
        assertThat(result.getItems().get(result.getItems().size() - 1).getNameKo()).isEqualTo("콜드 브루");
    }

    @Test
    @DisplayName("아이템 타입을 DRINK로 필터링하여 조회 시 음료만 반환한다")
    void getOverallItems_Success_FilterByDrink() {
        // Given
        given(beverageItemRepository.findAll()).willReturn(mockBeverages);

        // When
        ItemListResponse result = itemService.getOverallItems(ItemType.DRINK, 0, mockBeverages.size());

        // Then
        verify(beverageItemRepository, times(1)).findAll();

        assertThat(result).isNotNull();
        assertThat(result.getTotalCount()).isEqualTo(mockBeverages.size());
        assertThat(result.getItems()).hasSize(mockBeverages.size());
        assertThat(result.getItems().stream().allMatch(item -> item.getType() == ItemType.DRINK)).isTrue();
        assertThat(result.getCurrentPage()).isEqualTo(0);
        assertThat(result.getPageSize()).isEqualTo(mockBeverages.size());
        assertThat(result.getTotalPages()).isEqualTo(1);
    }

    @Test
    @DisplayName("아이템 타입을 DESSERT로 필터링하여 조회 시 디저트만 반환한다")
    void getOverallItems_Success_FilterByDessert() {
        // Given
        given(dessertItemRepository.findAll()).willReturn(mockDesserts);

        // When
        ItemListResponse result = itemService.getOverallItems(ItemType.DESSERT, 0, mockDesserts.size());

        // Then
        verify(dessertItemRepository, times(1)).findAll();

        assertThat(result).isNotNull();
        assertThat(result.getTotalCount()).isEqualTo(mockDesserts.size());
        assertThat(result.getItems()).hasSize(mockDesserts.size());
        assertThat(result.getItems().stream().allMatch(item -> item.getType() == ItemType.DESSERT)).isTrue();
        assertThat(result.getCurrentPage()).isEqualTo(0);
        assertThat(result.getPageSize()).isEqualTo(mockDesserts.size());
        assertThat(result.getTotalPages()).isEqualTo(1);
    }

    @Test
    @DisplayName("페이지네이션을 적용하여 첫 번째 페이지를 조회 시 올바른 아이템 수를 반환한다")
    void getOverallItems_Success_PaginationFirstPage() {
        // Given
        given(beverageItemRepository.findAll()).willReturn(mockBeverages);
        given(dessertItemRepository.findAll()).willReturn(mockDesserts);

        int page = 0;
        int size = 2; // 한 페이지에 2개씩

        // When
        ItemListResponse result = itemService.getOverallItems(null, page, size);

        // Then
        verify(beverageItemRepository, times(1)).findAll();
        verify(dessertItemRepository, times(1)).findAll();

        assertThat(result).isNotNull();
        assertThat(result.getTotalCount()).isEqualTo(mockBeverages.size() + mockDesserts.size());
        assertThat(result.getItems()).hasSize(size);
        assertThat(result.getCurrentPage()).isEqualTo(page);
        assertThat(result.getPageSize()).isEqualTo(size);
        assertThat(result.getTotalPages()).isEqualTo((int) Math.ceil((double)(mockBeverages.size() + mockDesserts.size()) / size));

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