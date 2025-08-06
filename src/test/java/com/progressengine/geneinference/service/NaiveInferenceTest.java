package com.progressengine.geneinference.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;

@Disabled("Implementation not ready for categorized genes")
public class NaiveInferenceTest extends InferenceEngineTest {

    @BeforeEach
    void setUp() {
        inferenceEngine = new NaiveInference();
    }
}
