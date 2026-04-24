package com.progressengine.geneinference.service;

import com.progressengine.geneinference.dto.*;
import com.progressengine.geneinference.exception.ResourceNotFoundException;
import com.progressengine.geneinference.mapper.DomainMapper;
import com.progressengine.geneinference.model.AllelePair;
import com.progressengine.geneinference.model.BirthRecord;
import com.progressengine.geneinference.model.Relationship;
import com.progressengine.geneinference.model.Sheep;
import com.progressengine.geneinference.model.enums.Allele;
import com.progressengine.geneinference.model.enums.Category;
import com.progressengine.geneinference.model.enums.DistributionType;
import com.progressengine.geneinference.model.enums.Grade;
import com.progressengine.geneinference.repository.SheepRepository;
import com.progressengine.geneinference.service.AlleleDomains.AlleleDomain;
import com.progressengine.geneinference.service.AlleleDomains.CategoryDomains;

import com.progressengine.geneinference.service.AlleleDomains.GradeAlleleDomain;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class SheepService {

    private final SheepRepository sheepRepository;
    private final RelationshipService relationshipService;

    public static Map<Grade, Double> createUniformDistribution() {
        Map<Grade, Double> uniformDistribution = new EnumMap<>(Grade.class);
        int totalGrades = Grade.values().length;
        double probability = 1.0 / totalGrades;

        for (Grade grade : Grade.values()) {
            uniformDistribution.put(grade, probability);
        }

        return uniformDistribution;
    }

    public static <A extends Enum<A> & Allele> Map<A, Double> createUniformDistribution(Category category) {
        AlleleDomain<A> domain = CategoryDomains.typedDomainFor(category);

        if (domain.getAlleles().isEmpty()) {
            throw new IllegalArgumentException(
                    "Cannot create uniform distribution for category " + category + " with no alleles"
            );
        }

        Map<A, Double> distribution = new EnumMap<>(domain.getAlleleType());
        double probability = 1.0 / domain.getAlleles().size();

        for (A allele : domain.getAlleles()) {
            distribution.put(allele, probability);
        }

        return distribution;
    }

    public SheepService(SheepRepository sheepRepository, RelationshipService relationshipService) {
        this.sheepRepository = sheepRepository;
        this.relationshipService = relationshipService;
    }

    public Sheep findById(Integer id) {
        return sheepRepository.findWithAllById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Sheep with id " + id + " not found"
                ));
    }

    public Sheep findByIdAndUserId(Integer id, UUID userId) {
        return sheepRepository
                .findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Sheep with id " + id + " not found"
                ));
    }

    @Transactional
    public SheepResponseDTO getSheepResponseDTO(Integer sheepId, UUID userId) {
        Sheep sheep = findByIdAndUserId(sheepId, userId);

        Set<Category> lockedCategories = Arrays.stream(Category.values())
                .filter(category -> isCategoryLockedForEditing(sheep, category))
                .collect(Collectors.toSet());

        return DomainMapper.toResponseDTO(sheep, lockedCategories);
    }

    public Sheep saveSheep(Sheep sheep) {
        return sheepRepository.save(sheep);
    }

    public void saveAll(List<Sheep> sheep) {
        sheepRepository.saveAll(sheep);
    }

    public List<Sheep> getAllSheep(UUID userId) {
        return sheepRepository.findAllForInference(userId);
    }

    public List<SheepSummaryResponseDTO> getAllSheepSummary() {
        return sheepRepository.listAllSummaries();
    }

    /**
     * Fetches sheep from the database based on the supplied name and grades.
     * If name is not null then any sheep that has a similar name to the string
     * and is not null is allowed through. If grades is a supplied filter then
     * it will only return sheep with at least one supplied grade as a phenotype
     * or hidden allele.
     *
     * @param userId - id of the user in the jwt subject
     * @param name   - name to search likeness
     * @param grades - Set of whitelisted grades
     * @return a List of Sheep that matches the filter criteria
     */
    public List<SheepSummaryResponseDTO> filterSheepByNameAndGrade(UUID userId, String name, Set<Grade> grades) {
        if (grades == null || grades.isEmpty()) {
            grades = EnumSet.allOf(Grade.class);
        }

        List<String> alleleCodes = grades.stream()
                .map(Grade::code)
                .toList();

        List<Category> gradeCategories = Arrays.stream(Category.values())
                .filter(category -> CategoryDomains.domainFor(category) instanceof GradeAlleleDomain)
                .toList();

        return sheepRepository.listSheepHavingAnyGradeAndName(
                userId,
                gradeCategories,
                alleleCodes,
                name
        );
    }


    public DistributionResponseDTO getDistributionProjectionsByCategoryAndType(
            UUID userId,
            Category category,
            DistributionType distributionType,
            List<Integer> sheepIds
    ) {
        List<SheepDistributionRow> rows = sheepRepository
                .listDistributionRowsByCategoryAndType(userId, category, distributionType, sheepIds == null || sheepIds.isEmpty() ? null : sheepIds);

        Map<Integer, Map<String, Double>> result = new HashMap<>();

        for (SheepDistributionRow r : rows) {
            Map<String, Double> m = result.computeIfAbsent(r.sheepId(), k -> new HashMap<>());
            Double prev = m.putIfAbsent(r.allele(), r.probability());
            if (prev != null) {
                throw new IllegalStateException(
                        "Duplicate distribution row: sheepId=" + r.sheepId()
                                + ", allele=" + r.allele()
                                + ", prev=" + prev
                                + ", next=" + r.probability()
                );
            }
        }

        return new DistributionResponseDTO(category, distributionType, result);
    }


    @Transactional
    public Map<String, SheepResponseDTO> getParents(UUID userId, Integer sheepId) {
        Sheep sheep = findByIdAndUserId(sheepId, userId);

        if (sheep == null) return Collections.emptyMap();

        Relationship rel = sheep.getParentRelationship();
        if (rel == null) return Collections.emptyMap();

        List<Sheep> parents = sheepRepository.findAllByIdInAndUserId(
                List.of(rel.getParent1().getId(), rel.getParent2().getId()),
                userId
        );
        parents.sort(Comparator.comparing(Sheep::getId));

        return Map.of(
                "parent1", DomainMapper.toResponseDTO(parents.get(0)),
                "parent2", DomainMapper.toResponseDTO(parents.get(1))
        );
    }

    @Transactional
    public List<SheepResponseDTO> getChildren(UUID userId, Integer sheepId) {
        return sheepRepository.findChildrenWithDetailByParentId(userId, sheepId).stream().map(DomainMapper::toResponseDTO).toList();
    }

    @Transactional
    public List<SheepResponseDTO> getPartners(UUID userId, Integer sheepId) {
        List<Relationship> relationships = relationshipService.findRelationshipsByParentWithUserId(userId, sheepId);

        if (relationships.isEmpty()) {
            return List.of();
        }

        return relationships.stream()
                .map(rel -> {
                    if (rel.getParent1().getId().equals(sheepId)) {
                        return DomainMapper.toResponseDTO(rel.getParent2());
                    } else {
                        return DomainMapper.toResponseDTO(rel.getParent1());
                    }
                }).toList();
    }

    public SheepResponseDTO evolvePhenotype(UUID userId, Integer sheepId, Category category) {
        Sheep sheep = findByIdAndUserId(sheepId, userId);
        sheep.evolvePhenotype(category);
        return DomainMapper.toResponseDTO(saveSheep(sheep));
    }

    @Transactional
    public Sheep saveNewSheep(SheepNewRequestDTO sheepNewRequestDTO, UUID userId) {
        Sheep sheep = DomainMapper.fromRequestDTO(sheepNewRequestDTO);
        sheep.setUserId(userId);
        return sheepRepository.save(sheep);
    }

    @Deprecated
    @Transactional
    public Sheep replaceSheep(Integer sheepId, SheepReplaceRequestDTO sheepReplaceRequestDTO) {
        Sheep existing = findById(sheepId);

        if (existing.getParentRelationship() != null) {
            existing.getParentRelationship().removeChildFromRelationship(existing);
        }

        existing.setName(sheepReplaceRequestDTO.getName());
        existing.setGenotypes(sheepReplaceRequestDTO.getGenotypes());

        existing.replaceDistributionsFromDTO(sheepReplaceRequestDTO.getDistributions());

        if (sheepReplaceRequestDTO.getParentRelationshipId() != null) {
            Relationship relationship = relationshipService.findById(sheepReplaceRequestDTO.getParentRelationshipId());
            relationship.addChildToRelationship(existing);
        } else {
            existing.setBirthRecord(null);
        }

        return sheepRepository.save(existing);
    }


    public boolean isCategoryLockedForEditing(Sheep sheep, Category category) {
        BirthRecord ownBirthRecord = sheep.getBirthRecord();
        if (ownBirthRecord != null && ownBirthRecord.hasCategory(category)) {
            return true;
        }

        List<Relationship> relationshipsAsParent = relationshipService.findRelationshipsByParent(sheep.getId());

        for (Relationship relationship : relationshipsAsParent) {
            if (relationship.hasBirthRecordsForCategory(category)) {
                return true;
            }
        }

        return false;
    }


    @Transactional
    public Sheep updateSheep(UUID userId, Integer sheepId, SheepUpdateRequestDTO updateSheepModel) {
        Sheep sheep = findByIdAndUserId(sheepId, userId);

        if (updateSheepModel.getName() != null) {
            sheep.setName(updateSheepModel.getName());
        }

        Map<Category, SheepGenotypeDTO> updatedGenotypes = updateSheepModel.getGenotypes();

        if (updatedGenotypes != null) {
            // Phase 1: validate
            for (Map.Entry<Category, SheepGenotypeDTO> entry : updatedGenotypes.entrySet()) {
                Category category = entry.getKey();
                SheepGenotypeDTO genotypeDTO = entry.getValue();

                if (isNoOp(genotypeDTO)) {
                    continue;
                }

                if (isCategoryLockedForEditing(sheep, category)) {
                    throw new IllegalStateException(
                            "Category " + category + " cannot be edited because it appears in recorded history"
                    );
                }

                validateGenotypePatch(sheep, category, genotypeDTO);
            }

            // Phase 2: apply
            for (Map.Entry<Category, SheepGenotypeDTO> entry : updatedGenotypes.entrySet()) {
                Category category = entry.getKey();
                SheepGenotypeDTO genotypeDTO = entry.getValue();

                if (isNoOp(genotypeDTO)) {
                    continue;
                }

                applyGenotypePatch(sheep, category, genotypeDTO);
            }
        }

        return saveSheep(sheep);
    }

    private boolean isNoOp(SheepGenotypeDTO dto) {
        return dto == null || (dto.phenotype() == null && dto.hiddenAllele() == null);
    }


    private <A extends Enum<A> & Allele> void validateGenotypePatch(
            Sheep sheep,
            Category category,
            SheepGenotypeDTO genotypeDTO
    ) {
        AlleleDomain<A> domain = CategoryDomains.typedDomainFor(category);

        A newPhenotype = genotypeDTO.phenotype() != null
                ? domain.parse(genotypeDTO.phenotype())
                : null;

        A newHidden = genotypeDTO.hiddenAllele() != null
                ? domain.parse(genotypeDTO.hiddenAllele())
                : null;

        A effectivePhenotype = newPhenotype != null
                ? newPhenotype
                : sheep.getPhenotype(category);

        if (effectivePhenotype == null) {
            throw new IllegalStateException(
                    "Cannot update hidden allele for category " + category + " because no phenotype exists"
            );
        }

        if (newHidden != null && !domain.isHiddenAllelePossible(effectivePhenotype, newHidden)) {
            throw new IllegalStateException(
                    "Hidden allele " + newHidden.code()
                            + " is not compatible with phenotype "
                            + effectivePhenotype.code()
                            + " for category " + category
            );
        }
    }


    private <A extends Enum<A> & Allele> void applyGenotypePatch(
            Sheep sheep,
            Category category,
            SheepGenotypeDTO genotypeDTO
    ) {
        AlleleDomain<A> domain = CategoryDomains.typedDomainFor(category);

        A newPhenotype = genotypeDTO.phenotype() != null
                ? domain.parse(genotypeDTO.phenotype())
                : null;

        A newHidden = genotypeDTO.hiddenAllele() != null
                ? domain.parse(genotypeDTO.hiddenAllele())
                : null;

        if (newPhenotype != null) {
            sheep.setPhenotype(category, newPhenotype);
        }

        if (newHidden != null) {
            sheep.setHiddenAllele(category, newHidden);
        }

        sheep.syncPriorFromPhenotype(category);
        sheep.copyDistribution(category, DistributionType.PRIOR, DistributionType.INFERRED);
    }


    @Transactional
    public void deleteSheep(UUID userId, Integer sheepId) {
        Sheep sheep = findByIdAndUserId(sheepId, userId);

        List<Relationship> relationships = relationshipService.findRelationshipsByParent(sheepId);

        // set all children as founder sheep
        for (Relationship relationship : relationships) {
            for (BirthRecord br : relationship.getBirthRecords()) {
                Sheep child = br.getChild();
                if (child != null) {
                    br.setChild(null);
                    child.setBirthRecord(null);
                }
            }
        }

        // remove the sheep as a child if it has a parent relationship
        Relationship parentRelationship = sheep.getParentRelationship();
        if (parentRelationship != null) {
            parentRelationship.removeChildFromRelationship(sheep);
        }

        relationshipService.deleteAll(relationships);

        sheepRepository.deleteByIdAndUserId(sheep.getId(), userId);
    }
}
