package com.progressengine.geneinference.service;

import com.progressengine.geneinference.model.Sheep;
import com.progressengine.geneinference.model.enums.Category;
import com.progressengine.geneinference.repository.SheepRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
public class SheepBackfillPageService {

    private final SheepRepository sheepRepository;

    public SheepBackfillPageService(SheepRepository sheepRepository) {
        this.sheepRepository = sheepRepository;
    }

    @Transactional
    public BackfillPageResult processPage(int page, int size, boolean dryRun) {
        Page<Sheep> sheepPage = sheepRepository.findAll(PageRequest.of(page, size));

        int changes = 0;

        for (Sheep sheep : sheepPage.getContent()) {
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
        }

        return new BackfillPageResult(changes, sheepPage.hasNext());
    }

    public record BackfillPageResult(int changes, boolean hasNext) {}
}

