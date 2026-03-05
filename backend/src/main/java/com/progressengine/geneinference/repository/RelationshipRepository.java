package com.progressengine.geneinference.repository;

import com.progressengine.geneinference.dto.RelationshipRow;
import com.progressengine.geneinference.model.Relationship;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RelationshipRepository extends JpaRepository<Relationship, Integer> {

    @EntityGraph(attributePaths = {"birthRecords", "birthRecords.phenotypesAtBirth"})
    Optional<Relationship> findByParent1_IdAndParent2_Id(Integer parent1Id, Integer parent2Id);

    @EntityGraph(attributePaths = {"birthRecords", "birthRecords.phenotypesAtBirth", "birthRecords.child"})
    @Query("SELECT r FROM Relationship r WHERE r.parent1.id = :parentId OR r.parent2.id = :parentId")
    List<Relationship> findByParentId(@Param("parentId") Integer parentId);

    @EntityGraph(attributePaths = {"birthRecords", "birthRecords.phenotypesAtBirth", "birthRecords.child"})
    @Query("""
    select r
    from Relationship r
    where (r.parent1.id = :parentId and r.parent1.userId = :userId)
       or (r.parent2.id = :parentId and r.parent2.userId = :userId)
""")
    List<Relationship> findByParentIdAndUserId(
            @Param("userId") UUID userId,
            @Param("parentId") Integer parentId
    );

    @Query(value = """
       (
         SELECT * FROM relationship 
         WHERE parent1_id = :p1 OR parent2_id = :p1
         LIMIT :limit
       )
       UNION ALL
       (
         SELECT * FROM relationship 
         WHERE parent1_id = :p2 OR parent2_id = :p2
         LIMIT :limit
       )
       """, nativeQuery = true)
    List<Relationship> findLimitedByParents(@Param("p1") Integer parent1Id,
                                            @Param("p2") Integer parent2Id,
                                            @Param("limit") int limit);

    @EntityGraph(attributePaths = {
            "parent1",
            "parent2",
            "birthRecords",
            "birthRecords.phenotypesAtBirth"
    })
    @Query("select r from Relationship r")
    List<Relationship> findAllWithFullGraph();

    @Query("""
        select new com.progressengine.geneinference.dto.RelationshipRow(
            r.id,
            p1.id,
            p1.name,
            p2.id,
            p2.name
        )
        from Relationship r
        join r.parent1 p1
        join r.parent2 p2
        where p1.userId = :userId and p2.userId = :userId
        order by r.id desc
    """)
    List<RelationshipRow> listAll(@Param("userId") UUID userId);

    @EntityGraph(attributePaths = {
            "parent1",
            "parent2",
            "birthRecords",
            "birthRecords.phenotypesAtBirth"
    })
    @Query("""
    select r from Relationship r
    where r.id = :id
        and r.parent1.userId = :userId
        and r.parent2.userId = :userId
    """)
    Optional<Relationship> findWithBirthsById(@Param("userId") UUID userId, @Param("id") Integer id);
}
