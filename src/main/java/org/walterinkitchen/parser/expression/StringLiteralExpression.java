package org.walterinkitchen.parser.expression;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

public class StringLiteralExpression extends LiteralExpression {

    @Getter
    @Setter(AccessLevel.PROTECTED)
    protected String text;

    public static StringLiteralExpression build(String text) {
        StringLiteralExpression expression = new StringLiteralExpression();
        expression.setText(text);
        return expression;
    }

    @Override
    public <C, T> T accept(ExpressionVisitor<C, T> visitor, C context) {
        return visitor.visit(this, context);
    }
}
