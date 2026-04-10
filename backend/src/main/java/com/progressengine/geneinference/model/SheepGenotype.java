package com.progressengine.geneinference.model;

import com.progressengine.geneinference.model.enums.Allele;
import com.progressengine.geneinference.model.enums.Category;
import com.progressengine.geneinference.service.AlleleDomains.AlleleDomain;
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
        this.category = category;
    }

    public String getPhenotypeCode() {
        return phenotypeCode;
    }

    public void setPhenotypeCode(String phenotypeCode) {
        this.phenotypeCode = phenotypeCode;
    }

    public String getHiddenAlleleCode() {
        return hiddenAlleleCode;
    }

    public void setHiddenAlleleCode(String hiddenAlleleCode) {
        this.hiddenAlleleCode = hiddenAlleleCode;
    }

    public <A extends Enum<A> & Allele> A getPhenotype(AlleleDomain<A> domain) {
        return phenotypeCode == null ? null : domain.parse(phenotypeCode);
    }

    public <A extends Enum<A> & Allele> void setPhenotype(A phenotype) {
        this.phenotypeCode = phenotype == null ? null : phenotype.code();
    }

    public <A extends Enum<A> & Allele> A getHiddenAllele(AlleleDomain<A> domain) {
        return hiddenAlleleCode == null ? null : domain.parse(hiddenAlleleCode);
    }

    public <A extends Enum<A> & Allele> void setHiddenAllele(A hiddenAllele) {
        this.hiddenAlleleCode = hiddenAllele == null ? null : hiddenAllele.code();
    }

    public <A extends Enum<A> & Allele> AllelePair<A> getGenotype(AlleleDomain<A> domain) {
        return new AllelePair<>(
                getPhenotype(domain),
                getHiddenAllele(domain)
        );
    }

    public <A extends Enum<A> & Allele> void setGenotype(AllelePair<A> genotype) {
        if (genotype == null) {
            throw new IllegalArgumentException("Genotype cannot be null");
        }
        if (genotype.getFirst() == null) {
            throw new IllegalArgumentException("Phenotype cannot be null");
        }

        this.phenotypeCode = genotype.getFirst().code();
        this.hiddenAlleleCode = genotype.getSecond() == null ? null : genotype.getSecond().code();
    }

    public <A extends Enum<A> & Allele> void setGenotype(A phenotype, A hiddenAllele) {
        if (phenotype == null) {
            throw new IllegalArgumentException("Phenotype cannot be null");
        }

        this.phenotypeCode = phenotype.code();
        this.hiddenAlleleCode = hiddenAllele == null ? null : hiddenAllele.code();
    }
}
