package com.progressengine.geneinference.model;

public class SheepNode implements Node<Sheep> {
    private final Sheep sheep;

    public SheepNode(Sheep sheep) {
        this.sheep = sheep;
    }

    public boolean isSheepNode() {
        return this.sheep != null;
    }

    @Override
    public Sheep getValue() {
        return this.sheep;
    }

    @Override
    public Class<Sheep> getValueType() {
        return Sheep.class;
    }
}
