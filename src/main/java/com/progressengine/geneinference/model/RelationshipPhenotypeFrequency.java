package com.progressengine.geneinference.model;

import com.progressengine.geneinference.model.enums.Category;
import com.progressengine.geneinference.model.enums.Grade;
import jakarta.persistence.*;

@Entity
@IdClass(RelationshipPhenotypeFrequencyKey.class)
public class RelationshipPhenotypeFrequency {

    @Id
    @ManyToOne
    @JoinColumn(name = "relationship_id", nullable = false)
    private Relationship relationship;

    @Id
    @Enumerated(EnumType.STRING)
    private Category category;

    @Id
    @Enumerated(EnumType.STRING)
    private Grade allele;

    private Integer frequency;

    public RelationshipPhenotypeFrequency() {}

    public RelationshipPhenotypeFrequency(Relationship relationship, Category category, Grade allele, Integer frequency) {
        this.relationship = relationship;
        this.category = category;
        this.allele = allele;
        this.frequency = frequency;
    }

    // getters and setters

    public Relationship getRelationship() {
        return relationship;
    }

    public void setRelationship(Relationship relationship) {
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

    public Integer getFrequency() {
        return frequency;
    }

    public void setFrequency(Integer frequency) {
        this.frequency = frequency;
    }
}

