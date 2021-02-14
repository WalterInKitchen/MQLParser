package org.walterinkitchen.parser.expression;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

public class ArrayExpression implements Expression {

    @Getter
    @Setter(AccessLevel.PROTECTED)
    protected List<Expression> expressions;

    public static ArrayExpression build(List<Expression> expressions){
        ArrayExpression expression = new ArrayExpression();
        expression.setExpressions(expressions);
        return expression;
    }

    @Override
    public <C, T> T accept(ExpressionVisitor<C, T> visitor, C context) {
        return visitor.visit(this, context);
    }
}
