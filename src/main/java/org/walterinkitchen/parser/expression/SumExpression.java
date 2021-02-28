package org.walterinkitchen.parser.expression;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

public class SumExpression extends AccumulatorExpression {

    @Getter
    @Setter(AccessLevel.PROTECTED)
    private Expression expression;

    private SumExpression() {
    }

    @Override
    public <C, T> T accept(ExpressionVisitor<C, T> visitor, C context) {
        return visitor.visit(this, context);
    }

    public static SumExpression build(Expression expression) {
        SumExpression sumExpression = new SumExpression();
        sumExpression.setExpression(expression);
        return sumExpression;
    }
}
