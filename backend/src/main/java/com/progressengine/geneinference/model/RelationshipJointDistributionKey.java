package com.progressengine.geneinference.model;

import com.progressengine.geneinference.model.enums.Category;
import com.progressengine.geneinference.model.enums.Grade;

import java.io.Serializable;
import java.util.Objects;

public class RelationshipJointDistributionKey implements Serializable {
    private Integer relationship;
    private Category category;
    private Grade grade1;
    private Grade grade2;

    public RelationshipJointDistributionKey() {}

    public RelationshipJointDistributionKey(
            Integer relationship, Category category, Grade grade1, Grade grade2
    ) {
        this.relationship = relationship;
        this.category = category;
        this.grade1 = grade1;
        this.grade2 = grade2;
    }

    // equals() and hashCode()
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RelationshipJointDistributionKey that)) return false;
        return Objects.equals(relationship, that.relationship)
                && Objects.equals(category, that.category)
                && grade1 == that.grade1
                && grade2 == that.grade2;
    }

    @Override
    public int hashCode() {
        return Objects.hash(relationship, category, grade1, grade2);
    }
}

