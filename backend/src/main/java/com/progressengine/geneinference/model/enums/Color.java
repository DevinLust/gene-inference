package com.progressengine.geneinference.model.enums;

public enum Color implements Allele{
    NORMAL("NRM"), WHITE("WHT"), RED("RED"),
    BLUE("BLU"), YELLOW("YEL"), PINK("PNK"),
    PURPLE("PUR"), SKY_BLUE("SKY"), ORANGE("ORA"),
    GREEN("GRN"), BROWN("BRN"), GREY("GRY"),
    LIME_GREEN("LIM"), BLACK("BLK");

    private final String code;

    Color(String code) {
        this.code = code;
    }

    @Override
    public String code() {
        return this.code;
    }

    public static Color fromCode(String code) {
        for (Color a : values()) {
            if (a.code.equals(code)) return a;
        }
        throw new IllegalArgumentException("Invalid code: " + code);
    }

    public boolean isRecessive() {
        return this == NORMAL;
    }
}
