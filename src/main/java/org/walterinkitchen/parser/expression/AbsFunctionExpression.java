package org.walterinkitchen.parser.expression;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Getter
public class AbsFunctionExpression implements Expression {

    @Setter(AccessLevel.PROTECTED)
    private Expression expression;

    @Override
    public <C, T> T accept(ExpressionVisitor<C, T> visitor, C context) {
        return visitor.visit(this, context);
    }

    public static AbsFunctionExpression build(Expression expr) {
        AbsFunctionExpression expression = new AbsFunctionExpression();
        expression.setExpression(expr);
        return expression;
    }
}
