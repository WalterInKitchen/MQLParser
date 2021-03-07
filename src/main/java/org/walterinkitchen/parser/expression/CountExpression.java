package org.walterinkitchen.parser.expression;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Getter
public class CountExpression implements Expression {
    @Setter(AccessLevel.PROTECTED)
    protected String alis;

    public static CountExpression build(String alis) {
        CountExpression expression = new CountExpression();
        expression.setAlis(alis);
        return expression;
    }

    @Override
    public <C, T> T accept(ExpressionVisitor<C, T> visitor, C context) {
        return visitor.visit(this, context);
    }
}
