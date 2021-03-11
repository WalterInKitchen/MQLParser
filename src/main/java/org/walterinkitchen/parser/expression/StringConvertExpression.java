package org.walterinkitchen.parser.expression;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Getter
public class StringConvertExpression implements Expression {
    public enum Method {
        TO_UPPER {
            @Override
            public String toCmd() {
                return "toUpper";
            }
        },
        TO_LOWER {
            @Override
            public String toCmd() {
                return "toLower";
            }
        };

        public abstract String toCmd();
    }

    @Setter(AccessLevel.PROTECTED)
    private Method method;

    @Setter(AccessLevel.PROTECTED)
    private Expression expression;

    @Override
    public <C, T> T accept(ExpressionVisitor<C, T> visitor, C context) {
        return visitor.visit(this, context);
    }

    public static StringConvertExpression build(Method method, Expression expr) {
        StringConvertExpression expression = new StringConvertExpression();
        expression.setExpression(expr);
        expression.setMethod(method);
        return expression;
    }
}
