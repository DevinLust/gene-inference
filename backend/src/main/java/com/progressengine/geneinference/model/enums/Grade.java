package com.progressengine.geneinference.model.enums;

public enum Grade implements Allele {
    S(5), A(4), B(3), C(2), D(1), E(0);

    private final int rank;

    Grade(int rank) { this.rank = rank; }

    @Override
    public String code() {
        return name();
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
}
