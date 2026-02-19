package com.progressengine.geneinference.repository;

import com.progressengine.geneinference.model.BirthRecord;
import com.progressengine.geneinference.model.enums.Category;
import com.progressengine.geneinference.model.enums.Grade;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BirthRecordRepository extends JpaRepository<BirthRecord, Integer> {
    @EntityGraph(attributePaths = {
            "phenotypesAtBirth",
            "parentRelationship",
            "child"
    })
    @Query("""
        select distinct br
        from BirthRecord br
        join br.phenotypesAtBirth p
        where br.parentRelationship.id = :relationshipId
          and p.category = :category
          and p.parent1Phenotype = :p1
          and p.parent2Phenotype = :p2
    """)
    List<BirthRecord> findBirthRecordsByParentPhenotypes(
            @Param("relationshipId") Integer relationshipId,
            @Param("category") Category category,
            @Param("p1") Grade p1,
            @Param("p2") Grade p2
    );

    @EntityGraph(attributePaths = {
            "child",
            "parentRelationship"
    })
    Optional<BirthRecord> findWithAllById(Integer id);
}
