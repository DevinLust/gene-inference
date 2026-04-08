package com.progressengine.geneinference.service;

import org.springframework.stereotype.Service;
import com.progressengine.geneinference.model.enums.Category;
import com.progressengine.geneinference.service.AlleleDomains.AlleleDomain;
import com.progressengine.geneinference.model.enums.Allele;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
public class CategoryDomainService {

    private final Map<Category, AlleleDomain<? extends Allele>> domainsByCategory;

    public CategoryDomainService(List<AlleleDomain<? extends Allele>> domains) {
        this.domainsByCategory = new EnumMap<>(Category.class);

        for (AlleleDomain<? extends Allele> domain : domains) {
            for (Category category : domain.supportedCategories()) {
                if (domainsByCategory.put(category, domain) != null) {
                    throw new IllegalStateException("Duplicate allele domain registration for " + category);
                }
            }
        }
    }

    public AlleleDomain<? extends Allele> domainFor(Category category) {
        AlleleDomain<? extends Allele> domain = domainsByCategory.get(category);
        if (domain == null) {
            throw new IllegalArgumentException("No allele domain registered for category " + category);
        }
        return domain;
    }
}
