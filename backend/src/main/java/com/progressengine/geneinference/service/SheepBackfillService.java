package com.progressengine.geneinference.service;

import com.progressengine.geneinference.model.Sheep;
import com.progressengine.geneinference.model.enums.Category;
import com.progressengine.geneinference.repository.SheepRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;


@Service
public class SheepBackfillService {

    private final SheepRepository sheepRepository;

    public SheepBackfillService(SheepRepository sheepRepository) {
        this.sheepRepository = sheepRepository;
    }

    @Transactional
    public void backfillMissingCategories(boolean dryRun) {
        int page = 0;
        int size = 5;
        int totalChanges = 0;

        Page<Sheep> sheepPage;

        do {
            sheepPage = sheepRepository.findAll(PageRequest.of(page, size));

            for (Sheep sheep : sheepPage.getContent()) {
                for (Category category : Category.values()) {
                    if (!sheep.hasGenotype(category)) {
                        totalChanges++;

                        if (dryRun) {
                            System.out.println("[DRY RUN] Would backfill sheep "
                                    + sheep.getId() + " category " + category);
                        } else {
                            sheep.initializeCategoryWithDefaults(category);
                        }
                    }
                }
            }

            page++;
        } while (sheepPage.hasNext());

        System.out.println(dryRun
                ? "[DRY RUN] Total missing category initializations: " + totalChanges
                : "Backfilled " + totalChanges + " category entries.");
    }
}
