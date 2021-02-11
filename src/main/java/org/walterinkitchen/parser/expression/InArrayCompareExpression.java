package org.walterinkitchen.parser.expression;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

public class InArrayCompareExpression extends CompareExpression {

    @Getter
    @Setter(AccessLevel.PROTECTED)
    protected Expression expr1;

    @Getter
    @Setter(AccessLevel.PROTECTED)
    protected Expression expr2;

    public static InArrayCompareExpression build(Expression expr1, Expression expr2) {
        InArrayCompareExpression expression = new InArrayCompareExpression();
        expression.setExpr1(expr1);
        expression.setExpr2(expr2);
        return expression;
    }

    @Override
    public <C, T> T accept(ExpressionVisitor<C, T> visitor, C context) {
        return visitor.visit(this, context);
    }
}
