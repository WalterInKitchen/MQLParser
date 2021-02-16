package org.walterinkitchen.parser.expression;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

public class CompareLikeExpression extends CompareExpression {

    @Getter
    @Setter(AccessLevel.PROTECTED)
    protected Expression expr1;

    @Getter
    @Setter(AccessLevel.PROTECTED)
    protected Expression expr2;

    @Getter
    @Setter(AccessLevel.PROTECTED)
    protected Expression escape;

    public static CompareLikeExpression build(Expression expr1, Expression expr2, Expression escape) {
        CompareLikeExpression expression = new CompareLikeExpression();
        expression.setExpr1(expr1);
        expression.setExpr2(expr2);
        expression.setEscape(escape);
        return expression;
    }

    @Override
    public <C, T> T accept(ExpressionVisitor<C, T> visitor, C context) {
        return visitor.visit(this, context);
    }
}
