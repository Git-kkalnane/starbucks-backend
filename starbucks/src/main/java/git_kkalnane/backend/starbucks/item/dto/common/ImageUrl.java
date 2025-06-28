package git_kkalnane.backend.starbucks.item.dto.common;

import git_kkalnane.backend.starbucks.item.domain.beverage.BeverageItem;

/**
 * 이미지 URL 정보를 담는 레코드
 *
 * @param hotImageUrl 핫 음료 이미지 URL
 * @param iceImageUrl 아이스 음료 이미지 URL
 */
public record ImageUrl(
    String hotImageUrl,
    String iceImageUrl
) {
  public static ImageUrl from(BeverageItem beverageItem) {
    return new ImageUrl(
        beverageItem.getHotImageUrl(),
        beverageItem.getIceImageUrl()
    );
  }
}

