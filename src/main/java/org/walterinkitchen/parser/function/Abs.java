package org.walterinkitchen.parser.function;

import org.walterinkitchen.parser.exception.FunctionCallArgsException;
import org.walterinkitchen.parser.expression.*;

import java.util.List;

public class Abs implements Function {
    public static final String FUNC_NAME = "abs";

    @Override
    public Expression call(List<Expression> args) {
        if (args.size() != 1) {
            throw new FunctionCallArgsException(FUNC_NAME, "参数数量错误");
        }
        return AbsFunctionExpression.build(args.get(0));
    }
}
