package git_kkalnane.backend.starbucks.store.repository;

import git_kkalnane.backend.starbucks.store.domain.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Store 엔티티에 대한 데이터 접근을 처리하는 리포지토리
 *
 * @author Seongjun In
 * @version 1.0
 */
@Repository
public interface StoreRepository extends JpaRepository<Store, Long> {

    /**
     * 매장 이름으로 매장 정보를 조회합니다.
     *
     * @param name 조회할 매장의 이름
     * @return Optional<Store> 조회된 매장 정보 (없을 경우 Optional.empty())
     */
    Optional<Store> findByName(String name);
}
