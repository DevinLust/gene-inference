package com.progressengine.geneinference.model;

import java.util.List;

public record DemoGraphData(
        List<Sheep> sheep,
        List<Relationship> relationships,
        Sheep targetSheep
) {
}
