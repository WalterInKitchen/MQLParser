package org.walterinkitchen.parser.expression;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Getter
public class IfNullExpression implements Expression {
    @Setter(AccessLevel.PROTECTED)
    private Expression expression;

    @Setter(AccessLevel.PROTECTED)
    private Expression replacement;

    @Override
    public <C, T> T accept(ExpressionVisitor<C, T> visitor, C context) {
        return visitor.visit(this, context);
    }

    public static IfNullExpression build(Expression expr, Expression replacement) {
        IfNullExpression ifNull = new IfNullExpression();
        ifNull.setExpression(expr);
        ifNull.setReplacement(replacement);
        return ifNull;
    }
}
