package org.walterinkitchen.parser.function;

import org.walterinkitchen.parser.aggregate.BaseExpressionVisitor;
import org.walterinkitchen.parser.expression.BaseAccumulatorExpression;
import org.walterinkitchen.parser.expression.Expression;

import java.util.ArrayList;
import java.util.List;

public class Average implements Function {
    public static final String FUNC_NAME = "AVG";
    private final BaseExpressionVisitor expressionVisitor = BaseExpressionVisitor.getInstance();

    @Override
    public Expression call(List<Expression> args) {
        List<Object> exprs = new ArrayList<>();
        return BaseAccumulatorExpression.build(BaseAccumulatorExpression.Type.AVG, args);
    }
}
