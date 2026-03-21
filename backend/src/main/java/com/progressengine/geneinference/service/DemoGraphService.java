package com.progressengine.geneinference.service;

import com.progressengine.geneinference.model.*;
import com.progressengine.geneinference.model.enums.Category;
import com.progressengine.geneinference.model.enums.Grade;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
public class DemoGraphService {

    public DemoGraphData getDefaultDemoGraph() {
        // First generation
        Sheep s1 = demoSheep(
                1,
                "Alpha",
                genotypeMap(
                        new GradePair(Grade.A, Grade.B),
                        new GradePair(Grade.B, Grade.B),
                        new GradePair(Grade.C, Grade.B),
                        new GradePair(Grade.A, Grade.A),
                        new GradePair(Grade.B, Grade.C)
                )
        );
        Sheep s2 = demoSheep(2,
                "Beta",
                genotypeMap(
                        new GradePair(Grade.C, Grade.E),
                        new GradePair(Grade.S, Grade.A),
                        new GradePair(Grade.C, Grade.E),
                        new GradePair(Grade.D, Grade.B),
                        new GradePair(Grade.S, Grade.S)
                )
        );
        Sheep s3 = demoSheep(3,
                "Gamma",
                genotypeMap(
                        new GradePair(Grade.A, Grade.A),
                        new GradePair(Grade.C, Grade.B),
                        new GradePair(Grade.D, Grade.E),
                        new GradePair(Grade.B, Grade.S),
                        new GradePair(Grade.E, Grade.E)
                )
        );
        Sheep s4 = demoSheep(4,
                "Delta",
                genotypeMap(
                        new GradePair(Grade.C, Grade.E),
                        new GradePair(Grade.B, Grade.C),
                        new GradePair(Grade.C, Grade.S),
                        new GradePair(Grade.S, Grade.S),
                        new GradePair(Grade.A, Grade.D)
                )
        );

        Relationship r1 = relationship(1, s1, s2);
        Relationship r2 = relationship(2, s2, s3);
        Relationship r3 = relationship(3, s3, s4);
        Relationship r4 = relationship(4, s3, s1);

        // Second generation
        Sheep c1 = demoSheep(5,
                "Epsilon",
                genotypeMap(
                        new GradePair(Grade.B, Grade.E),
                        new GradePair(Grade.S, Grade.B),
                        new GradePair(Grade.E, Grade.C),
                        new GradePair(Grade.A, Grade.D),
                        new GradePair(Grade.C, Grade.S)
                )
        );
        r1.addChildToRelationship(c1);

        Sheep c2 = demoSheep(6,
                "Phi",
                genotypeMap(
                        new GradePair(Grade.A, Grade.E),
                        new GradePair(Grade.B, Grade.A),
                        new GradePair(Grade.D, Grade.C),
                        new GradePair(Grade.B, Grade.B),
                        new GradePair(Grade.E, Grade.S)
                )
        );
        r2.addChildToRelationship(c2);

        Sheep c3 = demoSheep(7,
                "Eta",
                genotypeMap(
                        new GradePair(Grade.A, Grade.E), // A from s3, E from s4
                        new GradePair(Grade.C, Grade.C), // C from s3, C from s4
                        new GradePair(Grade.E, Grade.S), // E from s3, S from s4
                        new GradePair(Grade.B, Grade.S), // B from s3, S from s4
                        new GradePair(Grade.E, Grade.D)  // E from s3, D from s4
                )
        );
        r3.addChildToRelationship(c3);

        Sheep c4 = demoSheep(8,
                "Theta",
                genotypeMap(
                        new GradePair(Grade.A, Grade.B), // A from s3, B from s1
                        new GradePair(Grade.B, Grade.B), // B from s3, B from s1
                        new GradePair(Grade.D, Grade.C), // D from s3, C from s1
                        new GradePair(Grade.S, Grade.A), // S from s3, A from s1
                        new GradePair(Grade.E, Grade.C)  // E from s3, C from s1
                )
        );
        r4.addChildToRelationship(c4);

        Relationship r5 = relationship(5, s3, c4);
        Sheep c5 = demoSheep(9,
                "Mu",
                genotypeMap(
                        new GradePair(Grade.A, Grade.A), // A from s3, B from s1
                        new GradePair(Grade.C, Grade.B), // B from s3, B from s1
                        new GradePair(Grade.E, Grade.C), // D from s3, C from s1
                        new GradePair(Grade.A, Grade.S), // S from s3, A from s1
                        new GradePair(Grade.C, Grade.E)  // E from s3, C from s1
                )
        );
        r5.addChildToRelationship(c5);

        Relationship r6 = relationship(6, c1, c2);

        // Third generation
        Sheep gc1 = demoSheep(10,
                "Pi",
                genotypeMap(
                        new GradePair(Grade.B, Grade.A),
                        new GradePair(Grade.A, Grade.B),
                        new GradePair(Grade.C, Grade.C),
                        new GradePair(Grade.A, Grade.B),
                        new GradePair(Grade.S, Grade.S)
                )
        );
        r6.addChildToRelationship(gc1);


        return new DemoGraphData(
                List.of(s1, s2, s3, s4, c1, c2, c3, c4, c5, gc1),
                List.of(r1, r2, r3, r4, r5, r6),
                s3
        );
    }

    private Sheep demoSheep(int id, String name, Map<Category, GradePair> genotypes) {
        Sheep sheep = new Sheep();
        sheep.setId(id);
        sheep.setName(name);
        sheep.createDefaultDistributions();

        for (Map.Entry<Category, GradePair> entry : genotypes.entrySet()) {
            sheep.setGenotype(entry.getKey(), entry.getValue());
        }

        return sheep;
    }

    private Relationship relationship(int id, Sheep parent1, Sheep parent2) {
        Relationship relationship = new Relationship();
        relationship.setId(id);
        relationship.setParent1(parent1);
        relationship.setParent2(parent2);
        return relationship;
    }

    private Map<Category, GradePair> genotypeMap(
            GradePair swim,
            GradePair fly,
            GradePair run,
            GradePair power,
            GradePair stamina
    ) {
        Map<Category, GradePair> map = new EnumMap<>(Category.class);
        map.put(Category.SWIM, swim);
        map.put(Category.FLY, fly);
        map.put(Category.RUN, run);
        map.put(Category.POWER, power);
        map.put(Category.STAMINA, stamina);
        return map;
    }
}
