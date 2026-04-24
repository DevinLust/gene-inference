package com.progressengine.geneinference.service;

import org.springframework.stereotype.Service;


@Service
public class SheepBackfillService {

    private final SheepBackfillPageService pageService;

    public SheepBackfillService(SheepBackfillPageService pageService) {
        this.pageService = pageService;
    }

    public void backfillMissingCategories(boolean dryRun) {
        int page = 0;
        int size = 5;
        int totalChanges = 0;

        while (true) {
            SheepBackfillPageService.BackfillPageResult result = pageService.processPage(page, size, dryRun);
            totalChanges += result.changes();

            if (!result.hasNext()) {
                break;
            }

            page++;
        }

        System.out.println(dryRun
                ? "[DRY RUN] Total missing category initializations: " + totalChanges
                : "Backfilled " + totalChanges + " category entries.");
    }
}
