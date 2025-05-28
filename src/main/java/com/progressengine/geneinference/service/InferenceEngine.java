package com.progressengine.geneinference.service;

import com.progressengine.geneinference.model.Relationship;
import com.progressengine.geneinference.model.enums.Grade;

import java.util.Map;

public interface InferenceEngine {

    void findJointDistribution(Relationship relationship);

    Map<Grade, Double> inferChildHiddenDistribution(Relationship relationship,  Grade childPhenotype);

    void updateMarginalProbabilities(Relationship relationship);
}
