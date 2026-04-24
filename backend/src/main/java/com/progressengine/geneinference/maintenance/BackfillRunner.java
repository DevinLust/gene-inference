package com.progressengine.geneinference.maintenance;

import com.progressengine.geneinference.service.SheepBackfillService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class BackfillRunner implements CommandLineRunner {
    private final SheepBackfillService backfillService;

    public BackfillRunner(SheepBackfillService backfillService) {
        this.backfillService = backfillService;
    }

    @Override
    public void run(String... args) {
        if (!Arrays.asList(args).contains("--backfill-categories")) {
            return;
        }

        boolean dryRun = Arrays.asList(args).contains("--dry-run");

        System.out.println(dryRun ? "DRY RUN" : "RUNNING BACKFILL");

        backfillService.backfillMissingCategories(dryRun);

        System.out.println("DONE");

        System.exit(0);
    }
}
