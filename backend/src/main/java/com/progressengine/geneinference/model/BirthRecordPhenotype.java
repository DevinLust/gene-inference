package com.progressengine.geneinference.model;

import com.progressengine.geneinference.model.enums.Category;
import com.progressengine.geneinference.model.enums.Grade;
import jakarta.persistence.*;

import java.util.Objects;

@Entity
@IdClass(BirthRecordPhenotypeKey.class)
@Table(name = "birth_record_phenotype")
public class BirthRecordPhenotype {
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "birth_record_id", nullable = false)
    private BirthRecord birthRecord;

    @Id
    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    private Category category;

    @Enumerated(EnumType.STRING)
    @Column(name = "parent1_phenotype", nullable = false)
    private Grade parent1Phenotype;

    @Enumerated(EnumType.STRING)
    @Column(name = "parent2_phenotype", nullable = false)
    private Grade parent2Phenotype;

    @Enumerated(EnumType.STRING)
    @Column(name = "child_phenotype", nullable = false)
    private Grade childPhenotype;

    public BirthRecordPhenotype() {}

    public BirthRecordPhenotype(BirthRecord birthRecord, Category category) {
        this.birthRecord = birthRecord;
        this.category = category;
    }

    public BirthRecord getBirthRecord() {
        return birthRecord;
    }

    public void setBirthRecord(BirthRecord birthRecord) {
        this.birthRecord = birthRecord;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public Grade getParent1Phenotype() {
        return parent1Phenotype;
    }

    public void setParent1Phenotype(Grade parent1Phenotype) {
        this.parent1Phenotype = parent1Phenotype;
    }

    public Grade getParent2Phenotype() {
        return parent2Phenotype;
    }

    public void setParent2Phenotype(Grade parent2Phenotype) {
        this.parent2Phenotype = parent2Phenotype;
    }

    public Grade getChildPhenotype() {
        return childPhenotype;
    }

    public void setChildPhenotype(Grade childPhenotype) {
        this.childPhenotype = childPhenotype;
    }

    public void setAllPhenotypes(Grade parent1Phenotype, Grade parent2Phenotype, Grade childPhenotype) {
        this.parent1Phenotype = parent1Phenotype;
        this.parent2Phenotype = parent2Phenotype;
        this.childPhenotype = childPhenotype;
    }

    public void setAllPhenotypes(GradePair parentPhenotypes, Grade childPhenotype) {
        this.parent1Phenotype = parentPhenotypes.getFirst();
        this.parent2Phenotype = parentPhenotypes.getSecond();
        this.childPhenotype = childPhenotype;
    }
}
