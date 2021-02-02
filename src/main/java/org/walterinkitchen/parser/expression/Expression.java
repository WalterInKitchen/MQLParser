package org.walterinkitchen.parser.expression;

/**
 * Base language element
 */
public interface Expression {

    /**
     * Accept a visitor
     *
     * @param visitor visitor
     * @param context context
     * @param <C>     the context type
     * @param <T>     the result type
     * @return the result
     */
    <C, T> T accept(ExpressionVisitor<C, T> visitor, C context);

}
