package org.walterinkitchen.parser.expression;

public class OrExpression extends LogicExpression {
    @Override
    public <C, T> T accept(ExpressionVisitor<C, T> visitor, C context) {
        return visitor.visit(this, context);
    }
}