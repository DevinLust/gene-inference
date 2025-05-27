package com.progressengine.geneinference.repository;

import com.progressengine.geneinference.model.Relationship;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RelationshipRepository extends JpaRepository<Relationship, Integer> {

    Optional<Relationship> findByParent1_IdAndParent2_Id(Integer parent1Id, Integer parent2Id);
}
