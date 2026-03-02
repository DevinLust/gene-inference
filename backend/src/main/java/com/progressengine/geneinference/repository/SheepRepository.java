package com.progressengine.geneinference.repository;

import com.progressengine.geneinference.dto.SheepDistributionRow;
import com.progressengine.geneinference.dto.SheepSummaryResponseDTO;
import com.progressengine.geneinference.model.Sheep;
import com.progressengine.geneinference.model.enums.Category;
import com.progressengine.geneinference.model.enums.DistributionType;
import com.progressengine.geneinference.model.enums.Grade;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface SheepRepository extends JpaRepository<Sheep, Integer>, JpaSpecificationExecutor<Sheep> {
    @EntityGraph(attributePaths = {"genotypes", "distributions", "birthRecord", "birthRecord.parentRelationship"})
    @Query("""
        select s
        from Sheep s
        join s.birthRecord br
        join br.parentRelationship r
        where (r.parent1.id = :parentId or r.parent2.id = :parentId)
    """)
    List<Sheep> findChildrenWithDetailByParentId(@Param("parentId") Integer parentId);

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

    @Query("""
      select distinct new com.progressengine.geneinference.dto.SheepSummaryResponseDTO(s.id, s.name)
      from Sheep s
      join s.genotypes g
      where (g.phenotype in :grades or g.hiddenAllele in :grades)
        and (:name is null or lower(CAST(s.name AS String)) like lower(concat('%', CAST(:name AS String), '%')))
      order by s.id desc
    """)
    List<SheepSummaryResponseDTO> listSheepHavingAnyGradeAndName(
            @Param("grades") Set<Grade> grades,
            @Param("name") String name
    );

    @EntityGraph(value = "Sheep.withDistributionsAndGenotypes")
    @Query("SELECT s FROM Sheep s")
    List<Sheep> findAllForInference();

    @EntityGraph(attributePaths = {
            "distributions",
            "genotypes",
            "birthRecord",
            "birthRecord.parentRelationship"
    })
    Optional<Sheep> findWithAllById(Integer id);

    @Query("""
        select new com.progressengine.geneinference.dto.SheepSummaryResponseDTO(
            s.id,
            s.name
        )
        from Sheep s
        order by s.id desc
    """)
    List<SheepSummaryResponseDTO> listAllSummaries();

    @Query("""
      select new com.progressengine.geneinference.dto.SheepDistributionRow(
        d.sheep.id,
        d.grade,
        d.probability
      )
      from SheepDistribution d
      where d.category = :category
        and d.distributionType = :type
        and (:sheepIds is null or d.sheep.id in :sheepIds)
    """)
    List<SheepDistributionRow> listDistributionRowsByCategoryAndType(
            @Param("category") Category category,
            @Param("type") DistributionType distributionType,
            @Param("sheepIds") List<Integer> sheepIds
    );

}
