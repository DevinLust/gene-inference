package com.progressengine.geneinference.model;

import com.progressengine.geneinference.model.enums.Allele;
import com.progressengine.geneinference.service.AlleleDomains.AlleleDomain;

import java.io.Serializable;
import java.util.Objects;

public record AllelePair<A extends Enum<A> & Allele>(A allele1, A allele2) implements Serializable {

    public static <A extends Enum<A> & Allele> AllelePair<A> fromStrings(
            String first,
            String second,
            AlleleDomain<A> domain
    ) {
        return new AllelePair<>(first == null ? null : domain.parse(first), second == null ? null : domain.parse(second));
    }

    public A getFirst() {
        return this.allele1;
    }

    public A getSecond() {
        return this.allele2;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AllelePair<?>(Object allele3, Object allele4))) return false;
        return java.util.Objects.equals(allele1, allele3)
                && java.util.Objects.equals(allele2, allele4);
    }

    @Override
    public int hashCode() {
        return Objects.hash(allele1, allele2);
    }

    @Override
    public String toString() {
        return "(" + allele1 + ", " + allele2 + ")";
    }
}
