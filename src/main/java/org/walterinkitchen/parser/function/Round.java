package org.walterinkitchen.parser.function;

import org.walterinkitchen.parser.exception.FunctionCallArgsException;
import org.walterinkitchen.parser.expression.Expression;
import org.walterinkitchen.parser.expression.NumberLiteralExpression;
import org.walterinkitchen.parser.expression.RoundExpression;

import java.util.List;

public class Round implements Function {
    public static final String FUNC_NAME = "round";

    @Override
    public Expression call(List<Expression> args) {
        if (args.size() != 2) {
            throw new FunctionCallArgsException(FUNC_NAME, "参数数量错误");
        }
        if (!(args.get(1) instanceof NumberLiteralExpression)) {
            throw new FunctionCallArgsException(FUNC_NAME, "参数类型错误");
        }
        Integer place = Integer.parseInt(String.valueOf(((NumberLiteralExpression) args.get(1)).getNum()));
        return RoundExpression.build(args.get(0), place);
    }
}
