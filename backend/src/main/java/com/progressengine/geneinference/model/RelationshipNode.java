package com.progressengine.geneinference.model;

public class RelationshipNode implements Node<Relationship>{
    private final Relationship relationship;

    public RelationshipNode(Relationship relationship) {
        this.relationship = relationship;
    }

    @Override
    public Relationship getValue() {
        return relationship;
    }

    @Override
    public Class<Relationship> getValueType() {
        return Relationship.class;
    }
}
