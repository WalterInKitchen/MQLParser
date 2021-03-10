package org.walterinkitchen.parser.expression;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

public class ArrayExpression implements Expression, DistinctAble {

    @Getter
    @Setter(AccessLevel.PROTECTED)
    protected List<Expression> expressions;
    private boolean distinct;

    public static ArrayExpression build(List<Expression> expressions) {
        ArrayExpression expression = new ArrayExpression();
        expression.setExpressions(expressions);
        return expression;
    }

    @Override
    public <C, T> T accept(ExpressionVisitor<C, T> visitor, C context) {
        return visitor.visit(this, context);
    }

    @Override
    public void setDistinct(boolean distinct) {
        this.distinct = distinct;
    }

    @Override
    public boolean isDistinct() {
        return this.distinct;
    }
}
