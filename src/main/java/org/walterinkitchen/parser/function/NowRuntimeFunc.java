package org.walterinkitchen.parser.function;

import org.walterinkitchen.parser.expression.Expression;
import org.walterinkitchen.parser.expression.RuntimeFunctionCallExpression;

import java.util.List;

public class NowRuntimeFunc implements Function {
    public static final String FUNC_NAME = "now";

    @Override
    public Expression call(List<Expression> args) {
        return RuntimeFunctionCallExpression.build("$$NOW");
    }
}
