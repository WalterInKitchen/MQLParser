package org.walterinkitchen.parser.expression;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

public class XorExpression extends LogicExpression {
    @Getter
    @Setter(AccessLevel.PRIVATE)
    private List<Expression> expressions = new ArrayList<>();

    public static XorExpression build(List<Expression> expressions) {
        XorExpression xor = new XorExpression();
        xor.getExpressions().addAll(expressions);
        return xor;
    }

    @Override
    public <C, T> T accept(ExpressionVisitor<C, T> visitor, C context) {
        return visitor.visit(this, context);
    }
}
