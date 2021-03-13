package org.walterinkitchen.parser.expression;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Getter
public class RoundExpression implements Expression {
    @Setter(AccessLevel.PROTECTED)
    private Expression expr;
    @Setter(AccessLevel.PROTECTED)
    private Integer place;

    @Override
    public <C, T> T accept(ExpressionVisitor<C, T> visitor, C context) {
        return visitor.visit(this, context);
    }

    public static RoundExpression build(Expression expr, Integer place) {
        RoundExpression expression = new RoundExpression();
        expression.setExpr(expr);
        expression.setPlace(place);
        return expression;
    }
}
