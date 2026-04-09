package com.progressengine.geneinference.model.enums;

public enum Tone implements Allele {
    TWO_TONE("T"), MONOTONE("M");

    private String code;

    Tone(String code) {
        this.code = code;
    }

    @Override
    public String code() {
        return this.code;
    }

    public static Tone fromCode(String code) {
        for (Tone a : values()) {
            if (a.code.equals(code)) return a;
        }
        throw new IllegalArgumentException("Invalid code: " + code);
    }
}
