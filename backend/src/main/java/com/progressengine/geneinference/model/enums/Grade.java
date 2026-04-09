package com.progressengine.geneinference.model.enums;

public enum Grade implements Allele {
    S("S", 5), A("A", 4), B("B", 3), C("C", 2), D("D", 1), E("E", 0);

    private final String code;
    private final int rank;

    Grade(String code, int rank) { 
        this.code = code;
        this.rank = rank; 
    }

    @Override
    public String code() {
        return this.code;
    }

    public int rank() { return rank; }

    public boolean isBetterThan(Grade other) {
        return this.rank > other.rank;
    }

    public Grade promoteOnce() {
        return promoteBy(1);
    }

    public Grade promoteBy(int steps) {
        if (steps <= 0) return this;

        int newRank = Math.min(S.rank, this.rank + steps);
        return fromRank(newRank);
    }

    public static Grade fromRank(int rank) {
        for (Grade g : values()) {
            if (g.rank == rank) return g;
        }
        throw new IllegalArgumentException("Unknown Grade rank: " + rank);
    }

    public static Grade fromString(String str) {
        return Grade.valueOf(str);
    }

    public static Grade fromCode(String code) {
        for (Grade a : values()) {
            if (a.code.equals(code)) return a;
        }
        throw new IllegalArgumentException("Invalid code: " + code);
    }
}
