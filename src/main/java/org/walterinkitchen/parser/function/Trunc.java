package org.walterinkitchen.parser.function;

import org.walterinkitchen.parser.exception.FunctionCallArgsException;
import org.walterinkitchen.parser.expression.ArithmeticExpression;
import org.walterinkitchen.parser.expression.Expression;

import java.util.List;

public class Trunc implements Function {
    public static final String FUNC_NAME = "trunc";

    @Override
    public Expression call(List<Expression> args) {
        if (args.size() != 1 && args.size() != 2) {
            throw new FunctionCallArgsException(FUNC_NAME, "参数数量错误");
        }
        Expression expr2 = null;
        if (args.size() == 2) {
            expr2 = args.get(1);
        }
        return ArithmeticExpression.build(args.get(0), ArithmeticExpression.Operator.TRUNC, expr2);
    }
}
