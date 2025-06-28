package git_kkalnane.backend.starbucks.item.dto.response;

import git_kkalnane.backend.starbucks.item.domain.ItemStatus;
import git_kkalnane.backend.starbucks.item.domain.ItemType;
import git_kkalnane.backend.starbucks.item.domain.beverage.BeverageItem;
import git_kkalnane.backend.starbucks.item.domain.beverage.enums.BeverageSizeOption;
import git_kkalnane.backend.starbucks.item.domain.beverage.enums.BeverageTemperatureOption;
import git_kkalnane.backend.starbucks.item.domain.dessert.DessertItem;
import git_kkalnane.backend.starbucks.item.dto.common.ImageUrl;
import java.util.Set;

/**
 * 음료 상세 정보를 담는 응답 DTO
 *
 * @param id 아이템 ID
 * @param nameKo 아이템 한글 이름
 * @param nameEn 아이템 영문 이름
 * @param description 아이템 설명
 * @param price 가격
 * @param isCoffee 커피 여부
 * @param imageUrl 이미지 URL 정보
 * @param category 음료 카테고리
 * @param status 아이템 상태
 * @param supportedSizes 지원되는 사이즈 목록
 * @param supportedTemperatures 지원되는 온도 옵션 목록
 */

public record ItemDetailResponse(
    Long id,
    String nameKo,
    String nameEn,
    String description,
    int price,
    boolean isCoffee,
    ImageUrl imageUrl,
    ItemType category,
    ItemStatus status,
    Set<BeverageSizeOption> supportedSizes,
    Set<BeverageTemperatureOption> supportedTemperatures
) {
  
  public static ItemDetailResponse from(DessertItem dessertItem) {
    return new ItemDetailResponse(
        dessertItem.getId(),
        dessertItem.getDessertItemNameKo(),
        dessertItem.getDessertItemNameEn(),
        dessertItem.getDescription(),
        dessertItem.getPrice(),
        false, // isCoffee 항상 false
        ImageUrl.fromDefaultUrl(dessertItem.getImageUrl()),
        ItemType.DESSERT,
        dessertItem.getStatus(),
        Set.of(), // 사이즈 옵션 항상 없음
        Set.of()  // 아직 디저트는 옵션이 없음,
    );
  }
}

