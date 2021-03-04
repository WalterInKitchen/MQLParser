package org.walterinkitchen.parser.expression;

import lombok.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 分组表达式
 * 描述了GROUP BY expr1,expr2 ...
 */
public class GroupByExpression implements Expression {

    @Getter
    @Setter(AccessLevel.PROTECTED)
    private List<Expr> expressions;

    private GroupByExpression() {
    }

    @Override
    public <C, T> T accept(ExpressionVisitor<C, T> visitor, C context) {
        return visitor.visit(this, context);
    }

    public static GroupByExpression build(@NonNull List<Expression> expressions) {
        GroupByExpression expression = new GroupByExpression();
        expression.setExpressions(expressions.stream().map(x -> new Expr(x, null)).collect(Collectors.toList()));
        return expression;
    }

    public static GroupByExpression buildByExprs(@NonNull List<Expr> expressions) {
        GroupByExpression expression = new GroupByExpression();
        expression.setExpressions(expressions);
        return expression;
    }

    @AllArgsConstructor
    @Getter
    public static class Expr {
        private final Expression expression;
        private final String alias;
    }
}
