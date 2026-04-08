package com.progressengine.geneinference.service.AlleleDomains;

import com.progressengine.geneinference.model.enums.Tone;
import com.progressengine.geneinference.model.enums.Category;

import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.Set;
import java.util.List;

@Component
public class ToneAlleleDomain implements AlleleDomain<Tone> {

    @Override
    public Set<Category> supportedCategories() {
        return EnumSet.of(Category.TONE);
    }

    @Override
    public Class<Tone> getAlleleType() {
        return Tone.class;
    }

    @Override
    public List<Tone> getAlleles() {
        return List.of(Tone.values());
    }

    @Override
    public Tone parse(String code) {
        return Tone.valueOf(code);
    }
}
