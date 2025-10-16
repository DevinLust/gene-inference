package com.progressengine.geneinference.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;


@ExtendWith(MockitoExtension.class)
public class LoopyInferenceTest extends EnsembleInferenceTest {
    @BeforeEach
    public void setUp() {
        inferenceEngine = new LoopyInference(relationshipService);
    }
}
