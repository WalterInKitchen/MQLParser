package org.walterinkitchen.parser.expression;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 分组表达式
 * 描述了GROUP BY expr1,expr2 ...
 */
public class GroupByExpression implements Expression {

    @Getter
    @Setter(AccessLevel.PROTECTED)
    private List<Expression> expressions;

    private GroupByExpression() {
    }

    @Override
    public <C, T> T accept(ExpressionVisitor<C, T> visitor, C context) {
        return visitor.visit(this, context);
    }

    public static GroupByExpression build(List<Expression> expressions) {
        GroupByExpression expression = new GroupByExpression();
        expression.setExpressions(expressions);
        return expression;
    }
}
