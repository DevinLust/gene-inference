package com.progressengine.geneinference.model;

import com.progressengine.geneinference.dto.SheepGenotypeDTO;
import com.progressengine.geneinference.model.enums.Category;
import com.progressengine.geneinference.model.enums.DistributionType;
import com.progressengine.geneinference.service.SheepService;
import jakarta.persistence.*;
import com.progressengine.geneinference.model.enums.Grade;
import jakarta.transaction.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Entity
public class Sheep {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String name;

    @OneToMany(mappedBy = "sheep", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SheepGenotype> genotypes = new ArrayList<>();

    @OneToMany(mappedBy = "sheep", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    private List<SheepDistribution> distributions = new ArrayList<>();

    @Transient
    private Map<Category, Map<DistributionType, Map<Grade, SheepDistribution>>> distributionsByCategory = new EnumMap<>(Category.class);

    @Transient
    private boolean organized = false;

    @ManyToOne
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

    private SheepGenotype createIfAbsentSheepGenotype(Category category) {
        for (SheepGenotype genotype : this.genotypes) {
            if (genotype.getCategory().equals(category)) {
                return genotype;
            }
        }
        SheepGenotype newGenotype = new SheepGenotype(this, category);
        this.genotypes.add(newGenotype);
        return newGenotype;
    }

    @Transactional
    public void setGenotypes(Map<Category, SheepGenotypeDTO> genotypesDTO) {
        // Validate all Grades are present
        Set<Category> missingCategories = EnumSet.allOf(Category.class);
        missingCategories.removeAll(genotypesDTO.keySet());

        if (!missingCategories.isEmpty()) {
            throw new IllegalArgumentException("Missing categories in genotypesDTO: " + missingCategories);
        }

        for (Map.Entry<Category, SheepGenotypeDTO> entry : genotypesDTO.entrySet()) {
            Category category = entry.getKey();
            SheepGenotypeDTO genotypeDTO = entry.getValue();
            if (genotypeDTO.getPhenotype() == null) {
                throw new IllegalArgumentException("Phenotype of category " + category + " is null");
            }
            SheepGenotype genotype = createIfAbsentSheepGenotype(category);
            genotype.setGenotype(genotypeDTO.getPhenotype(),  genotypeDTO.getHiddenAllele());
        }
    }

    @Transactional
    public void setGenotype(Category category, GradePair genotype) {
        createIfAbsentSheepGenotype(category).setGenotype(genotype);
    }
    @Transactional
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
        createIfAbsentSheepGenotype(category).setPhenotype(phenotype);
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
        createIfAbsentSheepGenotype(category).setHiddenAllele(hiddenAllele);
    }
    public void setHiddenAllele(String categoryStr, Grade hiddenAllele) {
        setHiddenAllele(Category.valueOf(categoryStr), hiddenAllele);
    }

    // experimental List of SheepDistribution
    private void validateDistribution(Map<Grade, Double> distribution) {
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
    }

    @PostLoad
    public void organizeDistributions() {
        if (distributions == null) return;

        distributionsByCategory = new EnumMap<>(Category.class);

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

    public Map<Category, Map<DistributionType, Map<Grade, Double>>> getAllDistributions() {
        Map<Category, Map<DistributionType, Map<Grade, Double>>> distributionsByCategoryDTO = new EnumMap<>(Category.class);

        for (SheepDistribution dist : distributions) {
            distributionsByCategoryDTO
                    .computeIfAbsent(dist.getCategory(), k -> new EnumMap<>(DistributionType.class))
                    .computeIfAbsent(dist.getDistributionType(), k -> new EnumMap<>(Grade.class))
                    .put(dist.getGrade(), dist.getProbability());
        }
        return distributionsByCategoryDTO;
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

    private void upsertDistributionsByCategory(Category category, DistributionType distributionType, Map<Grade, Double> distribution) {
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

    private boolean missingDistributionByCategory(Category category, DistributionType distributionType) {
        if (distributionsByCategory == null) organizeDistributions();
        if (distributionsByCategory.get(category) == null) return true;
        return !distributionsByCategory.get(category).containsKey(distributionType);
    }

    // Upserts the partial distributions by categories into this sheep
    @Transactional
    public void upsertDistributionsFromDTO(Map<Category, Map<Grade, Double>> distributionsByCategoryDTO) {
        // the value passed in might be null in which case follow next steps as if no category is passed
        if (!organized) organizeDistributions();

        for (Category category : Category.values()) {
            // if a category is passed in then the prior should always be overwritten however the inferred should only be set if it's not already
            if (distributionsByCategoryDTO != null && distributionsByCategoryDTO.containsKey(category)) {
                Map<Grade, Double> distribution = distributionsByCategoryDTO.get(category);
                setDistribution(category, DistributionType.PRIOR, distribution);
                if (missingDistributionByCategory(category, DistributionType.INFERRED)) {
                    setDistribution(category, DistributionType.INFERRED, distribution);
                }
            } else if (missingDistributionByCategory(category, DistributionType.PRIOR)) {
                // if the category is not passed then it stays the same unless it is not set then it defaults to a uniform distribution
                setDistribution(category, DistributionType.PRIOR, SheepService.createUniformDistribution());
                setDistribution(category, DistributionType.INFERRED, SheepService.createUniformDistribution());
            }
        }
    }

    @Transactional
    public void createDefaultDistributions() {
        if (!organized) organizeDistributions();

        for (Category category : Category.values()) {
            setDistribution(category, DistributionType.PRIOR, SheepService.createUniformDistribution());
            setDistribution(category, DistributionType.INFERRED, SheepService.createUniformDistribution());
        }
    }

    @Transactional
    public void setDistribution(Category category, DistributionType distributionType, Map<Grade, Double> distribution) {
        if (!organized) {
            organizeDistributions();
        }

        validateDistribution(distribution);

        upsertDistributionsByCategory(category, distributionType, distribution);
    }

    @Transactional
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
