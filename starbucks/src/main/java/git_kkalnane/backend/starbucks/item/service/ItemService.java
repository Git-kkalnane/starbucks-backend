package git_kkalnane.backend.starbucks.item.service;


import git_kkalnane.backend.starbucks.item.common.exception.ItemErrorCode;
import git_kkalnane.backend.starbucks.item.common.exception.ItemException;
import git_kkalnane.backend.starbucks.item.domain.beverage.BeverageItem;
import git_kkalnane.backend.starbucks.item.domain.dessert.DessertItem;
import git_kkalnane.backend.starbucks.item.dto.response.ItemDetailResponse;
import git_kkalnane.backend.starbucks.item.dto.response.ItemListResponse;
import git_kkalnane.backend.starbucks.item.dto.response.ItemSummaryResponse;
import git_kkalnane.backend.starbucks.item.repository.BeverageItemRepository;
import git_kkalnane.backend.starbucks.item.repository.DessertItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemService {

    private final BeverageItemRepository beverageItemRepository;
    private final DessertItemRepository dessertItemRepository;
    
    private static final String BEVERAGE_NOT_FOUND = "해당하는 음료를 찾을 수 없습니다. ID: %d";
    private static final String DESSERT_NOT_FOUND = "해당하는 디저트를 찾을 수 없습니다. ID: %d";

    /**
     * 모든 음료(커피 포함) 목록을 조회, 정렬, 페이징하여 반환합니다.
     *
     * @param pageable 페이징 및 정렬 정보
     * @return ItemListResponse
     */
    public ItemListResponse getDrinkItems(Pageable pageable) {
        Page<BeverageItem> beveragePage = beverageItemRepository.findAll(pageable);

        List<ItemSummaryResponse> summaries = beveragePage.getContent().stream()
                .map(ItemSummaryResponse::from)
                .collect(Collectors.toList());

        return ItemListResponse.builder()
                .items(summaries)
                .totalCount(beveragePage.getTotalElements())
                .currentPage(beveragePage.getNumber())
                .totalPages(beveragePage.getTotalPages())
                .pageSize(beveragePage.getSize())
                .build();
    }

    /**
     * 모든 디저트 목록을 조회, 정렬, 페이징하여 반환합니다.
     *
     * @param pageable 페이징 및 정렬 정보
     * @return ItemListResponse
     */
    public ItemListResponse getDessertItems(Pageable pageable) {
        Page<DessertItem> dessertPage = dessertItemRepository.findAll(pageable);

        List<ItemSummaryResponse> summaries = dessertPage.getContent().stream()
                .map(ItemSummaryResponse::from)
                .collect(Collectors.toList());

        return ItemListResponse.builder()
                .items(summaries)
                .totalCount(dessertPage.getTotalElements())
                .currentPage(dessertPage.getNumber())
                .totalPages(dessertPage.getTotalPages())
                .pageSize(dessertPage.getSize())
                .build();
    }


    /**
     * ID로 디저트 상세 정보를 조회합니다.
     *
     * @param id 조회할 디저트 ID
     * @return 디저트 상세 정보
     * @throws ItemException 해당 ID의 디저트를 찾을 수 없는 경우 404 에러 반환
     */
    public ItemDetailResponse getDessertDetail(Long id) {
        DessertItem dessertItem = dessertItemRepository.findById(id)
                .orElseThrow(() -> new ItemException(
                       ItemErrorCode.DESSERT_NOT_FOUND
                ));
                
        return ItemDetailResponse.from(dessertItem);
    }
}