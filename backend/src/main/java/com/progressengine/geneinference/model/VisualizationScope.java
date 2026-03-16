package com.progressengine.geneinference.model;

import java.util.Set;

public class VisualizationScope {
    private final Sheep centerSheep;
    private final Set<Node<?>> scopedNodes;
    private final Set<Sheep> scopedSheep;

    public VisualizationScope(Sheep centerSheep, Set<Node<?>> scopedNodes, Set<Sheep> scopedSheep) {
        this.centerSheep = centerSheep;
        this.scopedNodes = scopedNodes;
        this.scopedSheep = scopedSheep;
    }

    public Sheep getCenterSheep() {
        return centerSheep;
    }

    public Set<Node<?>> getScopedNodes() {
        return scopedNodes;
    }

    public Set<Sheep> getScopedSheep() {
        return scopedSheep;
    }

    public boolean touches(Message message) {
        return scopedNodes.contains(message.getSource()) || scopedNodes.contains(message.getTarget());
    }

    public boolean containsSheep(Sheep sheep) {
        return scopedSheep.contains(sheep);
    }
}
