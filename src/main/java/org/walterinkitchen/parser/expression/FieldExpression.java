package org.walterinkitchen.parser.expression;

import lombok.Data;

@Data
public class FieldExpression implements Expression, DistinctAble {
    private String field;
    private boolean distinct;

    public static FieldExpression build(String field) {
        FieldExpression expression = new FieldExpression();
        expression.setField(field);
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
