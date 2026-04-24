package com.progressengine.geneinference.model.enums;

public enum Category {
    COLOR(0),
    TONE(1),
    SHINY(2),
    SWIM(3), FLY(4), RUN(5),
    POWER(6), STAMINA(7);

    private final int order;

    Category(int order) {
        this.order = order;
    }

    public static Category fromString(String str) {
        return Category.valueOf(str); // Throws IllegalArgumentException if not valid
    }

    public int getOrder() {
        return order;
    }
}
