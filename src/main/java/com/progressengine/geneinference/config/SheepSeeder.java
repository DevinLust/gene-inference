package com.progressengine.geneinference.config;

import com.progressengine.geneinference.model.GradePair;
import com.progressengine.geneinference.model.Sheep;
import com.progressengine.geneinference.model.enums.Category;
import com.progressengine.geneinference.model.enums.Grade;
import com.progressengine.geneinference.repository.SheepRepository;
import jakarta.transaction.Transactional;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class SheepSeeder implements CommandLineRunner {

    private static final String[] NAMES = {
            "Larry", "Barry", "Harry",
            "Noman", "Ajax", "Olimar",
            "Paula", "Dolly", "Daula", "Dopple"
    };

    private final SheepRepository sheepRepository;

    public SheepSeeder(SheepRepository sheepRepository) {
        this.sheepRepository = sheepRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (sheepRepository.count() > 0) {
            System.out.println("Sheep already exist. Skipping seeding.");
            return;
        }

        seedSheep();
    }

    private void seedSheep() {
        Random random = new Random(42); // seeded

        for (int i = 0; i < 12; i++) {
            Sheep sheep = new Sheep();

            String name = i < NAMES.length
                    ? NAMES[i]
                    : "sheep: " + (i + 1);

            sheep.setName(name);

            for (Category category : Category.values()) {
                Grade phenotype = Grade.values()[random.nextInt(Grade.values().length)];
                Grade hidden = Grade.values()[random.nextInt(Grade.values().length)];
                sheep.setGenotype(category, new GradePair(phenotype, hidden));
            }

            sheep.createDefaultDistributions();
            sheepRepository.save(sheep);
        }
    }
}

