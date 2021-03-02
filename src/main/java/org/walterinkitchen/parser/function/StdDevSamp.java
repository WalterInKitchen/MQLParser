package org.walterinkitchen.parser.function;

import lombok.NonNull;
import org.walterinkitchen.parser.expression.BaseAccumulatorExpression;
import org.walterinkitchen.parser.expression.Expression;

import java.util.List;

/**
 * 样本标准差
 */
public class StdDevSamp extends Average {
    public static final String FUNC_NAME = "stdDevSamp";

    @Override
    public Expression call(@NonNull List<Expression> args) {
        return BaseAccumulatorExpression.build(BaseAccumulatorExpression.Type.STD_DEV_SAMP, args);
    }
}
