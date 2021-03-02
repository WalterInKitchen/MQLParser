package org.walterinkitchen.parser.function;

import lombok.NonNull;
import org.walterinkitchen.parser.aggregate.BaseExpressionVisitor;
import org.walterinkitchen.parser.expression.BaseAccumulatorExpression;
import org.walterinkitchen.parser.expression.Expression;

import java.util.List;

public class Average implements Function {
    public static final String FUNC_NAME = "AVG";
    private final BaseExpressionVisitor expressionVisitor = BaseExpressionVisitor.getInstance();

    @Override
    public Expression call(@NonNull List<Expression> args) {
        return BaseAccumulatorExpression.build(BaseAccumulatorExpression.Type.AVG, args);
    }
}
