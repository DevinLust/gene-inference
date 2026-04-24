package com.progressengine.geneinference.model;

import com.progressengine.geneinference.model.enums.Allele;
import com.progressengine.geneinference.model.enums.Category;
import com.progressengine.geneinference.service.AlleleDomains.AlleleDomain;
import com.progressengine.geneinference.service.AlleleDomains.CategoryDomains;
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
        if (category == null) {
            throw new IllegalArgumentException("Category cannot be null");
        }

        if (this.category == category) {
            return;
        }

        validateCodesForCategory(
                category,
                parent1PhenotypeCode,
                parent2PhenotypeCode,
                childPhenotypeCode
        );

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

    public <A extends Enum<A> & Allele> A getParent1Phenotype() {
        requireCategorySet();
        AlleleDomain<A> domain = domain();
        return domain.parse(parent1PhenotypeCode);
    }

    public <A extends Enum<A> & Allele> void setParent1Phenotype(A parent1Phenotype) {
        validateTypedAllele(parent1Phenotype, "Parent 1 phenotype");
        this.parent1PhenotypeCode = parent1Phenotype.code();
    }

    public <A extends Enum<A> & Allele> A getParent2Phenotype() {
        requireCategorySet();
        AlleleDomain<A> domain = domain();
        return domain.parse(parent2PhenotypeCode);
    }

    public <A extends Enum<A> & Allele> void setParent2Phenotype(A parent2Phenotype) {
        validateTypedAllele(parent2Phenotype, "Parent 2 phenotype");
        this.parent2PhenotypeCode = parent2Phenotype.code();
    }

    public <A extends Enum<A> & Allele> A getChildPhenotype() {
        requireCategorySet();
        AlleleDomain<A> domain = domain();
        return domain.parse(childPhenotypeCode);
    }

    public <A extends Enum<A> & Allele> void setChildPhenotype(A childPhenotype) {
        validateTypedAllele(childPhenotype, "Child phenotype");
        this.childPhenotypeCode = childPhenotype.code();
    }

    public <A extends Enum<A> & Allele> void setAllPhenotypes(A parent1Phenotype, A parent2Phenotype, A childPhenotype) {
        if (parent1Phenotype == null || parent2Phenotype == null || childPhenotype == null) {
            throw new IllegalArgumentException("Phenotypes cannot be null");
        }
        validateTypedAllele(parent1Phenotype, "Parent 1 phenotype");
        validateTypedAllele(parent2Phenotype, "Parent 2 phenotype");
        validateTypedAllele(childPhenotype, "Child phenotype");

        setAllPhenotypeCodes(parent1Phenotype.code(), parent2Phenotype.code(), childPhenotype.code());
    }

    public <A extends Enum<A> & Allele> void setAllPhenotypes(AllelePair<A> parentPhenotypes, A childPhenotype) {
        if (parentPhenotypes == null) {
            throw new IllegalArgumentException("Parent phenotypes cannot be null");
        }
        setAllPhenotypes(parentPhenotypes.getFirst(), parentPhenotypes.getSecond(), childPhenotype);
    }

    public <A extends Enum<A> & Allele> void setAllPhenotypeCodes(String parent1PhenotypeCode, String parent2PhenotypeCode, String childPhenotypeCode) {
        requireCategorySet();
        if (parent1PhenotypeCode == null || parent2PhenotypeCode == null) {
            throw new IllegalArgumentException("Parent phenotypes cannot be null");
        }
        if (childPhenotypeCode == null) {
            throw new IllegalArgumentException("Child phenotype cannot be null");
        }
        domain().parse(parent1PhenotypeCode);
        domain().parse(parent2PhenotypeCode);
        domain().parse(childPhenotypeCode);

        this.parent1PhenotypeCode = parent1PhenotypeCode;
        this.parent2PhenotypeCode = parent2PhenotypeCode;
        this.childPhenotypeCode = childPhenotypeCode;
    }

    public <A extends Enum<A> & Allele> void setAllPhenotypeCodes(AlleleCodePair parentPhenotypeCodes, String childPhenotypeCode) {
        requireCategorySet();
        if (parentPhenotypeCodes == null) {
            throw new IllegalArgumentException("Parent phenotypes cannot be null");
        }
        
        setAllPhenotypeCodes(parentPhenotypeCodes.first(), parentPhenotypeCodes.second(), childPhenotypeCode);
    }

    public void setCategoryAndPhenotypeCodes(
        Category category,
        AlleleCodePair parentPhenotypeCodes,
        String childPhenotypeCode
    ) {
        if (category == null) {
            throw new IllegalArgumentException("Category cannot be null");
        }
        if (parentPhenotypeCodes == null) {
            throw new IllegalArgumentException("Parent phenotype codes cannot be null");
        }
        if (childPhenotypeCode == null) {
            throw new IllegalArgumentException("Child phenotype code cannot be null");
        }

        validateCodesForCategory(
                category,
                parentPhenotypeCodes.first(),
                parentPhenotypeCodes.second(),
                childPhenotypeCode
        );

        this.category = category;
        this.parent1PhenotypeCode = parentPhenotypeCodes.first();
        this.parent2PhenotypeCode = parentPhenotypeCodes.second();
        this.childPhenotypeCode = childPhenotypeCode;
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

    private <A extends Enum<A> & Allele> AlleleDomain<A> domain() {
        return CategoryDomains.typedDomainFor(category);
    }

    private void requireCategorySet() {
        if (category == null) {
            throw new IllegalStateException("Category must be set before reading or writing phenotypes");
        }
    }

    // checks the Allele is actually in the AlleleDomain for this category
    private <A extends Enum<A> & Allele> void validateTypedAllele(A allele, String fieldName) {
        requireCategorySet();

        if (allele == null) {
            throw new IllegalArgumentException(fieldName + " cannot be null");
        }

        AlleleDomain<A> domain = domain();
        Class<A> expectedType = domain.getAlleleType();
        if (!expectedType.isInstance(allele)) {
            throw new IllegalArgumentException(
                    fieldName + " " + allele + " does not belong to category " + category +
                    ". Expected allele type: " + expectedType.getSimpleName()
            );
        }

        A parsed = domain.parse(allele.code());
        if (parsed != allele) {
            throw new IllegalArgumentException(
                    fieldName + " " + allele + " is not a supported allele for category " + category
            );
        }
    }

    private void validateCodesForCategory(
        Category category,
        String parent1Code,
        String parent2Code,
        String childCode
    ) {
        AlleleDomain<?> domain = CategoryDomains.domainFor(category);

        if (parent1Code != null) {
            domain.parse(parent1Code);
        }
        if (parent2Code != null) {
            domain.parse(parent2Code);
        }
        if (childCode != null) {
            domain.parse(childCode);
        }
    }
}
