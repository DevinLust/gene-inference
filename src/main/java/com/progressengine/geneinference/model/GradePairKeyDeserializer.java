package com.progressengine.geneinference.model;

import com.fasterxml.jackson.databind.KeyDeserializer;

import java.io.IOException;

public class GradePairKeyDeserializer extends KeyDeserializer {

    @Override
    public Object deserializeKey(String key, com.fasterxml.jackson.databind.DeserializationContext ctxt)
            throws IOException {
        // Assuming your GradePair string key is formatted like "A_B"
        String[] parts = key.split("_");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid GradePair key: " + key);
        }
        return new GradePair(parts[0], parts[1]);
    }
}
