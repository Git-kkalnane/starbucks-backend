package git_kkalnane.backend.starbucks.store.service;

import git_kkalnane.backend.starbucks.store.common.exception.StoreErrorCode;
import git_kkalnane.backend.starbucks.store.common.exception.StoreException;
import git_kkalnane.backend.starbucks.store.domain.Store;
import git_kkalnane.backend.starbucks.store.dto.response.StoreDetailsResponse;
import git_kkalnane.backend.starbucks.store.dto.response.StoreListResponse;
import git_kkalnane.backend.starbucks.store.dto.response.StoreSummaryResponse;
import git_kkalnane.backend.starbucks.store.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


/**
 * 매장 관련 비즈니스 로직을 처리하는 서비스 클래스
 *
 * @author Seongjun In
 * @version 1.0
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StoreService {

    private final StoreRepository storeRepository;

    /**
     * 특정 매장의 상세 정보를 조회합니다.
     *
     * @param storeId 조회할 매장의 ID
     * @return 매장의 상세 정보 DTO
     * @throws StoreException 매장을 찾지 못한 경우
     */
    public StoreDetailsResponse getStoreDetails(Long storeId) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new StoreException(StoreErrorCode.STORE_NOT_FOUND));
        return StoreDetailsResponse.from(store);
    }

    /**
     * 페이징 처리된 전체 지점 목록을 조회합니다.
     */
    public StoreListResponse getStoreList(Pageable pageable) {
        Page<Store> storePage = storeRepository.findAll(pageable);
        List<StoreSummaryResponse> storeSummaries = storePage.getContent().stream()
                .map(StoreSummaryResponse::from)
                .toList();
        return StoreListResponse.builder()
                .stores(storeSummaries)
                .totalCount(storePage.getTotalElements())
                .currentPage(storePage.getNumber())
                .totalPages(storePage.getTotalPages())
                .pageSize(storePage.getSize())
                .build();
    }


}
