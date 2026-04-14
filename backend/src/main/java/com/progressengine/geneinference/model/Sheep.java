package com.progressengine.geneinference.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.progressengine.geneinference.dto.SheepGenotypeDTO;
import com.progressengine.geneinference.model.enums.Allele;
import com.progressengine.geneinference.model.enums.Category;
import com.progressengine.geneinference.model.enums.DistributionType;
import com.progressengine.geneinference.service.InferenceMath;
import com.progressengine.geneinference.service.SheepService;
import com.progressengine.geneinference.service.AlleleDomains.AlleleDomain;
import com.progressengine.geneinference.service.AlleleDomains.CategoryDomains;

import jakarta.persistence.*;
import com.progressengine.geneinference.model.enums.Grade;

import java.util.*;

@Entity
@NamedEntityGraph(
        name = "Sheep.withDistributionsAndGenotypes",
        attributeNodes = {
                @NamedAttributeNode("distributions"),
                @NamedAttributeNode("genotypes"),
                @NamedAttributeNode("birthRecord")
        }
)
public class Sheep {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String name;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @OneToMany(mappedBy = "sheep", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<SheepGenotype> genotypes = new HashSet<>();

    @OneToMany(mappedBy = "sheep", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<SheepDistribution> distributions = new HashSet<>();

    @Transient
    private Map<Category, Map<DistributionType, Map<String, SheepDistribution>>> distributionsByCategory = new EnumMap<>(Category.class);

    @Transient
    private boolean organized = false;

    @OneToOne(mappedBy = "child", optional = true)
    private BirthRecord birthRecord;

    public Sheep() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BirthRecord getBirthRecord() {
        return birthRecord;
    }

    public void setBirthRecord(BirthRecord birthRecord) {
        this.birthRecord = birthRecord;
    }


    public void applyNewSheepRequest(
            Map<Category, SheepGenotypeDTO> genotypes,
            Map<Category, Map<String, Double>> distributions
    ) {
        setGenotypes(genotypes);                 // validates and stores observed genotype/phenotype data
        syncPriorsFromObservedGenotypes();       // derive priors from phenotype/genotype rules
        upsertDistributionsFromDTO(distributions); // user-provided distributions override where allowed
    }

    public void syncPriorsFromObservedGenotypes() {
        for (Category category : Category.values()) {
            syncPriorFromPhenotype(category);
        }
    }


    /** Genotypes ------------------------------------------------------------------------------ */
    public Map<Category, SheepGenotypeDTO> getGenotypes() {
        Map<Category, SheepGenotypeDTO> genotypesByCategory = new EnumMap<>(Category.class);

        for (SheepGenotype genotype : this.genotypes) {
            genotypesByCategory.put(
                    genotype.getCategory(),
                    new SheepGenotypeDTO(genotype.getPhenotypeCode(), genotype.getHiddenAlleleCode())
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
        throw new IllegalStateException(formatErrorMessage("No genotype found for category: " + category));
    }


    public <A extends Enum<A> & Allele> AllelePair<A> getGenotype(Category category) {
        return findSheepGenotype(category).getGenotype();
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


    public void setGenotypes(Map<Category, SheepGenotypeDTO> genotypesDTO) {
        // Validate all Grades are present
        Set<Category> missingCategories = EnumSet.allOf(Category.class);
        missingCategories.removeAll(genotypesDTO.keySet());

        if (!missingCategories.isEmpty()) {
            throw new IllegalArgumentException(formatErrorMessage("Missing categories in genotypesDTO: " + missingCategories));
        }

        for (Map.Entry<Category, SheepGenotypeDTO> entry : genotypesDTO.entrySet()) {
            Category category = entry.getKey();
            SheepGenotypeDTO genotypeDTO = entry.getValue();
            if (genotypeDTO == null) {
                throw new IllegalArgumentException(
                        formatErrorMessage("Genotype DTO for category " + category + " is null")
                );
            }
            if (genotypeDTO.phenotype() == null) {
                throw new IllegalArgumentException(formatErrorMessage("Phenotype of category " + category + " is null"));
            }
            SheepGenotype genotype = createIfAbsentSheepGenotype(category);
            AllelePair<?> pair = genotypeDTO.toAllelePair(category);
            genotype.setGenotype(pair);
        }
    }

    public void updateGenotypes(Map<Category, SheepGenotypeDTO> updatedGenotypes) {
        if (this.birthRecord != null) {
            Relationship parentRelationship = this.birthRecord.getParentRelationship();
            parentRelationship.updateChildPhenotypeFrequencies(this, updatedGenotypes);
        } else if (updatedGenotypes != null && !updatedGenotypes.isEmpty()) {
            for (Map.Entry<Category, SheepGenotypeDTO> entry : updatedGenotypes.entrySet()) {
                Category category = entry.getKey();
                AllelePair<?> genotype = entry.getValue().toAllelePair(category);
                createIfAbsentSheepGenotype(category).setGenotype(genotype);
            }
        }
    }

    public <A extends Enum<A> & Allele> void evolvePhenotype(Category category) {
        AlleleDomain<A> domain = CategoryDomains.typedDomainFor(category);
        SheepGenotype genotype = findSheepGenotype(category);
        A phenotype = genotype.getPhenotype();
        A newPhenotype = domain.evolvePhenotype(phenotype);
        genotype.setPhenotype(newPhenotype);
    }


    public <A extends Enum<A> & Allele> void setGenotype(Category category, AllelePair<A> genotype) {
        if (genotype == null) {
            throw new IllegalArgumentException(
                    formatErrorMessage("Genotype for category " + category + " is null")
            );
        }
        if (genotype.getFirst() == null) {
            throw new IllegalArgumentException(
                    formatErrorMessage("Phenotype for category " + category + " is null")
            );
        }

        AlleleDomain<A> domain = CategoryDomains.typedDomainFor(category);

        final A phenotype;
        final A hiddenAllele;

        try {
            phenotype = domain.parse(genotype.getFirst().code());
            hiddenAllele = genotype.getSecond() == null
                    ? null
                    : domain.parse(genotype.getSecond().code());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    formatErrorMessage("Genotype does not belong to category " + category + ": " + e.getMessage()),
                    e
            );
        }

        if (hiddenAllele != null && !domain.isHiddenAllelePossible(phenotype, hiddenAllele)) {
            throw new IllegalArgumentException(
                    formatErrorMessage(
                            "Hidden allele " + hiddenAllele.code()
                                    + " is not compatible with phenotype "
                                    + phenotype.code()
                                    + " for category " + category
                    )
            );
        }

        createIfAbsentSheepGenotype(category).setGenotype(phenotype, hiddenAllele);
    }


    public <A extends Enum<A> & Allele> void setGenotypeCodes(
        Category category,
        String phenotypeCode,
        String hiddenAlleleCode
    ) {
        if (phenotypeCode == null) {
            throw new IllegalArgumentException(
                    formatErrorMessage("Phenotype of category " + category + " is null")
            );
        }

        AlleleDomain<A> domain = CategoryDomains.typedDomainFor(category);
        A phenotype = domain.parse(phenotypeCode);
        A hiddenAllele = hiddenAlleleCode == null ? null : domain.parse(hiddenAlleleCode);

        setGenotype(category, new AllelePair<>(phenotype, hiddenAllele));
    }

    public <A extends Enum<A> & Allele> A getPhenotype(Category category) {
        SheepGenotype genotype = findSheepGenotype(category);
        return genotype.getPhenotype();
    }
    public <A extends Enum<A> & Allele> A getPhenotype(String categoryStr) {
        return getPhenotype(Category.valueOf(categoryStr));
    }


    public <A extends Enum<A> & Allele> void setPhenotype(Category category, A phenotype) {
        if (phenotype == null) {
            throw new IllegalArgumentException(
                    formatErrorMessage("Phenotype of category " + category + " is null")
            );
        }

        setPhenotypeValidated(
                CategoryDomains.typedDomainFor(category),
                category,
                phenotype
        );
    }

    private <A extends Enum<A> & Allele> void setPhenotypeValidated(
            AlleleDomain<A> domain,
            Category category,
            A phenotype
    ) {
        try {
            A parsedPhenotype = domain.parse(phenotype.code());

            SheepGenotype genotype = createIfAbsentSheepGenotype(category);
            A existingHidden = genotype.getHiddenAllele();

            if (existingHidden != null && !domain.isHiddenAllelePossible(parsedPhenotype, existingHidden)) {
                throw new IllegalArgumentException(
                        formatErrorMessage(
                                "Phenotype " + phenotype.code()
                                        + " is not compatible with existing hidden allele "
                                        + existingHidden.code()
                                        + " for category " + category
                        )
                );
            }

            genotype.setPhenotype(parsedPhenotype);

        } catch (IllegalArgumentException e) {
            if (e.getMessage() != null && e.getMessage().contains("not compatible")) {
                throw e;
            }

            throw new IllegalArgumentException(
                    formatErrorMessage("Phenotype " + phenotype.code() + " does not belong to category " + category),
                    e
            );
        }
    }


    public <A extends Enum<A> & Allele> void setPhenotypeCode(Category category, String phenotypeCode) {
        if (phenotypeCode == null) {
            throw new IllegalArgumentException(
                    formatErrorMessage("Phenotype of category " + category + " is null")
            );
        }

        AlleleDomain<A> domain = CategoryDomains.typedDomainFor(category);
        A phenotype = domain.parse(phenotypeCode);
        setPhenotype(category, phenotype);
    }

    public <A extends Enum<A> & Allele> A getHiddenAllele(Category category) {
        SheepGenotype genotype = findSheepGenotype(category);
        return genotype.getHiddenAllele();
    }
    public <A extends Enum<A> & Allele> A getHiddenAllele(String categoryStr) {
        return getHiddenAllele(Category.valueOf(categoryStr));
    }


    public <A extends Enum<A> & Allele> void setHiddenAllele(Category category, A hiddenAllele) {
        AlleleDomain<A> domain = CategoryDomains.typedDomainFor(category);

        A parsedHidden = null;
        if (hiddenAllele != null) {
            try {
                parsedHidden = domain.parse(hiddenAllele.code());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException(
                        formatErrorMessage(
                                "Hidden allele " + hiddenAllele.code() + " does not belong to category " + category
                        ),
                        e
                );
            }
        }

        SheepGenotype genotype = createIfAbsentSheepGenotype(category);
        A existingPhenotype = genotype.getPhenotype();

        if (existingPhenotype != null && parsedHidden != null
                && !domain.isHiddenAllelePossible(existingPhenotype, parsedHidden)) {
            throw new IllegalArgumentException(
                    formatErrorMessage(
                            "Hidden allele " + parsedHidden.code()
                                    + " is not compatible with phenotype "
                                    + existingPhenotype.code()
                                    + " for category " + category
                    )
            );
        }

        genotype.setHiddenAllele(parsedHidden);
    }


    public <A extends Enum<A> & Allele> void setHiddenAlleleCode(Category category, String hiddenCode) {
        if (hiddenCode == null) {
            setHiddenAllele(category, null);
            return;
        }

        AlleleDomain<A> domain = CategoryDomains.typedDomainFor(category);
        A hiddenAllele = domain.parse(hiddenCode);
        setHiddenAllele(category, hiddenAllele);
    }
    // -------------------------------------------------------------------------------------------



    /** SheepDistribution ------------------------------------------------------------------------- */
    public <A extends Enum<A> & Allele> void syncPriorFromPhenotype(Category category) {
        AlleleDomain<A> domain = CategoryDomains.typedDomainFor(category);

        SheepGenotype genotype = findSheepGenotype(category);

        A phenotype = genotype.getPhenotype();
        if (phenotype == null) {
            throw new IllegalStateException(
                    formatErrorMessage("Cannot sync prior: phenotype is null for category " + category)
            );
        }

        Map<A, Double> prior = domain.hiddenPriorGivenPhenotype(phenotype);
        setDistribution(category, DistributionType.PRIOR, prior);

        A forcedHidden = extractDeterministicAllele(prior);
        if (forcedHidden != null) {
            genotype.setHiddenAllele(forcedHidden);
        } else if (genotype.getHiddenAllele() != null
                && !domain.isHiddenAllelePossible(phenotype, genotype.getHiddenAllele())) {
            genotype.setHiddenAllele(null);
        }
    }

    private <A extends Enum<A> & Allele> A extractDeterministicAllele(Map<A, Double> distribution) {
        A result = null;

        for (Map.Entry<A, Double> entry : distribution.entrySet()) {
            double value = entry.getValue();
            if (Math.abs(value - 1.0) < 1e-9) {
                if (result != null) {
                    return null;
                }
                result = entry.getKey();
            } else if (value > 1e-9) {
                return null;
            }
        }

        return result;
    }

    private <A extends Enum<A> & Allele> void validateDistribution(
        Category category,
        Map<A, Double> distribution
    ) {
        if (distribution == null) {
            throw new IllegalArgumentException(
                    formatErrorMessage("Distribution for category " + category + " is null")
            );
        }

        AlleleDomain<A> domain = CategoryDomains.typedDomainFor(category);
        Set<A> expectedAlleles = EnumSet.allOf(domain.getAlleleType());
        Set<A> actualAlleles = distribution.keySet();

        Set<A> missingAlleles = EnumSet.copyOf(expectedAlleles);
        missingAlleles.removeAll(actualAlleles);
        if (!missingAlleles.isEmpty()) {
            throw new IllegalArgumentException(
                    formatErrorMessage("Missing entries in distribution for category " + category + ": " + missingAlleles)
            );
        }

        Set<A> extraAlleles = actualAlleles.isEmpty()
                ? EnumSet.noneOf(domain.getAlleleType())
                : EnumSet.copyOf(actualAlleles);
        extraAlleles.removeAll(expectedAlleles);
        if (!extraAlleles.isEmpty()) {
            throw new IllegalArgumentException(
                    formatErrorMessage("Invalid alleles in distribution for category " + category + ": " + extraAlleles)
            );
        }

        // Validate sum ≈ 1.0
        InferenceMath.validateDistribution(distribution);
    }

    public void organizeDistributions() {
        if (distributions == null) return;

        distributionsByCategory = new EnumMap<>(Category.class);

        for (SheepDistribution dist : distributions) {
            distributionsByCategory
                    .computeIfAbsent(dist.getCategory(), k -> new EnumMap<>(DistributionType.class))
                    .computeIfAbsent(dist.getDistributionType(), k -> new HashMap<>())
                    .put(dist.getAlleleCode(), dist);
        }
        organized = true;
    }


    private Map<String, SheepDistribution> getDistributionByCategoryAndType(Category category,  DistributionType distributionType) {
        if (!distributionsByCategory.containsKey(category)) {
            throw new IllegalArgumentException(formatErrorMessage("Category " + category + " does not exist in distribution map"));
        }
        Map<DistributionType, Map<String, SheepDistribution>> typeMap = distributionsByCategory.get(category);

        if (typeMap == null) {
            throw new IllegalArgumentException(formatErrorMessage(category + " is missing distribution-type map"));
        }
        if (!typeMap.containsKey(distributionType)) {
            throw new IllegalArgumentException(formatErrorMessage("Distribution-type map is missing key for category " + category + " and type " + distributionType));
        }
        if (typeMap.get(distributionType) == null) {
            throw new IllegalArgumentException(formatErrorMessage("Distribution for " + category + " -> " + distributionType + " is uninitialized"));
        }

        return typeMap.get(distributionType);
    }


    public Map<Category, Map<DistributionType, Map<String, Double>>> getAllDistributions() {
        Map<Category, Map<DistributionType, Map<String, Double>>> distributionsByCategoryDTO = new EnumMap<>(Category.class);

        for (SheepDistribution dist : distributions) {
            distributionsByCategoryDTO
                    .computeIfAbsent(dist.getCategory(), k -> new EnumMap<>(DistributionType.class))
                    .computeIfAbsent(dist.getDistributionType(), k -> new HashMap<>())
                    .put(dist.getAlleleCode(), dist.getProbability());
        }
        return distributionsByCategoryDTO;
    }


    public Map<Category, Map<String, Double>> getAllDistributionsByType(DistributionType distributionType) {
        Map<Category, Map<String, Double>> distributionsByTypeDTO = new EnumMap<>(Category.class);

        for (SheepDistribution dist : distributions) {
            if (dist.getDistributionType() != distributionType) { continue; }
            distributionsByTypeDTO
                    .computeIfAbsent(dist.getCategory(), k -> new HashMap<>())
                    .put(dist.getAlleleCode(), dist.getProbability());
        }
        return distributionsByTypeDTO;
    }


    public <A extends Enum<A> & Allele> Map<A, Double> getDistribution(
        Category category,
        DistributionType distributionType
    ) {
        if (!organized) {
            organizeDistributions();
        }

        AlleleDomain<A> domain = CategoryDomains.typedDomainFor(category);
        Map<String, SheepDistribution> distMap = getDistributionByCategoryAndType(category, distributionType);

        Map<A, Double> result = new EnumMap<>(domain.getAlleleType());
        for (A allele : domain.getAlleles()) {
            SheepDistribution dist = distMap.get(allele.code());
            result.put(allele, dist == null ? 0.0 : dist.getProbability());
        }

        return result;
    }


    public <A extends Enum<A> & Allele> Map<A, Double> getDistribution(String categoryStr, String distributionTypeStr) {
        return getDistribution(Category.valueOf(categoryStr), DistributionType.valueOf(distributionTypeStr));
    }


    private Map<String, SheepDistribution> createIfAbsentDistributionByCategoryAndType(Category category, DistributionType distributionType) {
        return distributionsByCategory
                .computeIfAbsent(category, k -> new EnumMap<>(DistributionType.class))
                .computeIfAbsent(distributionType, k -> new HashMap<>());
    }


    private <A extends Enum<A> & Allele> void upsertDistributionsByCategory(
        Category category,
        DistributionType distributionType,
        Map<A, Double> distribution
    ) {
        Map<String, SheepDistribution> distMap =
                createIfAbsentDistributionByCategoryAndType(category, distributionType);

        for (Map.Entry<A, Double> entry : distribution.entrySet()) {
            A allele = entry.getKey();
            Double probability = entry.getValue();

            SheepDistribution sheepDistribution = distMap.computeIfAbsent(
                    allele.code(),
                    code -> {
                        SheepDistribution newDist =
                                new SheepDistribution(this, category, distributionType, code);
                        distributions.add(newDist);
                        return newDist;
                    }
            );

            sheepDistribution.setProbability(probability);
        }
    }


    private boolean missingDistributionByCategory(Category category, DistributionType distributionType) {
        if (distributionsByCategory == null) organizeDistributions();
        if (distributionsByCategory.get(category) == null) return true;
        return !distributionsByCategory.get(category).containsKey(distributionType);
    }


    public <A extends Enum<A> & Allele> void setDistributionFromCodes(
        Category category,
        DistributionType distributionType,
        Map<String, Double> distributionByCode
    ) {
        if (distributionByCode == null) {
            throw new IllegalArgumentException(
                    formatErrorMessage("Distribution for category " + category + " is null")
            );
        }

        AlleleDomain<A> domain = CategoryDomains.typedDomainFor(category);
        Map<A, Double> typedDistribution = new EnumMap<>(domain.getAlleleType());

        for (Map.Entry<String, Double> entry : distributionByCode.entrySet()) {
            A allele = domain.parse(entry.getKey());
            typedDistribution.put(allele, entry.getValue());
        }

        setDistribution(category, distributionType, typedDistribution);
    }


    public <A extends Enum<A> & Allele> void setUniformDistribution(
        Category category,
        DistributionType distributionType
    ) {
        Map<A, Double> distribution = SheepService.createUniformDistribution(category);
        setDistribution(
            category,
            distributionType,
            distribution
        );
    }


    // Replaces the distributions by categories into this sheep and resets the rest
    public void replaceDistributionsFromDTO(Map<Category, Map<String, Double>> distributionsByCategoryDTO) {
        if (!organized) {
            organizeDistributions();
        }

        for (Category category : Category.values()) {
            if (distributionsByCategoryDTO != null && distributionsByCategoryDTO.containsKey(category)) {
                Map<String, Double> distribution = distributionsByCategoryDTO.get(category);
                setDistributionFromCodes(category, DistributionType.PRIOR, distribution);
                setDistributionFromCodes(category, DistributionType.INFERRED, distribution);
            } else {
                setUniformDistribution(category, DistributionType.PRIOR);
                setUniformDistribution(category, DistributionType.INFERRED);
            }
        }
    }


    public void setDistributionByType(Map<Category, Map<String, Double>> distributionsByCategoryDTO, DistributionType distributionType) {
        if (!organized) organizeDistributions();

        for (Category category : Category.values()) {
            if (distributionsByCategoryDTO != null && distributionsByCategoryDTO.containsKey(category)) {
                Map<String, Double> distribution = distributionsByCategoryDTO.get(category);
                setDistributionFromCodes(category, distributionType, distribution);
            } else {
                setUniformDistribution(category, distributionType);
            }
        }
    }


    // Upserts the partial distributions by categories into this sheep
    public void upsertDistributionsFromDTO(Map<Category, Map<String, Double>> distributionsByCategoryDTO) {
        // the value passed in might be null in which case follow next steps as if no category is passed
        if (!organized) organizeDistributions();

        for (Category category : Category.values()) {
            // if a category is passed in then the prior should always be overwritten however the inferred should only be set if it's not already
            if (distributionsByCategoryDTO != null && distributionsByCategoryDTO.containsKey(category)) {
                Map<String, Double> distribution = distributionsByCategoryDTO.get(category);
                setDistributionFromCodes(category, DistributionType.PRIOR, distribution);

                // if the sheep doesn't already have an inferred distribution, set it to the new prior
                if (missingDistributionByCategory(category, DistributionType.INFERRED)) {
                    setDistributionFromCodes(category, DistributionType.INFERRED, distribution);
                }
            } else if (missingDistributionByCategory(category, DistributionType.PRIOR)) {
                // if the category is not passed then it stays the same unless it is not set then it defaults to a uniform distribution
                setUniformDistribution(category, DistributionType.PRIOR);
                setUniformDistribution(category, DistributionType.INFERRED);
            }
        }
    }


    public void createDefaultDistributions() {
        if (!organized) organizeDistributions();

        for (Category category : Category.values()) {
            setUniformDistribution(category, DistributionType.PRIOR);
            setUniformDistribution(category, DistributionType.INFERRED);
        }
    }


    public <A extends Enum<A> & Allele> void setDistribution(Category category, DistributionType distributionType, Map<A, Double> distribution) {
        if (!organized) {
            organizeDistributions();
        }
        validateDistribution(category, distribution);
        upsertDistributionsByCategory(category, distributionType, distribution);
    }


    public void setDistribution(String categoryStr, String distributionTypeStr, Map<Grade, Double> distribution) {
        setDistribution(Category.valueOf(categoryStr), DistributionType.valueOf(distributionTypeStr), distribution);
    }


    @JsonIgnore
    public Relationship getParentRelationship() {
        if (birthRecord == null) return null;
        return birthRecord.getParentRelationship();
    }


//    public void setParentRelationship(Relationship parentRelationship) {
//        this.parentRelationship = parentRelationship;
//    }
    // -------------------------------------------------------------------------------------------


    private String formatErrorMessage(String specificMessage) {
        return String.format("Error in sheep with id %d: %s", this.id, specificMessage);
    }

}
