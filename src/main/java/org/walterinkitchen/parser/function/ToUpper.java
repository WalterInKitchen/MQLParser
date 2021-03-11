package org.walterinkitchen.parser.function;

import lombok.NonNull;
import org.walterinkitchen.parser.exception.FunctionCallArgsException;
import org.walterinkitchen.parser.expression.Expression;
import org.walterinkitchen.parser.expression.StringConvertExpression;

import java.util.List;

public class ToUpper implements Function {
    public static final String FUNC_NAME = "ucase";

    @Override
    public Expression call(@NonNull List<Expression> args) {
        if (args.size() != 1) {
            throw new FunctionCallArgsException(FUNC_NAME, "参数数量错误");
        }
        return StringConvertExpression.build(StringConvertExpression.Method.TO_UPPER, args.get(0));
    }
}
