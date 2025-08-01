package com.progressengine.geneinference.model;

import com.progressengine.geneinference.model.enums.Category;
import com.progressengine.geneinference.model.enums.Grade;
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

    @Enumerated(EnumType.STRING)
    @Column(name = "phenotype")
    private Grade phenotype;

    @Enumerated(EnumType.STRING)
    @Column(name = "hidden")
    private Grade hiddenAllele;

    public SheepGenotype() {}

    public SheepGenotype(Sheep sheep, Category category) {
        this.sheep = sheep;
        this.category = category;
    }

    // Getters and Setters
    public Sheep getSheep() { return sheep; }
    public void setSheep(Sheep sheep) { this.sheep = sheep; }

    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }

    public Grade getPhenotype() { return phenotype; }
    public void setPhenotype(Grade phenotype) { this.phenotype = phenotype; }

    public Grade getHiddenAllele() { return hiddenAllele; }
    public void setHiddenAllele(Grade hiddenAllele) { this.hiddenAllele = hiddenAllele; }

    public GradePair getGenotype() { return new GradePair(this.phenotype, this.hiddenAllele); }
    public void setGenotype(GradePair genotype) {
        this.phenotype = genotype.getFirst();
        this.hiddenAllele = genotype.getSecond();
    }
    public void setGenotype(Grade phenotype, Grade hiddenAllele) {
        this.phenotype = phenotype;
        this.hiddenAllele = hiddenAllele;
    }
}

