package com.progressengine.geneinference.repository;

import com.progressengine.geneinference.model.Relationship;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RelationshipRepository extends JpaRepository<Relationship, Integer> {
}
