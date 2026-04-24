package com.progressengine.geneinference.service;

import com.progressengine.geneinference.model.*;
import com.progressengine.geneinference.model.enums.Category;
import com.progressengine.geneinference.model.enums.Allele;
import com.progressengine.geneinference.model.enums.Grade;
import com.progressengine.geneinference.service.AlleleDomains.AlleleDomain;
import com.progressengine.geneinference.service.AlleleDomains.CategoryDomains;
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
                        new AlleleCodePair(Grade.A, Grade.B),
                        new AlleleCodePair(Grade.B, Grade.B),
                        new AlleleCodePair(Grade.C, Grade.B),
                        new AlleleCodePair(Grade.A, Grade.A),
                        new AlleleCodePair(Grade.B, Grade.C)
                )
        );
        Sheep s2 = demoSheep(2,
                "Beta",
                genotypeMap(
                        new AlleleCodePair(Grade.C, Grade.E),
                        new AlleleCodePair(Grade.S, Grade.A),
                        new AlleleCodePair(Grade.C, Grade.E),
                        new AlleleCodePair(Grade.D, Grade.B),
                        new AlleleCodePair(Grade.S, Grade.S)
                )
        );
        Sheep s3 = demoSheep(3,
                "Gamma",
                genotypeMap(
                        new AlleleCodePair(Grade.A, Grade.A),
                        new AlleleCodePair(Grade.C, Grade.B),
                        new AlleleCodePair(Grade.D, Grade.E),
                        new AlleleCodePair(Grade.B, Grade.S),
                        new AlleleCodePair(Grade.E, Grade.E)
                )
        );
        Sheep s4 = demoSheep(4,
                "Delta",
                genotypeMap(
                        new AlleleCodePair(Grade.C, Grade.E),
                        new AlleleCodePair(Grade.B, Grade.C),
                        new AlleleCodePair(Grade.C, Grade.S),
                        new AlleleCodePair(Grade.S, Grade.S),
                        new AlleleCodePair(Grade.A, Grade.D)
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
                        new AlleleCodePair(Grade.B, Grade.E),
                        new AlleleCodePair(Grade.S, Grade.B),
                        new AlleleCodePair(Grade.E, Grade.C),
                        new AlleleCodePair(Grade.A, Grade.D),
                        new AlleleCodePair(Grade.C, Grade.S)
                )
        );
        r1.addChildToRelationship(c1);

        Sheep c2 = demoSheep(6,
                "Phi",
                genotypeMap(
                        new AlleleCodePair(Grade.A, Grade.E),
                        new AlleleCodePair(Grade.B, Grade.A),
                        new AlleleCodePair(Grade.D, Grade.C),
                        new AlleleCodePair(Grade.B, Grade.B),
                        new AlleleCodePair(Grade.E, Grade.S)
                )
        );
        r2.addChildToRelationship(c2);

        Sheep c3 = demoSheep(7,
                "Eta",
                genotypeMap(
                        new AlleleCodePair(Grade.A, Grade.E), // A from s3, E from s4
                        new AlleleCodePair(Grade.C, Grade.C), // C from s3, C from s4
                        new AlleleCodePair(Grade.E, Grade.S), // E from s3, S from s4
                        new AlleleCodePair(Grade.B, Grade.S), // B from s3, S from s4
                        new AlleleCodePair(Grade.E, Grade.D)  // E from s3, D from s4
                )
        );
        r3.addChildToRelationship(c3);

        Sheep c4 = demoSheep(8,
                "Theta",
                genotypeMap(
                        new AlleleCodePair(Grade.A, Grade.B), // A from s3, B from s1
                        new AlleleCodePair(Grade.B, Grade.B), // B from s3, B from s1
                        new AlleleCodePair(Grade.D, Grade.C), // D from s3, C from s1
                        new AlleleCodePair(Grade.S, Grade.A), // S from s3, A from s1
                        new AlleleCodePair(Grade.E, Grade.C)  // E from s3, C from s1
                )
        );
        r4.addChildToRelationship(c4);

        Relationship r5 = relationship(5, s3, c4);
        Sheep c5 = demoSheep(9,
                "Mu",
                genotypeMap(
                        new AlleleCodePair(Grade.A, Grade.A), // A from s3, B from s1
                        new AlleleCodePair(Grade.C, Grade.B), // B from s3, B from s1
                        new AlleleCodePair(Grade.E, Grade.C), // D from s3, C from s1
                        new AlleleCodePair(Grade.A, Grade.S), // S from s3, A from s1
                        new AlleleCodePair(Grade.C, Grade.E)  // E from s3, C from s1
                )
        );
        r5.addChildToRelationship(c5);

        Relationship r6 = relationship(6, c1, c2);

        // Third generation
        Sheep gc1 = demoSheep(10,
                "Pi",
                genotypeMap(
                        new AlleleCodePair(Grade.B, Grade.A),
                        new AlleleCodePair(Grade.A, Grade.B),
                        new AlleleCodePair(Grade.C, Grade.C),
                        new AlleleCodePair(Grade.A, Grade.B),
                        new AlleleCodePair(Grade.S, Grade.S)
                )
        );
        r6.addChildToRelationship(gc1);


        return new DemoGraphData(
                List.of(s1, s2, s3, s4, c1, c2, c3, c4, c5, gc1),
                List.of(r1, r2, r3, r4, r5, r6),
                s3
        );
    }

    private Sheep demoSheep(int id, String name, Map<Category, AlleleCodePair> genotypes) {
        Sheep sheep = new Sheep();
        sheep.setId(id);
        sheep.setName(name);
        sheep.createDefaultDistributions();

        for (Map.Entry<Category, AlleleCodePair> entry : genotypes.entrySet()) {
                Category category = entry.getKey();
                AlleleCodePair codePair = entry.getValue();

                setGenotypeFromCodes(sheep, category, codePair);
        }

        return sheep;
    }

    private <A extends Enum<A> & Allele> void setGenotypeFromCodes(
        Sheep sheep,
        Category category,
        AlleleCodePair codePair
    ) {
        AlleleDomain<A> domain = CategoryDomains.typedDomainFor(category);

        AllelePair<A> pair = codePair.toAllelePair(domain);

        sheep.setGenotype(category, pair);
    }

    private Relationship relationship(int id, Sheep parent1, Sheep parent2) {
        Relationship relationship = new Relationship();
        relationship.setId(id);
        relationship.setParent1(parent1);
        relationship.setParent2(parent2);
        return relationship;
    }

    private Map<Category, AlleleCodePair> genotypeMap(
            AlleleCodePair swim,
            AlleleCodePair fly,
            AlleleCodePair run,
            AlleleCodePair power,
            AlleleCodePair stamina
    ) {
        Map<Category, AlleleCodePair> map = new EnumMap<>(Category.class);
        map.put(Category.SWIM, swim);
        map.put(Category.FLY, fly);
        map.put(Category.RUN, run);
        map.put(Category.POWER, power);
        map.put(Category.STAMINA, stamina);
        return map;
    }
}
