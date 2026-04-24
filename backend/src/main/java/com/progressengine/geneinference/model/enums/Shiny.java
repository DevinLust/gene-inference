package com.progressengine.geneinference.model.enums;

public enum Shiny implements Allele{
    SHINY("SHN"), NON_SHINY("NRM");

    private final String code;

    Shiny(String code) {
        this.code = code;
    }

    @Override
    public String code() {
        return this.code;
    }

    public static Shiny fromCode(String code) {
        for (Shiny a : values()) {
            if (a.code.equals(code)) return a;
        }
        throw new IllegalArgumentException("Invalid code: " + code);
    }

    public boolean isRecessive() {
        return this == NON_SHINY;
    }
}
