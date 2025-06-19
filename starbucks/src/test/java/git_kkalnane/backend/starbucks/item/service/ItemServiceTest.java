package git_kkalnane.backend.starbucks.item.service;

import git_kkalnane.backend.starbucks.item.domain.BeverageItem;
import git_kkalnane.backend.starbucks.item.domain.DessertItem;
import git_kkalnane.backend.starbucks.item.domain.ItemStatus;
import git_kkalnane.backend.starbucks.item.domain.ItemType;
import git_kkalnane.backend.starbucks.item.dto.response.ItemListResponse;
import git_kkalnane.backend.starbucks.item.dto.response.ItemSummaryResponse;
import git_kkalnane.backend.starbucks.item.repository.BeverageItemRepository;
import git_kkalnane.backend.starbucks.item.repository.DessertItemRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
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

    @Test
    @DisplayName("음료와 디저트 아이템이 전체 목록을 조회하면 성공한다.")
    void getOverallItems_Success_BothExist() {
        // Given
        BeverageItem mockBeverage1 = BeverageItem.builder()
                .id(1L).beverageItemNameKo("아메리카노").beverageItemNameEn("Americano").price(4500)
                .imageUrl("beverage1.jpg").status(ItemStatus.AVAILABLE).build();
        BeverageItem mockBeverage2 = BeverageItem.builder()
                .id(2L).beverageItemNameKo("카페 라떼").beverageItemNameEn("Cafe Latte").price(5000)
                .imageUrl("beverage2.jpg").status(ItemStatus.AVAILABLE).build();
        List<BeverageItem> mockBeverages = Arrays.asList(mockBeverage1, mockBeverage2);

        DessertItem mockDessert1 = DessertItem.builder()
                .id(10L).dessertItemNameKo("딸기 케이크").dessertItemNameEn("Strawberry Cake").price(7000)
                .imageUrl("dessert1.jpg").status(ItemStatus.AVAILABLE).build();
        List<DessertItem> mockDesserts = Arrays.asList(mockDessert1);

        given(beverageItemRepository.findAll()).willReturn(mockBeverages);
        given(dessertItemRepository.findAll()).willReturn(mockDesserts);

        // When
        ItemListResponse result = itemService.getOverallItems();

        // Then
        verify(beverageItemRepository, times(1)).findAll();
        verify(dessertItemRepository, times(1)).findAll();

        assertThat(result).isNotNull();
        assertThat(result.getTotalCount()).isEqualTo(mockBeverages.size() + mockDesserts.size());
        assertThat(result.getItems()).hasSize(mockBeverages.size() + mockDesserts.size());

        ItemSummaryResponse firstItem = result.getItems().get(0);
        assertThat(firstItem.getId()).isEqualTo(mockBeverage1.getId());
        assertThat(firstItem.getNameKo()).isEqualTo(mockBeverage1.getBeverageItemNameKo());
        assertThat(firstItem.getType()).isEqualTo(ItemType.DRINK);

        ItemSummaryResponse lastItem = result.getItems().get(result.getItems().size() - 1);
        assertThat(lastItem.getId()).isEqualTo(mockDessert1.getId());
        assertThat(lastItem.getNameKo()).isEqualTo(mockDessert1.getDessertItemNameKo());
        assertThat(lastItem.getType()).isEqualTo(ItemType.DESSERT);
}
}