package com.progressengine.geneinference.service;

import com.progressengine.geneinference.repository.SheepRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
public class SheepBackfillService {

    private final SheepRepository sheepRepository;
    private final SheepBackfillPageService pageService;

    public SheepBackfillService(
            SheepRepository sheepRepository,
            SheepBackfillPageService pageService
    ) {
        this.sheepRepository = sheepRepository;
        this.pageService = pageService;
    }

    public void backfillMissingCategories(boolean dryRun) {
        int page = 0;
        int size = 5;
        int totalChanges = 0;
        int processed = 0;

        Page<Integer> idPage;

        do {
            System.out.println("=== Starting page " + page + " ===");

            idPage = sheepRepository.findAllIds(PageRequest.of(page, size));

            for (Integer id : idPage.getContent()) {
                try {
                    int changes = pageService.processSingleSheep(id, dryRun);
                    totalChanges += changes;
                    processed++;

                    if (processed % 10 == 0) {
                        logMemory(processed, totalChanges, page, id);
                    }

                } catch (Exception e) {
                    System.err.println("❌ FAILED on sheepId=" + id + " page=" + page);
                    throw e;
                }
            }

            System.out.println("=== Finished page " + page + " ===");

            page++;
        } while (idPage.hasNext());

        System.out.println(dryRun
                ? "[DRY RUN] Total missing category initializations: " + totalChanges
                : "Backfilled " + totalChanges + " category entries.");
    }

    private void logMemory(int processed, int totalChanges, int page, int currentId) {
        Runtime runtime = Runtime.getRuntime();

        long used = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024);
        long total = runtime.totalMemory() / (1024 * 1024);
        long max = runtime.maxMemory() / (1024 * 1024);

        System.out.println(
                "[PROGRESS] processed=" + processed +
                        " totalChanges=" + totalChanges +
                        " page=" + page +
                        " currentId=" + currentId +
                        " | memory: used=" + used + "MB total=" + total + "MB max=" + max + "MB"
        );
    }
}
