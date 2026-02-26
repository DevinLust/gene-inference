package com.progressengine.geneinference.repository;

import com.progressengine.geneinference.dto.BirthRecordRow;
import com.progressengine.geneinference.model.BirthRecord;
import com.progressengine.geneinference.model.enums.Category;
import com.progressengine.geneinference.model.enums.Grade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BirthRecordRepository extends JpaRepository<BirthRecord, Integer> {

    @Query("""
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
    order by br.id desc
    """)
    List<BirthRecordRow> listAllRows();


    @Query("""
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
    order by br.id desc
    """)
    List<BirthRecordRow> listRowsByParentRelationship(@Param("relId") Integer relId);

    @Query("""
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
        and exists (
          select 1
          from BirthRecordPhenotype pab
          where pab.birthRecord = br
            and pab.category = :category
            and pab.parent1Phenotype = :p1ph
            and pab.parent2Phenotype = :p2ph
        )
        order by br.id desc
    """)
    List<BirthRecordRow> findRowsByParentPhenotypes(
            @Param("relId") Integer relId,
            @Param("category") Category category,
            @Param("p1ph") Grade p1,
            @Param("p2ph") Grade p2
    );
}
