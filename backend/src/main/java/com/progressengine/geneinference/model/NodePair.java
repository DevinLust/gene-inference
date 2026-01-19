package com.progressengine.geneinference.model;

final class NodePair {
    final Node<?> from;
    final Node<?> to;

    NodePair(Node<?> from, Node<?> to) {
        this.from = from;
        this.to = to;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NodePair p)) return false;
        return from.equals(p.from) && to.equals(p.to);
    }

    @Override
    public int hashCode() {
        return 31 * from.hashCode() + to.hashCode();
    }
}

