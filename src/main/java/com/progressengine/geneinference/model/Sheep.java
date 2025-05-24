package com.progressengine.geneinference.model;

import jakarta.persistence.*;
import com.progressengine.geneinference.model.enums.Grade;

import java.util.Map;

@Entity
public class Sheep {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "phenotype")
    private Grade phenotype;

    @Enumerated(EnumType.STRING)
    @Column(name = "hidden_allele")
    private Grade hiddenAllele;

    @ElementCollection(fetch = FetchType.EAGER)
    @MapKeyEnumerated(EnumType.STRING)
    @MapKeyColumn(name = "grade")         // Name of the key column (for Grade enum)
    @Column(name = "probability")           // Name of the value column (Integer)
    private Map<Grade, Double> hiddenDistribution;

    @OneToOne
    @JoinColumn(name = "parent_relationship_id")
    private Relationship parentRelationship; // foreign key to Relationship

    public Sheep() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Grade getPhenotype() {
        return phenotype;
    }

    public void setPhenotype(Grade phenotype) {
        this.phenotype = phenotype;
    }

    public Grade getHiddenAllele() {
        return hiddenAllele;
    }

    public void setHiddenAllele(Grade hiddenAllele) {
        this.hiddenAllele = hiddenAllele;
    }

    public Map<Grade, Double> getHiddenDistribution() {
        return hiddenDistribution;
    }

    public void setHiddenDistribution(Map<Grade, Double> hiddenDistribution) {
        this.hiddenDistribution = hiddenDistribution;
    }

    public Relationship getParentRelationship() {
        return parentRelationship;
    }

    public void setParentRelationship(Relationship parentRelationship) {
        this.parentRelationship = parentRelationship;
    }
}
