package com.progressengine.geneinference.repository;

import com.progressengine.geneinference.model.Sheep;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface SheepRepository extends JpaRepository<Sheep, Integer> {
    List<Sheep> findAllByParentRelationship_IdIn(Collection<Integer> parentRelationshipIds);
}
