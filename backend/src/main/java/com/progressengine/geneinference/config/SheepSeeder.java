package com.progressengine.geneinference.config;

import com.progressengine.geneinference.model.AllelePair;
import com.progressengine.geneinference.model.Sheep;
import com.progressengine.geneinference.model.enums.Allele;
import com.progressengine.geneinference.model.enums.Category;
import com.progressengine.geneinference.service.AlleleDomains.AlleleDomain;
import com.progressengine.geneinference.service.AlleleDomains.CategoryDomains;
import com.progressengine.geneinference.repository.SheepRepository;
import jakarta.transaction.Transactional;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Random;
import java.util.List;

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
        if (true || sheepRepository.count() > 0) {
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
                setRandomAlleles(sheep, category, random);
            }

            sheep.createDefaultDistributions();
            sheepRepository.save(sheep);
        }
    }

    private <A extends Enum<A> & Allele> void setRandomAlleles(Sheep sheep, Category category, Random random) {
        AlleleDomain<A> domain = CategoryDomains.typedDomainFor(category);
        List<A> alleles = domain.getAlleles();
        A phenotype = alleles.get(random.nextInt(alleles.size()));
        A hidden = alleles.get(random.nextInt(alleles.size()));
        sheep.setGenotype(category, new AllelePair<>(phenotype, hidden));
    }
}

