package org.walterinkitchen.parser.expression;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

public class BaseCompareExpression extends CompareExpression {

    public enum Comparator {
        EQ,
        NEQ,
        NULL_SAFE_EQ,
        GTE,
        GT,
        LTE,
        LT;
    }

    @Getter
    @Setter(AccessLevel.PROTECTED)
    protected Expression expr1;

    @Getter
    @Setter(AccessLevel.PROTECTED)
    protected Expression expr2;

    @Getter
    @Setter(AccessLevel.PROTECTED)
    protected Comparator comparator;

    public static BaseCompareExpression build(Expression expr1, Comparator comparator, Expression expr2) {
        BaseCompareExpression expression = new BaseCompareExpression();
        expression.setExpr1(expr1);
        expression.setExpr2(expr2);
        expression.setComparator(comparator);
        return expression;
    }

    @Override
    public <C, T> T accept(ExpressionVisitor<C, T> visitor, C context) {
        return visitor.visit(this, context);
    }
}
