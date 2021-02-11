package org.walterinkitchen.parser.expression;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

public class BooleanLiteralExpression extends LiteralExpression {

    @Getter
    @Setter(AccessLevel.PROTECTED)
    protected boolean bool;

    public static BooleanLiteralExpression build(boolean bool) {
        BooleanLiteralExpression expression = new BooleanLiteralExpression();
        expression.setBool(bool);
        return expression;
    }

    @Override
    public <C, T> T accept(ExpressionVisitor<C, T> visitor, C context) {
        return visitor.visit(this, context);
    }
}
