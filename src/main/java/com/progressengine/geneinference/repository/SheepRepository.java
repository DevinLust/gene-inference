package com.progressengine.geneinference.repository;

import com.progressengine.geneinference.model.Sheep;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SheepRepository extends JpaRepository<Sheep, Integer> {
}
