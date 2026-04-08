package com.progressengine.geneinference.model.enums;

public enum Tone implements Allele {
    TWO_TONE, MONOTONE;

    @Override
    public String code() {
        return name();
    }
}
