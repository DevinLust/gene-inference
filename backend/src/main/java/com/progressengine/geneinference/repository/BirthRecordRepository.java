package com.progressengine.geneinference.repository;

import com.progressengine.geneinference.dto.BirthRecordRow;
import com.progressengine.geneinference.model.BirthRecord;
import com.progressengine.geneinference.model.enums.Category;
import com.progressengine.geneinference.model.enums.Grade;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface BirthRecordRepository extends JpaRepository<BirthRecord, Integer> {

    @EntityGraph(attributePaths = {
            "child",
            "phenotypesAtBirth",
            "parentRelationship",
            "parentRelationship.parent1",
            "parentRelationship.parent2"
    })
    @Query("""
        select distinct br
        from BirthRecord br
        join br.parentRelationship r
        join r.parent1 p1
        join r.parent2 p2
        where br.id = :id
            and p1.userId = :userId
            and p2.userId = :userId
    """)
    Optional<BirthRecord> findByIdAndUserId(@Param("id") Integer id, @Param("userId") UUID userId);

    @Query(
            value = """
    select new com.progressengine.geneinference.dto.BirthRecordRow(
      br.id,
      r.id,
      c.id,
      c.name,
      p1.id,
      p1.name,
      p2.id,
      p2.name
    )
    from BirthRecord br
    join br.parentRelationship r
    join r.parent1 p1
    join r.parent2 p2
    left join br.child c
    where p1.userId = :userId
      and p2.userId = :userId
  """,
            countQuery = """
    select count(br.id)
    from BirthRecord br
    join br.parentRelationship r
    join r.parent1 p1
    join r.parent2 p2
    where p1.userId = :userId
      and p2.userId = :userId
  """
    )
    Page<BirthRecordRow> pageAllRows(@Param("userId") UUID userId, Pageable pageable);


    @Query(
            value = """
    select new com.progressengine.geneinference.dto.BirthRecordRow(
      br.id,
      r.id,
      c.id,
      c.name,
      p1.id,
      p1.name,
      p2.id,
      p2.name
    )
    from BirthRecord br
    join br.parentRelationship r
    join r.parent1 p1
    join r.parent2 p2
    left join br.child c
    where r.id = :relId
      and p1.userId = :userId
      and p2.userId = :userId
  """,
            countQuery = """
    select count(br.id)
    from BirthRecord br
    join br.parentRelationship r
    join r.parent1 p1
    join r.parent2 p2
    where r.id = :relId
      and p1.userId = :userId
      and p2.userId = :userId
  """
    )
    Page<BirthRecordRow> pageRowsByParentRelationship(
            @Param("userId") UUID userId,
            @Param("relId") Integer relId,
            Pageable pageable
    );

    @Query(
            value = """
    select new com.progressengine.geneinference.dto.BirthRecordRow(
      br.id,
      r.id,
      c.id,
      c.name,
      p1.id,
      p1.name,
      p2.id,
      p2.name
    )
    from BirthRecord br
    join br.parentRelationship r
    join r.parent1 p1
    join r.parent2 p2
    left join br.child c
    where r.id = :relId
      and p1.userId = :userId
      and p2.userId = :userId
      and exists (
        select 1
        from BirthRecordPhenotype pab
        where pab.birthRecord = br
          and pab.category = :category
          and pab.parent1PhenotypeCode = :p1ph
          and pab.parent2PhenotypeCode = :p2ph
      )
  """,
            countQuery = """
    select count(br.id)
    from BirthRecord br
    join br.parentRelationship r
    join r.parent1 p1
    join r.parent2 p2
    where br.parentRelationship.id = :relId
      and p1.userId = :userId
      and p2.userId = :userId
      and exists (
        select 1
        from BirthRecordPhenotype pab
        where pab.birthRecord = br
          and pab.category = :category
          and pab.parent1PhenotypeCode = :p1ph
          and pab.parent2PhenotypeCode = :p2ph
      )
  """
    )
    Page<BirthRecordRow> pageRowsByParentPhenotypes(
            @Param("userId") UUID userId,
            @Param("relId") Integer relId,
            @Param("category") Category category,
            @Param("p1ph") Grade p1,
            @Param("p2ph") Grade p2,
            Pageable pageable
    );
}
