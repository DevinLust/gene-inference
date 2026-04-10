package com.progressengine.geneinference.model;

import com.progressengine.geneinference.model.enums.Allele;
import com.progressengine.geneinference.model.enums.Category;
import com.progressengine.geneinference.service.AlleleDomains.AlleleDomain;
import com.progressengine.geneinference.service.AlleleDomains.CategoryDomains;

import jakarta.persistence.*;

@Entity
@IdClass(SheepGenotypeKey.class)
@Table(name = "sheep_genotype")
public class SheepGenotype {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sheep_id", nullable = false)
    private Sheep sheep;

    @Id
    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    private Category category;

    @Column(name = "phenotype")
    private String phenotypeCode;

    @Column(name = "hidden")
    private String hiddenAlleleCode;

    public SheepGenotype() {}

    public SheepGenotype(Sheep sheep, Category category) {
        this.sheep = sheep;
        this.category = category;
    }

    public Sheep getSheep() {
        return sheep;
    }

    public void setSheep(Sheep sheep) {
        this.sheep = sheep;
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

        AlleleDomain<?> newDomain = CategoryDomains.domainFor(category);

        if (phenotypeCode != null) {
            newDomain.parse(phenotypeCode);
        }
        if (hiddenAlleleCode != null) {
            newDomain.parse(hiddenAlleleCode);
        }

        this.category = category;
    }

    public void setCategoryAndGenotypeCodes(Category category, String phenotypeCode, String hiddenAlleleCode) {
        if (category == null) {
            throw new IllegalArgumentException("Category cannot be null");
        }

        AlleleDomain<?> newDomain = CategoryDomains.domainFor(category);

        if (phenotypeCode == null) {
            throw new IllegalArgumentException("Phenotype code cannot be null");
        }

        newDomain.parse(phenotypeCode);
        if (hiddenAlleleCode != null) {
            newDomain.parse(hiddenAlleleCode);
        }

        this.category = category;
        this.phenotypeCode = phenotypeCode;
        this.hiddenAlleleCode = hiddenAlleleCode;
    }

    public String getPhenotypeCode() {
        return phenotypeCode;
    }

    public void setPhenotypeCode(String phenotypeCode) {
        if (phenotypeCode == null) {
            throw new IllegalArgumentException("Phenotype code cannot be null");
        }
        domain().parse(phenotypeCode);
        this.phenotypeCode = phenotypeCode;
    }

    public String getHiddenAlleleCode() {
        return hiddenAlleleCode;
    }

    public void setHiddenAlleleCode(String hiddenAlleleCode) {
        if (hiddenAlleleCode == null) {
            this.hiddenAlleleCode = null;
            return;
        }
        domain().parse(hiddenAlleleCode);
        this.hiddenAlleleCode = hiddenAlleleCode;
    }

    public void setGenotypeCodes(String phenotypeCode, String hiddenAlleleCode) {
        setPhenotypeCode(phenotypeCode);
        setHiddenAlleleCode(hiddenAlleleCode);
    }

    public <A extends Enum<A> & Allele> A getPhenotype() {
        requireCategorySet();
        AlleleDomain<A> domain = domain();
        return domain.parse(phenotypeCode);
    }

    public <A extends Enum<A> & Allele> void setPhenotype(A phenotype) {
        validateTypedAllele(phenotype, "Phenotype", false);
        this.phenotypeCode = phenotype.code();
    }

    public <A extends Enum<A> & Allele> A getHiddenAllele() {
        if (hiddenAlleleCode == null) {
            return null;
        }
        requireCategorySet();
        AlleleDomain<A> domain = domain();
        return domain.parse(hiddenAlleleCode);
    }

    public <A extends Enum<A> & Allele> void setHiddenAllele(A hiddenAllele) {
        validateTypedAllele(hiddenAllele, "Hidden allele", true);
        this.hiddenAlleleCode = hiddenAllele == null ? null : hiddenAllele.code();
    }

    public <A extends Enum<A> & Allele> AllelePair<A> getGenotype() {
        A phen = getPhenotype();
        A hidden = getHiddenAllele();
        return new AllelePair<>(phen, hidden);
    }

    public <A extends Enum<A> & Allele> void setGenotype(AllelePair<A> genotype) {
        if (genotype == null) {
            throw new IllegalArgumentException("Genotype cannot be null");
        }
        setPhenotype(genotype.getFirst());
        setHiddenAllele(genotype.getSecond());
    }

    public <A extends Enum<A> & Allele> void setGenotype(A phenotype, A hiddenAllele) {
        setPhenotype(phenotype);
        setHiddenAllele(hiddenAllele);
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
    private <A extends Enum<A> & Allele> void validateTypedAllele(A allele, String fieldName, boolean allowNull) {
        requireCategorySet();

        if (allele == null) {
            if (allowNull) {
                return;
            }
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
}
