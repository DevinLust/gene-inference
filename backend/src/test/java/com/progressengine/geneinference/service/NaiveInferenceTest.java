package com.progressengine.geneinference.service;

import org.junit.jupiter.api.BeforeEach;

public class NaiveInferenceTest extends InferenceEngineTest {

    @BeforeEach
    void setUp() {
        inferenceEngine = new NaiveInference();
    }
}
