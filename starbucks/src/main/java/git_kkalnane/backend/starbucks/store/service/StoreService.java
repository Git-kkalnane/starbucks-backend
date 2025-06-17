package git_kkalnane.backend.starbucks.store.service;

import git_kkalnane.backend.starbucks.store.common.exception.StoreErrorCode;
import git_kkalnane.backend.starbucks.store.common.exception.StoreException;
import git_kkalnane.backend.starbucks.store.domain.CrowdLevel;
import git_kkalnane.backend.starbucks.store.domain.Store;
import git_kkalnane.backend.starbucks.store.dto.response.StoreDetailsResponse;
import git_kkalnane.backend.starbucks.store.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StoreService {

    private final StoreRepository storeRepository;

    public StoreDetailsResponse getStoreDetails(Long storeId) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new StoreException(StoreErrorCode.STORE_NOT_FOUND));
        return StoreDetailsResponse.from(store);
    }


}
