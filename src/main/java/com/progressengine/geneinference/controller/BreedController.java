package com.progressengine.geneinference.controller;

import com.progressengine.geneinference.model.Relationship;
import com.progressengine.geneinference.model.Sheep;
import com.progressengine.geneinference.model.enums.Category;
import com.progressengine.geneinference.model.enums.DistributionType;
import com.progressengine.geneinference.model.enums.Grade;
import com.progressengine.geneinference.service.InferenceEngine;
import com.progressengine.geneinference.service.RelationshipService;
import com.progressengine.geneinference.service.SheepService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;

import java.util.EnumMap;

@RestController
@RequestMapping(value = "/breed")
public class BreedController {

    private final SheepService sheepService;
    private final RelationshipService relationshipService;
    private final InferenceEngine inferenceEngine;

    public BreedController(SheepService sheepService, RelationshipService relationshipService, @Qualifier("loopy") InferenceEngine inferenceEngine) {
        this.sheepService = sheepService;
        this.relationshipService = relationshipService;
        this.inferenceEngine = inferenceEngine;
    }

    @Transactional
    @PostMapping(value = "/{sheep1Id}/{sheep2Id}")
    public String breed(@PathVariable Integer sheep1Id, @PathVariable Integer sheep2Id, @RequestParam(name = "saveChild", defaultValue = "true") boolean saveChild) {
        // find/create the relationship of these two sheep
        Sheep sheep1 = sheepService.findById(sheep1Id);
        Sheep sheep2 = sheepService.findById(sheep2Id);
        Relationship relationship = relationshipService.findOrCreateRelationship(sheep1, sheep2);

        // create a new child from the two sheep
        Sheep newChild = RelationshipService.breedNewSheep(relationship);

        // get the new joint distribution from the additional offspring data
        inferenceEngine.findJointDistribution(relationship);

        // update the marginal distributions of the parents' using the joint distribution
        inferenceEngine.updateMarginalProbabilities(relationship);
        sheepService.saveSheep(sheep1);
        sheepService.saveSheep(sheep2);

        // infer child hidden distribution
        inferenceEngine.inferChildHiddenDistribution(relationship,  newChild);
        newChild.setHiddenDistribution(new EnumMap<>(newChild.getPriorDistribution()));

        // testing new distribution
        newChild.setDistribution(Category.SWIM, DistributionType.INFERRED, newChild.getHiddenDistribution());
        sheep1.setDistribution(Category.SWIM, DistributionType.INFERRED, sheep1.getHiddenDistribution());
        sheep2.setDistribution(Category.SWIM, DistributionType.INFERRED, sheep2.getHiddenDistribution());
        // ----------------------------------

        // testing new joint distribution
        relationship.setJointDistribution(Category.SWIM, relationship.getHiddenPairsDistribution());
        // ----------------------------------

        // save relationship and new child
        Relationship savedRelationship = relationshipService.saveRelationship(relationship);
        if (saveChild) {
            sheepService.saveSheep(newChild);
        }

        // TODO - propagate probability to other partners and children

        return String.format("Sheep has been bred with phenotype: %s%nIn relationship with id: %s", newChild.getPhenotype(), savedRelationship.getId());
    }

}
