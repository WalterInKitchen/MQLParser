package org.walterinkitchen.parser.function;

import lombok.NonNull;
import org.walterinkitchen.parser.expression.BaseAccumulatorExpression;
import org.walterinkitchen.parser.expression.Expression;

import java.util.List;

/**
 * 总体标准差
 */
public class StdDevPop extends Average {
    public static final String FUNC_NAME = "StdDevPop";

    @Override
    public Expression call(@NonNull List<Expression> args) {
        return BaseAccumulatorExpression.build(BaseAccumulatorExpression.Type.STD_DEV_POP, args);
    }
}
