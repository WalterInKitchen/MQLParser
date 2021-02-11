package org.walterinkitchen.parser.expression;

import lombok.AccessLevel;
import lombok.Setter;

public class AllAnyCompareExpression extends BaseCompareExpression {
    public enum Type {
        ALL,
        ANY
    }

    @Setter(AccessLevel.PRIVATE)
    private Type type;

    public static AllAnyCompareExpression build(Type type, Expression expr1, Comparator comparator, Expression expr2) {
        AllAnyCompareExpression expression = new AllAnyCompareExpression();
        expression.setType(type);
        expression.setComparator(comparator);
        expression.setExpr1(expr1);
        expression.setExpr2(expr2);
        return expression;
    }
}
