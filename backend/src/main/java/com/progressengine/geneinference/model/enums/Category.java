package com.progressengine.geneinference.model.enums;

public enum Category {
    SWIM(0), FLY(1), RUN(2), POWER(3), STAMINA(4);

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
