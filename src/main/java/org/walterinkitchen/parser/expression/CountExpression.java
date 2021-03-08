package org.walterinkitchen.parser.expression;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Getter
public class CountExpression implements Expression {
    @Setter(AccessLevel.PROTECTED)
    protected String alis;

    /**
     * 要count的字段名
     */
    @Setter(AccessLevel.PROTECTED)
    protected Expression expr;

    public static CountExpression build(Expression expr, String alis) {
        CountExpression expression = new CountExpression();
        expression.setAlis(alis);
        expression.setExpr(expr);
        return expression;
    }

    @Override
    public <C, T> T accept(ExpressionVisitor<C, T> visitor, C context) {
        return visitor.visit(this, context);
    }
}
