package git_kkalnane.backend.starbucks.item.service;

import git_kkalnane.backend.starbucks.item.domain.BeverageItem;
import git_kkalnane.backend.starbucks.item.domain.DessertItem;
import git_kkalnane.backend.starbucks.item.dto.response.ItemListResponse;
import git_kkalnane.backend.starbucks.item.dto.response.ItemSummaryResponse;
import git_kkalnane.backend.starbucks.item.repository.BeverageItemRepository;
import git_kkalnane.backend.starbucks.item.repository.DessertItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemService {

    private final BeverageItemRepository beverageItemRepository;
    private final DessertItemRepository dessertItemRepository;

    /**
     * 모든 음료(커피 포함) 목록을 조회, 정렬, 페이징하여 반환합니다.
     *
     * @param page  페이지 번호
     * @param size  페이지 크기
     * @return ItemListResponse
     */
    public ItemListResponse getDrinkItems(int page, int size) {
        List<BeverageItem> beverageItems = beverageItemRepository.findAll();

        List<ItemSummaryResponse> summaries = beverageItems.stream()
                .map(ItemSummaryResponse::from)
                .collect(Collectors.toList());

        return paginateAndBuildResponse(summaries, page, size);
    }

    /**
     * 모든 디저트 목록을 조회, 정렬, 페이징하여 반환합니다.
     *
     * @param page  페이지 번호
     * @param size  페이지 크기
     * @return ItemListResponse
     */
    public ItemListResponse getDessertItems(int page, int size) {
        List<DessertItem> dessertItems = dessertItemRepository.findAll();

        List<ItemSummaryResponse> summaries = dessertItems.stream()
                .map(ItemSummaryResponse::from)
                .collect(Collectors.toList());

        return paginateAndBuildResponse(summaries, page, size);
    }

    /**
     * 아이템 목록을 정렬하고 페이지네이션을 적용하여 최종 응답 객체를 생성하는 private 헬퍼 메서드입니다.
     *
     * @param items 정렬 및 페이징할 아이템 DTO 리스트
     * @param page  페이지 번호
     * @param size  페이지 크기
     * @return ItemListResponse 최종 응답
     */
    private ItemListResponse paginateAndBuildResponse(List<ItemSummaryResponse> items, int page, int size) {
        items.sort(Comparator.comparing(ItemSummaryResponse::getNameKo));

        int start = page * size;
        List<ItemSummaryResponse> pagedItems;

        if (start >= items.size()) {
            pagedItems = new ArrayList<>();
        } else {
            int end = Math.min(start + size, items.size());
            pagedItems = items.subList(start, end);
        }

        long totalElements = items.size();
        int totalPages = (totalElements == 0) ? 0 : (int) Math.ceil((double) totalElements / size);

        return ItemListResponse.builder()
                .items(pagedItems)
                .totalCount(totalElements)
                .currentPage(page)
                .totalPages(totalPages)
                .pageSize(size)
                .build();
    }
}