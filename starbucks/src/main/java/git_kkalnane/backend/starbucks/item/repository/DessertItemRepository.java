package git_kkalnane.backend.starbucks.item.repository;

import git_kkalnane.backend.starbucks.item.domain.dessert.DessertItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
/**
 * 디저트 상품({@link DessertItem}) 엔티티에 대한 데이터 접근을 관리하는 레포지토리 인터페이스입니다.
 * 상품 목록 조회 기능에서 디저트 데이터를 가져오는 핵심 역할을 합니다.
 *
 * @author Seongjun In
 * @version 1.0
 */
@Repository
public interface DessertItemRepository extends JpaRepository<DessertItem, Long> {
}
