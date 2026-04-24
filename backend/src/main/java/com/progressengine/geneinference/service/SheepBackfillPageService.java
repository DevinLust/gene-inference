package com.progressengine.geneinference.service;

import com.progressengine.geneinference.model.Sheep;
import com.progressengine.geneinference.model.enums.Category;
import com.progressengine.geneinference.repository.SheepRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class SheepBackfillPageService {

    private final SheepRepository sheepRepository;

    public SheepBackfillPageService(SheepRepository sheepRepository) {
        this.sheepRepository = sheepRepository;
    }

    @Transactional
    public int processSingleSheep(Integer sheepId, boolean dryRun) {
        Sheep sheep = sheepRepository.findById(sheepId).orElseThrow();

        int changes = 0;

        for (Category category : Category.values()) {
            if (!sheep.hasGenotype(category)) {
                changes++;

                if (dryRun) {
                    System.out.println("[DRY RUN] Would backfill sheep "
                            + sheep.getId() + " category " + category);
                } else {
                    sheep.initializeCategoryWithDefaults(category);
                }
            }
        }

        if (!dryRun && changes > 0) {
            System.out.println("Updated sheepId=" + sheepId + " changes=" + changes);
        }

        return changes;
    }

    public record BackfillPageResult(int changes, boolean hasNext) {}
}

