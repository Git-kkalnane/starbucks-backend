package git_kkalnane.backend.starbucks.item.domain.beverage.enums;

public enum BeverageShotOption {
    NO_SHOT(0),
    SHOT(500),
    DOUBLE_SHOT(1000);

    private final int extraPrice;

    BeverageShotOption(int extraPrice) {
        this.extraPrice = extraPrice;
    }

    public int getExtraPrice() {
        return extraPrice;
    }
}
