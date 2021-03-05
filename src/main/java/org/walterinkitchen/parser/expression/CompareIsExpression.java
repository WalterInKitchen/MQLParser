package org.walterinkitchen.parser.expression;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

public class CompareIsExpression extends CompareExpression {

    public enum Type {
        NULL,
        TRUE,
        FALSE,
        UNKNOWN
    }

    @Getter
    @Setter(AccessLevel.PRIVATE)
    private Expression expression;

    @Getter
    @Setter(AccessLevel.PRIVATE)
    private Type type;

    @Override
    public <C, T> T accept(ExpressionVisitor<C, T> visitor, C context) {
        return visitor.visit(this, context);
    }

    public static CompareIsExpression build(Expression expression, Type type) {
        CompareIsExpression is = new CompareIsExpression();
        is.setType(type);
        is.setExpression(expression);
        return is;
    }
}
