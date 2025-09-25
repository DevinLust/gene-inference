package com.progressengine.geneinference.model;

import com.progressengine.geneinference.model.enums.Category;
import com.progressengine.geneinference.model.enums.Grade;

import java.io.Serializable;
import java.util.Objects;

public class RelationshipPhenotypeFrequencyKey implements Serializable {

    private Integer relationship;
    private Category category;
    private Grade allele;

    public RelationshipPhenotypeFrequencyKey() {}

    public RelationshipPhenotypeFrequencyKey(Integer relationshipId, Category category, Grade allele) {
        this.relationship = relationshipId;
        this.category = category;
        this.allele = allele;
    }

    public Integer getRelationship() {
        return relationship;
    }

    public void setRelationship(Integer relationship) {
        this.relationship = relationship;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public Grade getAllele() {
        return allele;
    }

    public void setAllele(Grade allele) {
        this.allele = allele;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RelationshipPhenotypeFrequencyKey that)) return false;
        return Objects.equals(relationship, that.relationship)
                && category == that.category
                && Objects.equals(allele, that.allele);
    }

    @Override
    public int hashCode() {
        return Objects.hash(relationship, category, allele);
    }
}

