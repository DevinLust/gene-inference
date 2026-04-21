package com.progressengine.geneinference.model;

import com.progressengine.geneinference.dto.SheepGenotypeDTO;
import com.progressengine.geneinference.model.enums.Category;
import jakarta.persistence.*;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Entity
@Table(name = "birth_record")
public class BirthRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "relationship_id")
    private Relationship parentRelationship;

    @OneToOne
    @JoinColumn(name = "sheep_id")
    private Sheep child;

    @OneToMany(mappedBy = "birthRecord", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    private Set<BirthRecordPhenotype> phenotypesAtBirth = new HashSet<>();

    public static BirthRecord create(Relationship parentRelationship, Map<Category, SheepGenotypeDTO> childGenotypes, Sheep savedChildRef) {
        BirthRecord record = new BirthRecord();
        Sheep parent1 = parentRelationship.getParent1();
        Sheep parent2 = parentRelationship.getParent2();

        record.parentRelationship = parentRelationship;

        for (Category category : Category.values()) {
            BirthRecordPhenotype phenotypeRecord = new BirthRecordPhenotype(record, category);
            phenotypeRecord.setAllPhenotypeCodes(parent1.getPhenotype(category).code(), parent2.getPhenotype(category).code(), childGenotypes.get(category).phenotype());
            record.phenotypesAtBirth.add(phenotypeRecord);
        }

        record.child = savedChildRef;
        if (savedChildRef != null && savedChildRef.getBirthRecord() != record) {
            savedChildRef.setBirthRecord(record);
        }
        return record;
    }

    public BirthRecord() {}

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Relationship getParentRelationship() {
        return parentRelationship;
    }

    public void setParentRelationship(Relationship parentRelationship) {
        this.parentRelationship = parentRelationship;
    }

    public Sheep getChild() {
        return child;
    }

    public void setChild(Sheep child) {
        this.child = child;
    }

    public boolean hasCategory(Category category) {
        for (BirthRecordPhenotype phenotypes : phenotypesAtBirth) {
            if (phenotypes.getCategory() == category) {
                return true;
            }
        }
        return false;
    }

    public Set<BirthRecordPhenotype> getPhenotypesAtBirth() {
        return phenotypesAtBirth;
    }

    public Map<Category, PhenotypeAtBirth> getPhenotypesAtBirthOrganized() {
        Map<Category, PhenotypeAtBirth> phenotypes = new EnumMap<>(Category.class);
        for (BirthRecordPhenotype phenotypeRecords : phenotypesAtBirth) {
            PhenotypeAtBirth phenotypeRecord = new PhenotypeAtBirth(phenotypeRecords);
            phenotypes.put(phenotypeRecords.getCategory(), phenotypeRecord);
        }
        return phenotypes;
    }
}
