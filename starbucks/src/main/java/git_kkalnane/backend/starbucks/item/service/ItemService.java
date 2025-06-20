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
 * @author Seongjun In
 * @version 1.0
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemService {

    private final BeverageItemRepository beverageItemRepository;
    private final DessertItemRepository dessertItemRepository;

    /**
     * 시스템에 등록된 음료 및 디저트 아이템 목록을 조회합니다.
     * 타입별 필터링, 정렬, 페이지네이션을 적용하여 결과를 반환합니다.
     *
     * @param type 조회할 아이템의 타입 (선택 사항: {@link ItemType#COFFEE}, {@link ItemType#BEVERAGE}, {@link ItemType#DESSERT}). null인 경우 모든 아이템 조회.
     * @param page 조회할 페이지 번호 (0부터 시작).
     * @param size 한 페이지당 아이템 개수.
     * @return {@link ItemListResponse} 형식의 아이템 목록 응답
     */
    public ItemListResponse getOverallItems(ItemType type, int page, int size) {
        List<ItemSummaryResponse> allItemSummaries = new ArrayList<>();

        // 1: "디저트 보기" - type이 DESSERT로 명확하게 지정된 경우
        if (type == ItemType.DESSERT) {
            List<DessertItem> dessertItems = dessertItemRepository.findAll();
            allItemSummaries.addAll(dessertItems.stream()
                    .map(ItemSummaryResponse::from)
                    .collect(Collectors.toList()));
        }
        // 2: "커피&음료 보기" - type이 COFFEE 또는 BEVERAGE로 지정된 경우
        else if (type == ItemType.COFFEE || type == ItemType.BEVERAGE) {
            List<BeverageItem> beverageItems = beverageItemRepository.findByCategory(type);
            allItemSummaries.addAll(beverageItems.stream()
                    .map(ItemSummaryResponse::from)
                    .collect(Collectors.toList()));
        }
        // 3: "전체 보기" - type이 지정되지 않은 경우
        else {
            // 모든 음료(커피 포함)를 리스트에 추가
            List<BeverageItem> beverageItems = beverageItemRepository.findAll();
            allItemSummaries.addAll(beverageItems.stream()
                    .map(ItemSummaryResponse::from)
                    .collect(Collectors.toList()));

            // 모든 디저트를 리스트에 추가
            List<DessertItem> dessertItems = dessertItemRepository.findAll();
            allItemSummaries.addAll(dessertItems.stream()
                    .map(ItemSummaryResponse::from)
                    .collect(Collectors.toList()));
        }

        // 정렬 적용 (한글 이름 기준 오름차순)
        allItemSummaries.sort(Comparator.comparing(ItemSummaryResponse::getNameKo));

        // 페이지네이션 적용
        int start = page * size;
        int end = Math.min(start + size, allItemSummaries.size());

        List<ItemSummaryResponse> pagedItems;
        if (start >= allItemSummaries.size()) {
            pagedItems = new ArrayList<>(); // 요청한 페이지가 전체 데이터 범위를 벗어날 경우 빈 리스트 반환
        } else {
            pagedItems = allItemSummaries.subList(start, end);
        }

        long totalElements = allItemSummaries.size();
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