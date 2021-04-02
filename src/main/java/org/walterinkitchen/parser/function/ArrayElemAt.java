package org.walterinkitchen.parser.function;

import org.walterinkitchen.parser.exception.FunctionCallArgsException;
import org.walterinkitchen.parser.expression.ArithmeticExpression;
import org.walterinkitchen.parser.expression.Expression;

import java.util.List;

public class ArrayElemAt implements Function {
    public static final String FUNC_NAME = "arrayElemAt";

    @Override
    public Expression call(List<Expression> args) {
        if (args.size() != 2) {
            throw new FunctionCallArgsException(FUNC_NAME, "参数数量错误");
        }
        return ArithmeticExpression.build(args.get(0), ArithmeticExpression.Operator.ARRAY_ELEM_AT, args.get(1));
    }
}
