package com.jool.plugin.mapping.utils.underscore;

public interface PredicateIndexed<T> {
    boolean test(int index, T arg);
}
