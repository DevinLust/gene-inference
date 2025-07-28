package com.progressengine.geneinference.model;

import com.progressengine.geneinference.dto.SheepGenotypeDTO;
import com.progressengine.geneinference.model.enums.Category;
import com.progressengine.geneinference.model.enums.DistributionType;
import jakarta.persistence.*;
import com.progressengine.geneinference.model.enums.Grade;

import java.util.*;
import java.util.stream.Collectors;

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

    @OneToMany(mappedBy = "sheep", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SheepGenotype> genotypes = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @MapKeyEnumerated(EnumType.STRING)
    @MapKeyColumn(name = "grade")         // Name of the key column (for Grade enum)
    @Column(name = "probability")           // Name of the value column (Integer)
    private Map<Grade, Double> hiddenDistribution;

    @ElementCollection
    @CollectionTable(name = "sheep_prior_distribution", joinColumns = @JoinColumn(name = "sheep_id"))
    @MapKeyEnumerated(EnumType.STRING)
    @MapKeyColumn(name = "grade")
    @Column(name = "probability")
    private Map<Grade, Double> priorDistribution;

    @OneToMany(mappedBy = "sheep", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    private List<SheepDistribution> distributions = new ArrayList<>();

    @Transient
    private Map<Category, Map<DistributionType, Map<Grade, SheepDistribution>>> distributionsByCategory = new EnumMap<>(Category.class);

    @Transient
    private boolean organized = false;

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

    // experimental List of Genotypes
    public Map<Category, SheepGenotypeDTO> getGenotypes() {
        Map<Category, SheepGenotypeDTO> genotypesByCategory = new EnumMap<>(Category.class);

        for (SheepGenotype genotype : this.genotypes) {
            genotypesByCategory.put(
                    genotype.getCategory(),
                    new SheepGenotypeDTO(genotype.getPhenotype(), genotype.getHiddenAllele())
            );
        }

        return genotypesByCategory;
    }

    private SheepGenotype findSheepGenotype(Category category) {
        for (SheepGenotype genotype : this.genotypes) {
            if (genotype.getCategory().equals(category)) {
                return genotype;
            }
        }
        throw new IllegalStateException("No genotype found for category: " + category);
    }

    public GradePair getGenotype(Category category) {
        return findSheepGenotype(category).getGenotype();
    }
    public GradePair getGenotype(String categoryStr) {
        return getGenotype(Category.valueOf(categoryStr));
    }

    public void setGenotype(Category category, GradePair genotype) {
        findSheepGenotype(category).setGenotype(genotype);
    }
    public void setGenotype(String categoryStr, GradePair genotype) {
        setGenotype(Category.valueOf(categoryStr), genotype);
    }

    public Grade getPhenotype(Category category) {
        return findSheepGenotype(category).getPhenotype();
    }
    public Grade getPhenotype(String categoryStr) {
        return getPhenotype(Category.valueOf(categoryStr));
    }

    public void setPhenotype(Category category, Grade phenotype) {
        findSheepGenotype(category).setPhenotype(phenotype);
    }
    public void setPhenotype(String categoryStr, Grade phenotype) {
        setPhenotype(Category.valueOf(categoryStr), phenotype);
    }

    public Grade getHiddenAllele(Category category) {
        return findSheepGenotype(category).getHiddenAllele();
    }
    public Grade getHiddenAllele(String categoryStr) {
        return getHiddenAllele(Category.valueOf(categoryStr));
    }

    public void setHiddenAllele(Category category, Grade hiddenAllele) {
        findSheepGenotype(category).setHiddenAllele(hiddenAllele);
    }
    public void setHiddenAllele(String categoryStr, Grade hiddenAllele) {
        setHiddenAllele(Category.valueOf(categoryStr), hiddenAllele);
    }

    public Map<Grade, Double> getHiddenDistribution() {
        return hiddenDistribution;
    }

    public void setHiddenDistribution(Map<Grade, Double> hiddenDistribution) {
        this.hiddenDistribution = hiddenDistribution;
    }

    public Map<Grade, Double> getPriorDistribution() {
        return priorDistribution;
    }

    public void setPriorDistribution(Map<Grade, Double> priorDistribution) {
        this.priorDistribution = priorDistribution;
    }

    // experimental List of SheepDistribution
    @PostLoad
    public void organizeDistributions() {
        if (distributions == null) return;

        distributionsByCategory = new EnumMap<>(Category.class);

        System.out.println(distributions.size());

        for (SheepDistribution dist : distributions) {
            distributionsByCategory
                    .computeIfAbsent(dist.getCategory(), k -> new EnumMap<>(DistributionType.class))
                    .computeIfAbsent(dist.getDistributionType(), k -> new EnumMap<>(Grade.class))
                    .put(dist.getGrade(), dist);
        }
        organized = true;
    }

    private Map<Grade, SheepDistribution> getDistributionByCategoryAndType(Category category,  DistributionType distributionType) {
        Map<DistributionType, Map<Grade, SheepDistribution>> typeMap = distributionsByCategory.get(category);

        if (typeMap == null || !typeMap.containsKey(distributionType)) {
            throw new IllegalStateException("Distribution not initialized for category " + category + " and type " + distributionType);
        }

        return typeMap.get(distributionType);
    }

    public Map<Grade, Double> getDistribution(Category category, DistributionType distributionType) {
        if (!organized) {
            organizeDistributions();
        }

        Map<Grade, SheepDistribution> distMap = getDistributionByCategoryAndType(category, distributionType);

        return distMap.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().getProbability()));
    }

    public Map<Grade, Double> getDistribution(String categoryStr, String distributionTypeStr) {
        return getDistribution(Category.valueOf(categoryStr), DistributionType.valueOf(distributionTypeStr));
    }

    private Map<Grade, SheepDistribution> createIfAbsentDistributionByCategoryAndType(Category category, DistributionType distributionType) {
        return distributionsByCategory
                .computeIfAbsent(category, k -> new EnumMap<>(DistributionType.class))
                .computeIfAbsent(distributionType, k -> new EnumMap<>(Grade.class));
    }

    public void setDistribution(Category category, DistributionType distributionType, Map<Grade, Double> distribution) {
        if (!organized) {
            organizeDistributions();
        }

        // Validate all Grades are present
        Set<Grade> missingGrades = EnumSet.allOf(Grade.class);
        missingGrades.removeAll(distribution.keySet());

        if (!missingGrades.isEmpty()) {
            throw new IllegalArgumentException("Missing grades in distribution: " + missingGrades);
        }

        // Validate sum ≈ 1.0
        double total = distribution.values().stream()
                .mapToDouble(Double::doubleValue)
                .sum();

        if (Math.abs(total - 1.0) > 1e-6) {
            throw new IllegalArgumentException("Distribution probabilities must sum to 1.0 (±1e-6). Actual sum: " + total);
        }

        Map<Grade, SheepDistribution> distMap = createIfAbsentDistributionByCategoryAndType(category, distributionType);

        // set the associated SheepDistribution to the new probability
        for  (Map.Entry<Grade, Double> entry : distribution.entrySet()) {
            Grade key = entry.getKey();
            Double value = entry.getValue();
            SheepDistribution sheepDistribution = distMap.computeIfAbsent(
                    key,
                    k -> {
                        SheepDistribution newDist = new SheepDistribution(this, category, distributionType, k);
                        distributions.add(newDist); // Ensure it's part of the entity list
                        return newDist;
                    }
            );
            sheepDistribution.setProbability(value);
        }
    }

    public void setDistribution(String categoryStr, String distributionTypeStr, Map<Grade, Double> distribution) {
        setDistribution(Category.valueOf(categoryStr), DistributionType.valueOf(distributionTypeStr), distribution);
    }

    public Relationship getParentRelationship() {
        return parentRelationship;
    }

    public void setParentRelationship(Relationship parentRelationship) {
        this.parentRelationship = parentRelationship;
    }
}
