package com.progressengine.geneinference.model;

import com.progressengine.geneinference.model.enums.Grade;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class GradePair implements Serializable {

    @Enumerated(EnumType.STRING)
    private Grade grade1;

    @Enumerated(EnumType.STRING)
    private Grade grade2;

    public GradePair() {}

    public GradePair(Grade first, Grade second) {
        this.grade1 = first;
        this.grade2 = second;
    }

    public GradePair(String first, String second) {
        this.grade1 = Grade.valueOf(first);
        this.grade2 = Grade.valueOf(second);
    }

    public Grade getFirst() {
        return grade1;
    }

    public void setFirst(Grade first) {
        this.grade1 = first;
    }

    public Grade getSecond() {
        return grade2;
    }

    public void setSecond(Grade second) {
        this.grade2 = second;
    }

    // Ensure consistent equality for use as map key
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GradePair other)) return false;
        return grade1 == other.grade1 && grade2 == other.grade2;
    }

    @Override
    public int hashCode() {
        return Objects.hash(grade1, grade2);
    }

    @Override
    public String toString() {
        return "(" + grade1 + ", " + grade2 + ")";
    }
}

