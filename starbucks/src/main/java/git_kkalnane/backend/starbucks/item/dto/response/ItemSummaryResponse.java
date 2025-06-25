package git_kkalnane.backend.starbucks.item.dto.response;

import git_kkalnane.backend.starbucks.item.domain.*;
import git_kkalnane.backend.starbucks.item.domain.beverage.BeverageItem;
import git_kkalnane.backend.starbucks.item.domain.beverage.enums.BeverageShotOption;
import git_kkalnane.backend.starbucks.item.domain.beverage.enums.BeverageSizeOption;
import git_kkalnane.backend.starbucks.item.domain.beverage.enums.BeverageTemperatureOption;
import git_kkalnane.backend.starbucks.item.domain.dessert.DessertItem;
import lombok.Builder;
import lombok.Getter;

/**
 * 아이템 목록 조회 시 각 아이템의 요약 정보를 담는 DTO입니다.
 * 음료(BeverageItem)와 디저트(DessertItem) 공통으로 사용될 수 있도록 설계되었습니다.
 *
 * @author Seongjun In
 * @version 1.0
 */
@Getter
@Builder
public class ItemSummaryResponse {

    private final Long id;
    private final String nameKo;
    private final String nameEn;
    private final int price;
    private final String hotImageUrl;
    private final String iceImageUrl;
    private final String dessertImageUrl;
    private final ItemStatus status;
    private final ItemType type;
    private final BeverageSizeOption sizeOption;
    private final BeverageTemperatureOption temperatureOption;
    private final BeverageShotOption shotOption;

    /**
     * {@link BeverageItem} 엔티티로부터 {@link ItemSummaryResponse} DTO를 생성하는 팩토리 메서드입니다.
     *
     * @param beverageItem 변환할 음료 아이템 엔티티
     * @return {@link ItemSummaryResponse} 객체
     */
    public static ItemSummaryResponse from(BeverageItem beverageItem) {
        return ItemSummaryResponse.builder()
                .id(beverageItem.getId())
                .nameKo(beverageItem.getBeverageItemNameKo())
                .nameEn(beverageItem.getBeverageItemNameEn())
                .price(beverageItem.getPrice())
                .hotImageUrl(beverageItem.getHotImageUrl())
                .iceImageUrl(beverageItem.getIceImageUrl())
                .status(beverageItem.getStatus())
                .type(beverageItem.getCategory())
                .sizeOption(beverageItem.getSizeOption())
                .temperatureOption(beverageItem.getTemperatureOption())
                .shotOption(beverageItem.getShotOption())
                .build();
    }

    /**
     * {@link DessertItem} 엔티티로부터 {@link ItemSummaryResponse} DTO를 생성하는 팩토리 메서드입니다.
     *
     * @param dessertItem 변환할 디저트 아이템 엔티티
     * @return {@link ItemSummaryResponse} 객체
     */
    public static ItemSummaryResponse from(DessertItem dessertItem) {
        return ItemSummaryResponse.builder()
                .id(dessertItem.getId())
                .nameKo(dessertItem.getDessertItemNameKo())
                .nameEn(dessertItem.getDessertItemNameEn())
                .price(dessertItem.getPrice())
                .dessertImageUrl(dessertItem.getImageUrl())
                .status(dessertItem.getStatus())
                .type(ItemType.DESSERT)
                .sizeOption(null)
                .temperatureOption(null)
                .shotOption(null)
                .build();
    }

}
