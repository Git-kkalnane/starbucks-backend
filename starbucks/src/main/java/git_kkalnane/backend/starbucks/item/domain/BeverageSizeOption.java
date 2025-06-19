package git_kkalnane.backend.starbucks.item.domain;

public enum BeverageSizeOption {
    TALL(0),
    GRANDED(500),
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
