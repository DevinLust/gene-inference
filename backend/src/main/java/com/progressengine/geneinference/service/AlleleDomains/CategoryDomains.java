package com.progressengine.geneinference.service.AlleleDomains;

import com.progressengine.geneinference.model.enums.Category;
import com.progressengine.geneinference.model.enums.Allele;

import java.util.EnumMap;
import java.util.Map;

/**
 * Sets up the mapping of Categories to AlleleDomains from the supported
 * Categories set up in each AlleleDomain
 */
public final class CategoryDomains {
    private static final Map<Category, AlleleDomain<?>> DOMAINS = new EnumMap<>(Category.class);

    static {
        register(new GradeAlleleDomain());
        register(new ToneAlleleDomain());
        register(new ColorAlleleDomain());
        register(new ShinyAlleleDomain());
    }

    private CategoryDomains() {}

    private static void register(AlleleDomain<?> domain) {
        for (Category category : domain.supportedCategories()) {
            if (DOMAINS.put(category, domain) != null) {
                throw new IllegalStateException("Duplicate allele domain registration for " + category);
            }
        }
    }

    public static AlleleDomain<?> domainFor(Category category) {
        AlleleDomain<?> domain = DOMAINS.get(category);
        if (domain == null) {
            throw new IllegalArgumentException("No allele domain registered for category " + category);
        }
        return domain;
    }

    @SuppressWarnings("unchecked")
    public static <A extends Enum<A> & Allele> AlleleDomain<A> typedDomainFor(Category category) {
        return (AlleleDomain<A>) domainFor(category);
    }
}
