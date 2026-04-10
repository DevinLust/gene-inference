package com.progressengine.geneinference.model;

import com.progressengine.geneinference.model.enums.Allele;
import com.progressengine.geneinference.service.AlleleDomains.AlleleDomain;

import java.io.Serializable;
import java.util.Objects;

public class AllelePair<A extends Enum<A> & Allele> implements Serializable {

    private A allele1;
    private A allele2;

    public AllelePair(A first, A second) {
        this.allele1 = first;
        this.allele2 = second;
    }

    public static <A extends Enum<A> & Allele> AllelePair<A> fromStrings(
        String first,
        String second,
        AlleleDomain<A> domain
    ) {
        return new AllelePair<>(domain.parse(first), domain.parse(second));
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
        if (!(o instanceof AllelePair other)) return false;
        return allele1 == other.allele1 && allele2 == other.allele2;
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
