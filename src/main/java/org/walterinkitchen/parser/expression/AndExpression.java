package org.walterinkitchen.parser.expression;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

public class AndExpression extends LogicExpression {

    @Getter
    @Setter(AccessLevel.PRIVATE)
    private List<Expression> expressions = new ArrayList<>();

    public static AndExpression build(List<Expression> expressions) {
        AndExpression and = new AndExpression();
        and.getExpressions().addAll(expressions);
        return and;
    }

    @Override
    public <C, T> T accept(ExpressionVisitor<C, T> visitor, C context) {
        return visitor.visit(this, context);
    }
}
