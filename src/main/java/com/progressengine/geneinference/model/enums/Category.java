package com.progressengine.geneinference.model.enums;

public enum Category {
    SWIM, FLY;

    public static Category fromString(String str) {
        return Category.valueOf(str); // Throws IllegalArgumentException if not valid
    }
}
