package org.walterinkitchen.parser.function;

/**
 * 用来查找函数
 */
public interface FunctionProvider {
    Function getFunctionByName(String name);

    Function getRuntimeFunctionByName(String name);
}
