package com.progressengine.geneinference.model;

import com.progressengine.geneinference.model.enums.Allele;
import com.progressengine.geneinference.service.AlleleDomains.AlleleDomain;
import org.springframework.lang.NonNull;

public record AlleleCodePair(String first, String second) {
    public AlleleCodePair {
        if (first == null) {
            throw new IllegalArgumentException("first allele code cannot be null");
        }
        if (second == null) {
            throw new IllegalArgumentException("second allele code cannot be null");
        }
    }

    public AlleleCodePair(Allele first, Allele second) {
        this(
                requireCode(first, "first allele cannot be null"),
                requireCode(second, "second allele cannot be null")
        );
    }

    private static String requireCode(Allele allele, String message) {
        if (allele == null) {
            throw new IllegalArgumentException(message);
        }
        return allele.code();
    }

    public static <A extends Enum<A> & Allele> AlleleCodePair fromAllelePair(AllelePair<A> pair) {
        if (pair == null) {
            throw new IllegalArgumentException("pair cannot be null");
        }
        if (pair.getFirst() == null || pair.getSecond() == null) {
            throw new IllegalArgumentException("allele pair values cannot be null");
        }

        return new AlleleCodePair(
                pair.getFirst().code(),
                pair.getSecond().code()
        );
    }

    public <A extends Enum<A> & Allele> AllelePair<A> toAllelePair(AlleleDomain<A> domain) {
        return new AllelePair<>(
                domain.parse(first),
                domain.parse(second)
        );
    }

    public String toKey() {
        return first + "|" + second;
    }

    public static AlleleCodePair fromKey(String key) {
        String[] parts = key.split("\\|");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid key: " + key);
        }
        return new AlleleCodePair(parts[0], parts[1]);
    }

    @Override
    @NonNull
    public String toString() {
        return first + "|" + second;
    }
}
