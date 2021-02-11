package org.walterinkitchen.parser.expression;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

public class NotExpression extends LogicExpression {

    @Getter
    @Setter(AccessLevel.PRIVATE)
    private Expression expression;


    public static Expression build(Expression expr) {
        NotExpression not = new NotExpression();
        not.setExpression(expr);
        return not;
    }

    @Override
    public <C, T> T accept(ExpressionVisitor<C, T> visitor, C context) {
        return visitor.visit(this, context);
    }
}
