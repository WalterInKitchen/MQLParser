package org.walterinkitchen.parser.function;

import lombok.NonNull;
import org.walterinkitchen.parser.exception.FunctionCallArgsException;
import org.walterinkitchen.parser.expression.ArithmeticExpression;
import org.walterinkitchen.parser.expression.Expression;

import java.util.List;

public class Ceil implements Function {
    public static final String FUNC_NAME = "ceil";

    @Override
    public Expression call(@NonNull List<Expression> args) {
        if (args.size() != 1) {
            throw new FunctionCallArgsException(FUNC_NAME, "参数数量错误");
        }
        return ArithmeticExpression.build(args.get(0), ArithmeticExpression.Operator.CEIL, null);
    }
}
