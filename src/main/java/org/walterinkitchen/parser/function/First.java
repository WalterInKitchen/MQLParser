package org.walterinkitchen.parser.function;

import lombok.NonNull;
import org.walterinkitchen.parser.aggregate.BaseExpressionVisitor;
import org.walterinkitchen.parser.exception.FunctionCallArgsException;
import org.walterinkitchen.parser.expression.Expression;
import org.walterinkitchen.parser.expression.FirstLastExpression;

import java.util.List;

public class First extends Average {
    public static final String FUNC_NAME = "first";

    @Override
    public Expression call(@NonNull List<Expression> args) {
        if (args.size() != 1) {
            throw new FunctionCallArgsException(FUNC_NAME, "参数数量错误");
        }
        return FirstLastExpression.build(FirstLastExpression.Type.FIRST, args.get(0));
    }
}
