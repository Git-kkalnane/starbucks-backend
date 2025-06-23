package git_kkalnane.backend.starbucks.store.domain;

import git_kkalnane.backend.starbucks._global.entity.BaseTimeEntity;
import git_kkalnane.backend.starbucks.merchant.domain.Merchant;
import jakarta.persistence.*;
import lombok.*;

/**
 * 스타벅스 매장 정보를 나타내는 엔티티 클래스입니다.
 * 데이터베이스의 'stores' 테이블과 매핑됩니다.
 * 매장의 기본 정보, 위치, 편의시설, 혼잡도 등을 관리합니다.
 *
 * @author Seongjun In
 * @version 1.0
 */
@Entity
@Table(name = "stores")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Store extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "merchant_id", nullable = false)
    private Merchant merchant;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Column(name = "address", nullable = false, length = 255)
    private String address;

    @Column(name = "phone", nullable = false, length = 20)
    private String phone;

    @Column(name = "opening_hours")
    private String openingHours;

    @Column(name = "has_drive_through", nullable = false)
    private boolean hasDriveThrough = false;

    @Column(name = "seating_capacity", nullable = false)
    private int seatingCapacity;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "image_url", length = 255)
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "current_crowd_level", nullable = false)
    private CrowdLevel currentCrowdLevel = CrowdLevel.LOW;

}
