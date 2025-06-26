package git_kkalnane.backend.starbucks.item.domain.beverage.enums;

public enum BeverageSizeOption {
    TALL(0),
    GRANDE(500),
    VENTI(500),
    SHORT(0),
    SOLO(0),
    DOPPIO(0);

    private final int extraPrice;

    BeverageSizeOption(int extrePrice) {
        this.extraPrice = extrePrice;
    }
    public int getExtraPrice() {
        return extraPrice;
    }
}
