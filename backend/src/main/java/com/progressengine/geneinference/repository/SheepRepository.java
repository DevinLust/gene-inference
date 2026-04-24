package com.progressengine.geneinference.repository;

import com.progressengine.geneinference.dto.SheepDistributionRow;
import com.progressengine.geneinference.dto.SheepSummaryResponseDTO;
import com.progressengine.geneinference.model.Sheep;
import com.progressengine.geneinference.model.enums.Category;
import com.progressengine.geneinference.model.enums.DistributionType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
public interface SheepRepository extends JpaRepository<Sheep, Integer>, JpaSpecificationExecutor<Sheep> {
    @EntityGraph(attributePaths = {"genotypes", "distributions", "birthRecord", "birthRecord.parentRelationship"})
    @Query("""
    select distinct s
    from Sheep s
    join s.birthRecord br
    join br.parentRelationship r
    where s.userId = :userId
      and (
            (r.parent1.id = :parentId and r.parent1.userId = :userId)
         or (r.parent2.id = :parentId and r.parent2.userId = :userId)
      )
""")
    List<Sheep> findChildrenWithDetailByParentId(
            @Param("userId") UUID userId,
            @Param("parentId") Integer parentId
    );

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
      where s.userId = :userId
        and g.category in :categories
        and (g.phenotypeCode in :grades or g.hiddenAlleleCode in :grades)
        and (:name is null or lower(cast(s.name as string)) like lower(concat('%', cast(:name as string), '%')))
      order by s.id desc
    """)
    List<SheepSummaryResponseDTO> listSheepHavingAnyGradeAndName(
            @Param("userId") UUID userId,
            @Param("categories") List<Category> categories,
            @Param("grades") List<String> grades,
            @Param("name") String name
    );

    @EntityGraph(value = "Sheep.withDistributionsAndGenotypes")
    @Query("""
        SELECT s FROM Sheep s
        where s.userId = :userId
    """)
    List<Sheep> findAllForInference(@Param("userId") UUID userId);

    @EntityGraph(attributePaths = {
            "distributions",
            "genotypes",
            "birthRecord",
            "birthRecord.parentRelationship"
    })
    Optional<Sheep> findWithAllById(Integer id);

    @EntityGraph(attributePaths = {
            "distributions",
            "genotypes",
            "birthRecord",
            "birthRecord.parentRelationship"
    })
    Optional<Sheep> findByIdAndUserId(Integer id, UUID userId);

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
        d.alleleCode,
        d.probability
      )
      from SheepDistribution d
      where d.sheep.userId = :userId
        and d.category = :category
        and d.distributionType = :type
        and (:sheepIds is null or d.sheep.id in :sheepIds)
    """)
    List<SheepDistributionRow> listDistributionRowsByCategoryAndType(
            @Param("userId") UUID userId,
            @Param("category") Category category,
            @Param("type") DistributionType type,
            @Param("sheepIds") List<Integer> sheepIds
    );

    List<Sheep> findAllByIdInAndUserId(List<Integer> ids, UUID userId);

    long deleteByIdAndUserId(Integer id, UUID userId);
}
