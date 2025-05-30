package com.progressengine.geneinference.controller;

import com.progressengine.geneinference.model.Relationship;
import com.progressengine.geneinference.model.Sheep;
import com.progressengine.geneinference.model.enums.Grade;
import com.progressengine.geneinference.service.InferenceEngine;
import com.progressengine.geneinference.service.RelationshipService;
import com.progressengine.geneinference.service.SheepService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/breed")
public class BreedController {

    private final SheepService sheepService;
    private final RelationshipService relationshipService;
    private final InferenceEngine inferenceEngine;

    public BreedController(SheepService sheepService, RelationshipService relationshipService, InferenceEngine inferenceEngine) {
        this.sheepService = sheepService;
        this.relationshipService = relationshipService;
        this.inferenceEngine = inferenceEngine;
    }

    @PostMapping(value = "/{sheep1Id}/{sheep2Id}")
    public String breed(@PathVariable Integer sheep1Id, @PathVariable Integer sheep2Id) {
        // find/create the relationship of these two sheep
        Sheep sheep1 = sheepService.findById(sheep1Id);
        Sheep sheep2 = sheepService.findById(sheep2Id);
        Relationship relationship = relationshipService.findOrCreateRelationship(sheep1, sheep2);

        // create a new child from the two sheep
        Sheep newChild = RelationshipService.breedNewSheep(relationship);
        Grade childPhenotype = newChild.getPhenotype();

        // get the new joint distribution from the additional offspring data
        inferenceEngine.findJointDistribution(relationship);

        // update the marginal distributions of the parents' using the joint distribution and product of experts
        inferenceEngine.updateMarginalProbabilities(relationship);

        // infer child hidden distribution
        newChild.setHiddenDistribution(inferenceEngine.inferChildHiddenDistribution(relationship,  childPhenotype));

        // save relationship and new child
        Relationship savedRelationship = relationshipService.saveRelationship(relationship);
        Sheep savedChild = sheepService.saveSheep(newChild);

        // optional - propagate probability to other partners and children

        return String.format("Sheep has been bred with id: %s%nIn relationship with id: %s", savedChild.getId(), savedRelationship.getId());
    }

}
