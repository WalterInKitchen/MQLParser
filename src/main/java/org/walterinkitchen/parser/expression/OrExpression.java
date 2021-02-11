package org.walterinkitchen.parser.expression;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

public class OrExpression extends LogicExpression {
    @Getter
    @Setter(AccessLevel.PRIVATE)
    private List<Expression> expressions = new ArrayList<>();

    public static OrExpression build(List<Expression> expressions) {
        OrExpression or = new OrExpression();
        or.getExpressions().addAll(expressions);
        return or;
    }

    @Override
    public <C, T> T accept(ExpressionVisitor<C, T> visitor, C context) {
        return visitor.visit(this, context);
    }
}
