package git_kkalnane.backend.starbucks.item.repository;

import git_kkalnane.backend.starbucks.item.domain.ItemType;
import git_kkalnane.backend.starbucks.item.domain.beverage.BeverageItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 음료 상품({@link BeverageItem}) 엔티티에 대한 데이터 접근을 관리하는 레포지토리 인터페이스입니다.
 * 상품 목록 조회 기능에서 음료 데이터를 가져오는 핵심 역할을 합니다.
 *
 * @author Seongjun In
 * @version 1.0
 */
@Repository
public interface BeverageItemRepository extends JpaRepository<BeverageItem, Long> {
    List<BeverageItem> findByCategory(ItemType category);
    
    @Query("SELECT bi FROM BeverageItem bi " +
           "LEFT JOIN FETCH bi.supportedSizes " +
           "LEFT JOIN FETCH bi.supportedTemperatures " +
           "WHERE bi.id = :id")
    Optional<BeverageItem> findByIdWithDetails(@Param("id") Long id);
}
