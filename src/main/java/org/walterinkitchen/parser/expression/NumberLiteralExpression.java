package org.walterinkitchen.parser.expression;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

public class NumberLiteralExpression extends LiteralExpression {

    @Getter
    @Setter(AccessLevel.PROTECTED)
    protected BigDecimal num;

    public static NumberLiteralExpression build(BigDecimal num) {
        NumberLiteralExpression expression = new NumberLiteralExpression();
        expression.setNum(num);
        return expression;
    }

    @Override
    public <C, T> T accept(ExpressionVisitor<C, T> visitor, C context) {
        return visitor.visit(this, context);
    }
}
