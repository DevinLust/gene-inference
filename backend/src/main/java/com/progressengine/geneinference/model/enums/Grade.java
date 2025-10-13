package com.progressengine.geneinference.model.enums;

public enum Grade {
    S(5), A(4), B(3), C(2), D(1), E(0);

    private final int value;
    Grade(int value) {
        this.value = value;
    }

    public boolean isBetterThan(Grade other) {
        return this.value > other.value;
    }

    public static Grade fromString(String str) {
        return Grade.valueOf(str); // Throws IllegalArgumentException if not valid
    }

}
