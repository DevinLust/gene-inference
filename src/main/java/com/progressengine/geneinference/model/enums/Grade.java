package com.progressengine.geneinference.model.enums;

public enum Grade {
    S, A, B, C, D, E;

    public static Grade fromString(String str) {
        return Grade.valueOf(str); // Throws IllegalArgumentException if not valid
    }

}
