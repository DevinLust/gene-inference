package com.progressengine.geneinference.dto;

import com.progressengine.geneinference.model.AllelePair;
import com.progressengine.geneinference.model.enums.Allele;
import com.progressengine.geneinference.model.enums.Category;
import com.progressengine.geneinference.service.AlleleDomains.AlleleDomain;
import com.progressengine.geneinference.service.AlleleDomains.CategoryDomains;

public record SheepGenotypeDTO(String phenotype, String hiddenAllele) {

    public <A extends Enum<A> & Allele> AllelePair<A> toAllelePair(Category category) {
        AlleleDomain<A> domain = CategoryDomains.typedDomainFor(category);
        return AllelePair.fromStrings(phenotype, hiddenAllele, domain);
    }

    public static <A extends Enum<A> & Allele> SheepGenotypeDTO fromAllelePair(AllelePair<A> pair) {
        return new SheepGenotypeDTO(
                pair.getFirst().code(),
                pair.getSecond().code()
        );
    }
}

