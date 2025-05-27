package com.progressengine.geneinference.controller;

import com.progressengine.geneinference.model.Relationship;
import com.progressengine.geneinference.model.Sheep;
import com.progressengine.geneinference.repository.SheepRepository;
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

    public BreedController(SheepService sheepService, RelationshipService relationshipService) {
        this.sheepService = sheepService;
        this.relationshipService = relationshipService;
    }

    @PostMapping(value = "/{sheep1Id}/{sheep2Id}")
    public String breed(@PathVariable Integer sheep1Id, @PathVariable Integer sheep2Id) {
        // find/create the relationship of these two sheep
        Sheep sheep1 = sheepService.findById(sheep1Id);
        Sheep sheep2 = sheepService.findById(sheep2Id);
        Relationship relationship = relationshipService.findOrCreateRelationship(sheep1, sheep2);

        // create a new child from the two sheep

        // update the offspring phenotype frequency in the relationship

        // get the new joint distribution from the additional offspring data

        // update the marginal distributions of the parents' using the joint distribution and product of experts

        // infer child hidden distribution

        // optional - propagate probability to other partners and children
        return "new child bred";
    }

}
