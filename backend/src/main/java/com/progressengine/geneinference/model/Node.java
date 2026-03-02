package com.progressengine.geneinference.model;

public interface Node<V> {
    V getValue();
    Class<V> getValueType();
}
