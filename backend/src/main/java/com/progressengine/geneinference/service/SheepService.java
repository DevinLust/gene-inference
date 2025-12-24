package com.progressengine.geneinference.service;

import com.progressengine.geneinference.dto.*;
import com.progressengine.geneinference.exception.ResourceNotFoundException;
import com.progressengine.geneinference.model.GradePair;
import com.progressengine.geneinference.model.Relationship;
import com.progressengine.geneinference.model.Sheep;
import com.progressengine.geneinference.model.enums.Category;
import com.progressengine.geneinference.model.enums.DistributionType;
import com.progressengine.geneinference.model.enums.Grade;
import com.progressengine.geneinference.repository.RelationshipRepository;
import com.progressengine.geneinference.repository.SheepRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class SheepService {

    private final SheepRepository sheepRepository;
    private final RelationshipRepository relationshipRepository;

    public static Map<Grade, Double> createUniformDistribution() {
        Map<Grade, Double> uniformDistribution = new EnumMap<>(Grade.class);
        int totalGrades = Grade.values().length;
        double probability = 1.0 / totalGrades;

        for (Grade grade : Grade.values()) {
            uniformDistribution.put(grade, probability);
        }

        return uniformDistribution;
    }

    public SheepService(RelationshipRepository relationshipRepository, SheepRepository sheepRepository) {
        this.relationshipRepository = relationshipRepository;
        this.sheepRepository = sheepRepository;
    }

    public Sheep findById(Integer id) {
        return sheepRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Sheep with id " + id + " not found"
                ));
    }

    public Sheep saveSheep(Sheep sheep) {
        return sheepRepository.save(sheep);
    }

    public void saveAll(List<Sheep> sheep) {
        sheepRepository.saveAll(sheep);
    }

    public List<Sheep> getAllSheep() {
        return sheepRepository.findAll();
    }

    /**
     * Fetches sheep from the database based on the supplied name and grades.
     * If name is not null then any sheep that has a similar name to the string
     * and is not null is allowed through. If grades is a supplied filter then
     * it will only return sheep with at least one supplied grade as a phenotype
     * or hidden allele.
     *
     * @param name - name to search likeness
     * @param grades - Set of whitelisted grades
     * @return a List of Sheep that matches the filter criteria
     */
    public List<Sheep> filterSheep(String name, Set<Grade> grades) {
        if (grades == null || grades.isEmpty()) {
            grades = Arrays.stream(Grade.values()).collect(Collectors.toSet());
        }

        Set<String> gradeStrings = grades.stream().map(Grade::name).collect(Collectors.toSet());
        return sheepRepository.findSheepHavingAnyGradeAndNameNative(gradeStrings, name);
    }


    @Transactional
    public Map<String, SheepResponseDTO> getParents(Integer sheepId) {
        Sheep sheep = findById(sheepId);

        if (sheep == null) return Collections.emptyMap();

        Relationship rel = sheep.getParentRelationship();
        if (rel == null) return Collections.emptyMap();

        List<Sheep> parents = sheepRepository.findAllById(
                List.of(rel.getParent1().getId(), rel.getParent2().getId())
        );
        parents.sort((a, b) -> a.getId().compareTo(b.getId()));

        return Map.of(
                "parent1", toResponseDTO(parents.get(0)),
                "parent2", toResponseDTO(parents.get(1))
        );
    }

    @Transactional
    public List<SheepResponseDTO> getChildren(Integer sheepId) {
        List<Relationship> relationships = relationshipRepository.findByParentId(sheepId);

        if (relationships.isEmpty()) {
            return List.of();
        }

        List<Integer> relationshipIds = relationships.stream().map(Relationship::getId).toList();

        return sheepRepository.findAllByParentRelationship_IdIn(relationshipIds).stream().map(this::toResponseDTO).toList();
    }

    @Transactional
    public List<SheepResponseDTO> getPartners(Integer sheepId) {
        List<Relationship> relationships = relationshipRepository.findByParentId(sheepId);

        if (relationships.isEmpty()) {
            return List.of();
        }

        return relationships.stream()
                .map(rel -> {
                    if (rel.getParent1().getId().equals(sheepId)) {
                        return toResponseDTO(rel.getParent2());
                    } else {
                        return toResponseDTO(rel.getParent1());
                    }
                }).toList();
    }

    @Transactional
    public Sheep saveNewSheep(SheepNewRequestDTO sheepNewRequestDTO) {
        Sheep sheep = fromRequestDTO(sheepNewRequestDTO);
        return sheepRepository.save(sheep);
    }

    @Transactional
    public Sheep replaceSheep(Integer sheepId, SheepReplaceRequestDTO sheepReplaceRequestDTO) {
        Sheep existing = findById(sheepId);

        existing.setName(sheepReplaceRequestDTO.getName());
        existing.setGenotypes(sheepReplaceRequestDTO.getGenotypes());

        existing.replaceDistributionsFromDTO(sheepReplaceRequestDTO.getDistributions());

        if (sheepReplaceRequestDTO.getParentRelationshipId() != null) {
            Relationship relationship = relationshipRepository.findById(sheepReplaceRequestDTO.getParentRelationshipId())
                    .orElseThrow(() -> new ResourceNotFoundException("Invalid parentRelationshipId: " + sheepReplaceRequestDTO.getParentRelationshipId()));
            existing.setParentRelationship(relationship);
        } else {
            existing.setParentRelationship(null);
        }

        return sheepRepository.save(existing);
    }

    @Transactional
    public Sheep updateSheep(Integer sheepId, SheepUpdateRequestDTO updateSheepModel) {
        Sheep sheep = findById(sheepId);

        if (updateSheepModel.getName() != null) {
            sheep.setName(updateSheepModel.getName());
        }

        Map<Category, SheepGenotypeDTO> updatedGenotypes = updateSheepModel.getGenotypes();
        if (updatedGenotypes != null) {
            for (Map.Entry<Category, SheepGenotypeDTO> entry : updatedGenotypes.entrySet()) {
                GradePair genotype = entry.getValue().toGradePair();
                sheep.setGenotype(entry.getKey(), genotype);
            }
        }

        Map<Category, Map<Grade, Double>> updatedPriors = updateSheepModel.getDistributions();
        if (updatedPriors != null) {
            for (Map.Entry<Category, Map<Grade, Double>> entry : updatedPriors.entrySet()) {
                sheep.setDistribution(entry.getKey(), DistributionType.PRIOR, entry.getValue());
            }
        }

        return saveSheep(sheep);
    }

    @Transactional
    public void deleteSheep(Integer sheepId) {
        Sheep sheep = findById(sheepId);

        List<Relationship> relationships = relationshipRepository.findByParentId(sheepId);
        Set<Integer> relationshipIds = relationships.stream().map(Relationship::getId).collect(Collectors.toSet());

        List<Sheep> affectedChildren = sheepRepository.findAllByParentRelationship_IdIn(relationshipIds);
        for (Sheep child : affectedChildren) {
            child.setParentRelationship(null);
        }

        relationshipRepository.deleteAll(relationships);

        sheepRepository.delete(sheep);
    }

    public Sheep fromRequestDTO(SheepNewRequestDTO dto) {
        Sheep sheep = new Sheep();
        sheep.setName(dto.getName());
        sheep.setGenotypes(dto.getGenotypes());

        sheep.upsertDistributionsFromDTO(dto.getDistributions());

        if (dto.getParentRelationshipId() != null) {
            Relationship relationship = relationshipRepository.findById(dto.getParentRelationshipId())
                    .orElseThrow(() -> new ResourceNotFoundException("Invalid parentRelationshipId: " + dto.getParentRelationshipId()));
            sheep.setParentRelationship(relationship);
        }

        return sheep;
    }

    public SheepResponseDTO toResponseDTO(Sheep sheep) {
        SheepResponseDTO responseDTO = new SheepResponseDTO();
        responseDTO.setId(sheep.getId());
        responseDTO.setName(sheep.getName());
        responseDTO.setGenotypes(sheep.getGenotypes());
        responseDTO.setDistributions(sheep.getAllDistributions());

        if (sheep.getParentRelationship() != null) {
            responseDTO.setParentRelationshipId(sheep.getParentRelationship().getId());
        }

        return responseDTO;
    }
}
