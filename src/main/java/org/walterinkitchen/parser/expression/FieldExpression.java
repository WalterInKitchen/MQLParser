package org.walterinkitchen.parser.expression;

import lombok.Data;

@Data
public class FieldExpression implements Expression {
    private String field;

    public static FieldExpression build(String field) {
        FieldExpression expression = new FieldExpression();
        expression.setField(field);
        return expression;
    }

    @Override
    public <C, T> T accept(ExpressionVisitor<C, T> visitor, C context) {
        return visitor.visit(this, context);
    }
}
