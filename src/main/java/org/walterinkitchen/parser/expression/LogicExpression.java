package org.walterinkitchen.parser.expression;

public abstract class LogicExpression implements Expression {
    @Override
    public <C, T> T accept(ExpressionVisitor<C, T> visitor, C context) {
        return visitor.visit(this, context);
    }
}
