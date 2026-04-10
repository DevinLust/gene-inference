package com.progressengine.geneinference.model;

import com.progressengine.geneinference.model.enums.Allele;
import com.progressengine.geneinference.model.enums.Category;
import com.progressengine.geneinference.service.AlleleDomains.AlleleDomain;
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

    @Column(name = "parent1_phenotype", nullable = false)
    private String parent1PhenotypeCode;

    @Column(name = "parent2_phenotype", nullable = false)
    private String parent2PhenotypeCode;

    @Column(name = "child_phenotype", nullable = false)
    private String childPhenotypeCode;

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

    public String getParent1PhenotypeCode() {
        return parent1PhenotypeCode;
    }

    public void setParent1PhenotypeCode(String parent1PhenotypeCode) {
        this.parent1PhenotypeCode = parent1PhenotypeCode;
    }

    public String getParent2PhenotypeCode() {
        return parent2PhenotypeCode;
    }

    public void setParent2PhenotypeCode(String parent2PhenotypeCode) {
        this.parent2PhenotypeCode = parent2PhenotypeCode;
    }

    public String getChildPhenotypeCode() {
        return childPhenotypeCode;
    }

    public void setChildPhenotypeCode(String childPhenotypeCode) {
        this.childPhenotypeCode = childPhenotypeCode;
    }

    public <A extends Enum<A> & Allele> A getParent1Phenotype(AlleleDomain<A> domain) {
        return domain.parse(parent1PhenotypeCode);
    }

    public <A extends Enum<A> & Allele> void setParent1Phenotype(A parent1Phenotype) {
        if (parent1Phenotype == null) {
            throw new IllegalArgumentException("Parent 1 phenotype cannot be null");
        }
        this.parent1PhenotypeCode = parent1Phenotype.code();
    }

    public <A extends Enum<A> & Allele> A getParent2Phenotype(AlleleDomain<A> domain) {
        return domain.parse(parent2PhenotypeCode);
    }

    public <A extends Enum<A> & Allele> void setParent2Phenotype(A parent2Phenotype) {
        if (parent2Phenotype == null) {
            throw new IllegalArgumentException("Parent 2 phenotype cannot be null");
        }
        this.parent2PhenotypeCode = parent2Phenotype.code();
    }

    public <A extends Enum<A> & Allele> A getChildPhenotype(AlleleDomain<A> domain) {
        return domain.parse(childPhenotypeCode);
    }

    public <A extends Enum<A> & Allele> void setChildPhenotype(A childPhenotype) {
        if (childPhenotype == null) {
            throw new IllegalArgumentException("Child phenotype cannot be null");
        }
        this.childPhenotypeCode = childPhenotype.code();
    }

    public <A extends Enum<A> & Allele> void setAllPhenotypes(A parent1Phenotype, A parent2Phenotype, A childPhenotype) {
        if (parent1Phenotype == null || parent2Phenotype == null || childPhenotype == null) {
            throw new IllegalArgumentException("Phenotypes cannot be null");
        }

        this.parent1PhenotypeCode = parent1Phenotype.code();
        this.parent2PhenotypeCode = parent2Phenotype.code();
        this.childPhenotypeCode = childPhenotype.code();
    }

    public <A extends Enum<A> & Allele> void setAllPhenotypes(AllelePair<A> parentPhenotypes, A childPhenotype) {
        if (parentPhenotypes == null) {
            throw new IllegalArgumentException("Parent phenotypes cannot be null");
        }
        setAllPhenotypes(parentPhenotypes.getFirst(), parentPhenotypes.getSecond(), childPhenotype);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BirthRecordPhenotype that)) return false;
        return Objects.equals(birthRecord != null ? birthRecord.getId() : null, that.birthRecord != null ? that.birthRecord.getId() : null)
                && category == that.category;
    }

    @Override
    public int hashCode() {
        return Objects.hash(birthRecord != null ? birthRecord.getId() : null, category);
    }
}
