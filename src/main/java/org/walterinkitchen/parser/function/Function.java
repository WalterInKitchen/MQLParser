package org.walterinkitchen.parser.function;


import org.walterinkitchen.parser.expression.Expression;

import java.util.List;

/**
 * MQL函数
 */
public interface Function {

    /**
     * Call this function
     *
     * @param args args
     * @return result
     */
    Expression call(List<Expression> args);
}
