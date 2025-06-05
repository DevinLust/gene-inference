package com.progressengine.geneinference.repository;

import com.progressengine.geneinference.model.Relationship;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RelationshipRepository extends JpaRepository<Relationship, Integer> {

    Optional<Relationship> findByParent1_IdAndParent2_Id(Integer parent1Id, Integer parent2Id);

    @Query("SELECT r FROM Relationship r WHERE r.parent1.id = :parentId OR r.parent2.id = :parentId")
    List<Relationship> findByParentId(@Param("parentId") Integer parentId);

}
