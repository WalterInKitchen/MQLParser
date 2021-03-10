package org.walterinkitchen.parser.function;

import org.walterinkitchen.parser.exception.FunctionCallArgsException;
import org.walterinkitchen.parser.expression.Expression;
import org.walterinkitchen.parser.expression.IfNullExpression;

import java.util.List;

public class IfNull implements Function {
    public static final String FUNC_NAME = "ifNull";

    @Override
    public Expression call(List<Expression> args) {
        if (args.size() != 2) {
            throw new FunctionCallArgsException(FUNC_NAME, "参数数量错误");
        }
        return IfNullExpression.build(args.get(0), args.get(1));
    }
}
