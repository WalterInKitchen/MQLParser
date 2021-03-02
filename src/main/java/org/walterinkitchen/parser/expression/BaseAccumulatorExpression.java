package org.walterinkitchen.parser.expression;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BaseAccumulatorExpression extends AccumulatorExpression {
    public enum Type {
        SUM() {
            @Override
            public String keyWord() {
                return "sum";
            }
        },
        AVG() {
            @Override
            public String keyWord() {
                return "avg";
            }
        },
        MAX() {
            @Override
            public String keyWord() {
                return "max";
            }
        },
        MIN() {
            @Override
            public String keyWord() {
                return "min";
            }
        },
        STD_DEV_POP() {
            @Override
            public String keyWord() {
                return "stdDevPop";
            }
        },
        STD_DEV_SAMP() {
            @Override
            public String keyWord() {
                return "stdDevSamp";
            }
        };

        public abstract String keyWord();
    }

    @Getter
    @Setter(AccessLevel.PROTECTED)
    private List<Expression> expressions;

    @Getter
    @Setter(AccessLevel.PROTECTED)
    private Type type;

    private BaseAccumulatorExpression() {
    }

    @Override
    public <C, T> T accept(ExpressionVisitor<C, T> visitor, C context) {
        return visitor.visit(this, context);
    }

    public static BaseAccumulatorExpression build(Type type, Expression expression) {
        BaseAccumulatorExpression baseAccumulatorExpression = new BaseAccumulatorExpression();
        baseAccumulatorExpression.setExpressions(Collections.singletonList(expression));
        baseAccumulatorExpression.setType(type);
        return baseAccumulatorExpression;
    }

    public static BaseAccumulatorExpression build(Type type, List<Expression> expressions) {
        BaseAccumulatorExpression baseAccumulatorExpression = new BaseAccumulatorExpression();
        baseAccumulatorExpression.setExpressions(new ArrayList<>(expressions));
        baseAccumulatorExpression.setType(type);
        return baseAccumulatorExpression;
    }
}
