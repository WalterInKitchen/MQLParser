package org.walterinkitchen.parser.expression;

public class NullLiteralExpression extends LiteralExpression {

    public static NullLiteralExpression build() {
        return new NullLiteralExpression();
    }

    @Override
    public <C, T> T accept(ExpressionVisitor<C, T> visitor, C context) {
        return visitor.visit(this, context);
    }
}
