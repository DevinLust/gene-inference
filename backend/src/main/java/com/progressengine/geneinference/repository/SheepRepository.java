package com.progressengine.geneinference.repository;

import com.progressengine.geneinference.model.Sheep;
import com.progressengine.geneinference.model.enums.Grade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Set;

@Repository
public interface SheepRepository extends JpaRepository<Sheep, Integer>, JpaSpecificationExecutor<Sheep> {
    List<Sheep> findAllByParentRelationship_IdIn(Collection<Integer> parentRelationshipIds);

    @Query(value = """
        SELECT s.*
        FROM sheep s
        JOIN sheep_genotype g ON g.sheep_id = s.id
        WHERE (g.phenotype IN (:grades) OR g."hidden" IN (:grades))
          AND (:name IS NULL OR (s.name IS NOT NULL AND s.name <> '' AND s.name ILIKE '%' || :name || '%'))
        GROUP BY s.id
        HAVING COUNT(DISTINCT CASE
               WHEN g.phenotype IN (:grades) THEN g.phenotype
               WHEN g."hidden" IN (:grades) THEN g."hidden"
           END) = :gradeCount
    """, nativeQuery = true)
    List<Sheep> findSheepHavingAllGradesAndNameNative(
            @Param("grades") Set<String> grades,
            @Param("gradeCount") long gradeCount,
            @Param("name") String name
    );

    @Query(value = """
        SELECT DISTINCT s.*
        FROM sheep s
        JOIN sheep_genotype g ON g.sheep_id = s.id
        WHERE (g.phenotype IN (:grades) OR g."hidden" IN (:grades))
          AND (:name IS NULL OR (s.name IS NOT NULL AND s.name <> '' AND s.name ILIKE '%' || :name || '%'))
    """, nativeQuery = true)
    List<Sheep> findSheepHavingAnyGradeAndNameNative(
            @Param("grades") Set<String> grades,
            @Param("name") String name
    );

}
