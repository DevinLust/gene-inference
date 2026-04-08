package com.progressengine.geneinference.service.AlleleDomains;

import java.util.List;
import java.util.Set;

import com.progressengine.geneinference.model.enums.Allele;
import com.progressengine.geneinference.model.enums.Category;

public interface AlleleDomain<A extends Allele> {
    Set<Category> supportedCategories();
    Class<A> getAlleleType();
    List<A> getAlleles();
    A parse(String code);
}
