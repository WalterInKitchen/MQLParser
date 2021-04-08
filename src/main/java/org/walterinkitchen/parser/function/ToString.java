package org.walterinkitchen.parser.function;

import org.walterinkitchen.parser.exception.FunctionCallArgsException;
import org.walterinkitchen.parser.expression.ArithmeticExpression;
import org.walterinkitchen.parser.expression.Expression;

import java.util.List;

public class ToString implements Function {
    public static final String FUNC_NAME = "toString";

    @Override
    public Expression call(List<Expression> args) {
        if (args.size() != 1) {
            throw new FunctionCallArgsException(FUNC_NAME, "参数数量错误");
        }
        return ArithmeticExpression.build(args.get(0), ArithmeticExpression.Operator.TO_STRING, null);
    }
}
