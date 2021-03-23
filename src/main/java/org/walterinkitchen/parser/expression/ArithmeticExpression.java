package org.walterinkitchen.parser.expression;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

public class ArithmeticExpression implements Expression {

    public enum Operator {
        PLUS,
        MINUS,
        MULTI,
        DIV,
        MOD,
        POWER,
        SQRT,
        LOG,
        LN,
        TRUNC
    }

    @Getter
    @Setter(AccessLevel.PROTECTED)
    protected Operator operator;
    @Getter
    @Setter(AccessLevel.PROTECTED)
    protected Expression expr1;

    @Getter
    @Setter(AccessLevel.PROTECTED)
    protected Expression expr2;

    public static ArithmeticExpression build(Expression expr1, Operator operator, Expression expr2) {
        ArithmeticExpression expression = new ArithmeticExpression();
        expression.setExpr1(expr1);
        expression.setExpr2(expr2);
        expression.setOperator(operator);
        return expression;
    }

    @Override
    public <C, T> T accept(ExpressionVisitor<C, T> visitor, C context) {
        return visitor.visit(this, context);
    }
}
