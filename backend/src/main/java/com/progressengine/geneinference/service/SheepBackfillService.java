package com.progressengine.geneinference.service;

import com.progressengine.geneinference.model.Sheep;
import com.progressengine.geneinference.model.enums.Category;
import com.progressengine.geneinference.repository.SheepRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SheepBackfillService {

    private final SheepRepository sheepRepository;

    public SheepBackfillService(SheepRepository sheepRepository) {
        this.sheepRepository = sheepRepository;
    }

    @Transactional
    public void backfillMissingCategories(boolean dryRun) {
        List<Sheep> sheepList = sheepRepository.findAll();

        int totalChanges = 0;

        for (Sheep sheep : sheepList) {
            for (Category category : Category.values()) {

                if (!sheep.hasGenotype(category)) {
                    totalChanges++;

                    if (dryRun) {
                        System.out.println(
                                "[DRY RUN] Would backfill sheep " + sheep.getId()
                                        + " category " + category
                        );
                    } else {
                        sheep.initializeCategoryWithDefaults(category);
                    }
                }
            }
        }

        System.out.println(
                dryRun
                        ? "[DRY RUN] Total missing category initializations: " + totalChanges
                        : "Backfilled " + totalChanges + " category entries."
        );
    }
}
