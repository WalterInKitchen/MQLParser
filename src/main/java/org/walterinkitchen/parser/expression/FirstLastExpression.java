package org.walterinkitchen.parser.expression;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

public class FirstLastExpression extends AccumulatorExpression {
    public enum Type {
        FIRST() {
            @Override
            public String keyWord() {
                return "first";
            }
        },
        LAST() {
            @Override
            public String keyWord() {
                return "last";
            }
        };

        public abstract String keyWord();
    }

    @Getter
    @Setter(AccessLevel.PROTECTED)
    private Expression expression;

    @Getter
    @Setter(AccessLevel.PROTECTED)
    private Type type;


    @Override
    public <C, T> T accept(ExpressionVisitor<C, T> visitor, C context) {
        return visitor.visit(this, context);
    }

    public static FirstLastExpression build(Type type, Expression expression){
        FirstLastExpression firstLastExpression = new FirstLastExpression();
        firstLastExpression.setType(type);
        firstLastExpression.setExpression(expression);
        return firstLastExpression;
    }
}
