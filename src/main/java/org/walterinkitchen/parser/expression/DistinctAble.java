package org.walterinkitchen.parser.expression;

public interface DistinctAble {

    default boolean isDistinct() {
        return false;
    }

    void setDistinct(boolean distinct);
}
