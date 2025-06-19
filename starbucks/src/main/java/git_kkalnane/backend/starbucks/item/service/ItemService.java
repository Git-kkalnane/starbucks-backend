package git_kkalnane.backend.starbucks.item.service;

import git_kkalnane.backend.starbucks.item.domain.BeverageItem;
import git_kkalnane.backend.starbucks.item.domain.DessertItem;
import git_kkalnane.backend.starbucks.item.domain.ItemType;
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

/**
 * 아이템(상품) 관련 비즈니스 로직을 처리하는 서비스 클래스입니다.
 * 아이템 목록 조회 기능을 담당합니다.
 *
 * @author Seongjun In // 작성자 이름을 채워주세요.
 * @version 1.0
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemService {

    private final BeverageItemRepository beverageItemRepository;
    private final DessertItemRepository dessertItemRepository;

    /**
     * 시스템에 등록된 모든 음료 및 디저트 아이템 목록을 조회합니다.
     * 필터링이나 정렬 없이 현재 사용 가능한 모든 아이템을 반환합니다.
     * 페이지네이션을 적용하여 지정된 페이지의 아이템 목록을 반환합니다.
     *
     * @param type 조회할 아이템의 타입 (선택 사항: {@link ItemType#DRINK}, {@link ItemType#DESSERT}). null인 경우 모든 아이템 조회.
     * @param page 조회할 페이지 번호 (0부터 시작).
     * @param size 한 페이지당 아이템 개수.
     * @return {@link ItemListResponse} 형식의 아이템 목록 응답
     */
    public ItemListResponse getOverallItems(ItemType type, int page, int size) {
        List<ItemSummaryResponse> allItemSummaries = new ArrayList<>();

        // 1. 아이템 타입별 필터링된 데이터 조회 및 DTO 변환
        if (type == null || type == ItemType.DRINK) {
            List<BeverageItem> beverageItems = beverageItemRepository.findAll();
            List<ItemSummaryResponse> beverageSummaries = beverageItems.stream()
                    .map(ItemSummaryResponse::from)
                    .collect(Collectors.toList());
            allItemSummaries.addAll(beverageSummaries);
        }

        if (type == null || type == ItemType.DESSERT) {
            List<DessertItem> dessertItems = dessertItemRepository.findAll();
            List<ItemSummaryResponse> dessertSummaries = dessertItems.stream()
                    .map(ItemSummaryResponse::from)
                    .collect(Collectors.toList());
            allItemSummaries.addAll(dessertSummaries);
        }

        // 정렬 적용 (한글 기준)
        allItemSummaries.sort(Comparator.comparing(ItemSummaryResponse::getNameKo));


        // 페이지네여선 적용
        int start = page * size;
        int end = Math.min(start + size, allItemSummaries.size());

        List<ItemSummaryResponse> pagedItems;
        if (start < allItemSummaries.size()) {
            pagedItems = allItemSummaries.subList(start, end);
        } else {
            pagedItems = new ArrayList<>();
        }

        long totalElements = allItemSummaries.size();
        int totalPages = (int) Math.ceil((double) totalElements / size);
        if (totalElements == 0) totalPages = 0;

        return ItemListResponse.builder()
                .items(pagedItems)
                .totalCount(totalElements)
                .currentPage(page)
                .totalPages(totalPages)
                .pageSize(size)
                .build();

    }
}


