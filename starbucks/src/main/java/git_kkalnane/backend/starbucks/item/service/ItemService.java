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
@Transactional(readOnly = true) // 읽기 전용 트랜잭션으로 설정
public class ItemService {

    private final BeverageItemRepository beverageItemRepository;
    private final DessertItemRepository dessertItemRepository;

    /**
     * 시스템에 등록된 모든 음료 및 디저트 아이템 목록을 조회합니다.
     * 필터링이나 정렬 없이 현재 사용 가능한 모든 아이템을 반환합니다.
     *
     * @return {@link ItemListResponse} 형식의 아이템 목록 응답
     */
    public ItemListResponse getOverallItems() {
        // 1. 모든 음료 아이템 조회 및 DTO 변환
        List<BeverageItem> beverageItems = beverageItemRepository.findAll();
        List<ItemSummaryResponse> beverageSummaries = beverageItems.stream()
                .map(ItemSummaryResponse::from) // BeverageItem을 ItemSummaryResponse로 변환
                .collect(Collectors.toList());

        // 2. 모든 디저트 아이템 조회 및 DTO 변환
        List<DessertItem> dessertItems = dessertItemRepository.findAll();
        List<ItemSummaryResponse> dessertSummaries = dessertItems.stream()
                .map(ItemSummaryResponse::from) // DessertItem을 ItemSummaryResponse로 변환
                .collect(Collectors.toList());

        // 3. 두 목록을 하나로 합치기
        List<ItemSummaryResponse> allItemSummaries = new ArrayList<>();
        allItemSummaries.addAll(beverageSummaries);
        allItemSummaries.addAll(dessertSummaries);

        // 4. ItemListResponse 생성 및 반환
        return ItemListResponse.builder()
                .items(allItemSummaries)
                .totalCount(allItemSummaries.size())
                .build();
    }

    // TODO:
    // - 필터링 기능 (예: 카테고리별, 품절 여부 포함/제외) 추가
    // - 정렬 기능 (예: 가격순, 이름순) 추가
    // - 페이징 기능 추가 (성능 최적화 및 대규모 데이터 처리)
    // - ItemStatus.HIDDEN 상태인 아이템은 조회에서 제외하는 로직 추가 (현재는 모든 상태 포함)
}