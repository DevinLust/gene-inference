package com.progressengine.geneinference.service;

import com.progressengine.geneinference.dto.*;
import com.progressengine.geneinference.exception.ResourceNotFoundException;
import com.progressengine.geneinference.model.BirthRecord;
import com.progressengine.geneinference.model.Relationship;
import com.progressengine.geneinference.model.Sheep;
import com.progressengine.geneinference.model.enums.Category;
import com.progressengine.geneinference.model.enums.DistributionType;
import com.progressengine.geneinference.model.enums.Grade;
import com.progressengine.geneinference.repository.SheepRepository;
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

    public Sheep saveSheep(Sheep sheep) {
        return sheepRepository.save(sheep);
    }

    public void saveAll(List<Sheep> sheep) {
        sheepRepository.saveAll(sheep);
    }

    public List<Sheep> getAllSheep() {
        return sheepRepository.findAllForInference();
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
        return sheepRepository.findSheepHavingAnyGradeAndName(grades, name);
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
        List<Integer> childIds = sheepRepository.findSavedChildIdsRearedBy(sheepId);
        if (childIds.isEmpty()) return List.of();

        return sheepRepository.findWithAllByIdIn(childIds).stream().map(this::toResponseDTO).toList();
    }

    @Transactional
    public List<SheepResponseDTO> getPartners(Integer sheepId) {
        List<Relationship> relationships = relationshipService.findRelationshipsByParent(sheepId);

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

    public SheepResponseDTO evolvePhenotype(Integer sheepId, Category category) {
        Sheep sheep = findById(sheepId);
        sheep.evolvePhenotype(category);
        return toResponseDTO(saveSheep(sheep));
    }

    @Transactional
    public Sheep saveNewSheep(SheepNewRequestDTO sheepNewRequestDTO) {
        Sheep sheep = fromRequestDTO(sheepNewRequestDTO);
        return sheepRepository.save(sheep);
    }

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

    @Transactional
    public Sheep updateSheep(Integer sheepId, SheepUpdateRequestDTO updateSheepModel) {
        Sheep sheep = findById(sheepId);

        if (updateSheepModel.getName() != null) {
            sheep.setName(updateSheepModel.getName());
        }

        Map<Category, SheepGenotypeDTO> updatedGenotypes = updateSheepModel.getGenotypes();
        sheep.updateGenotypes(updatedGenotypes);

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

        sheepRepository.delete(sheep);
    }

    public Sheep fromRequestDTO(SheepNewRequestDTO dto) {
        Sheep sheep = new Sheep();
        sheep.setName(dto.getName());
        sheep.setGenotypes(dto.getGenotypes());

        sheep.upsertDistributionsFromDTO(dto.getDistributions());

        return sheep;
    }

    public Sheep fromRequestDTO(SheepBreedRequestDTO dto) {
        Sheep sheep = new Sheep();
        sheep.setName(dto.getName());
        sheep.setGenotypes(dto.getGenotypes());

        sheep.upsertDistributionsFromDTO(dto.getDistributions());

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
