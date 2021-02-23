package org.walterinkitchen.parser.exception;

import lombok.Getter;

/**
 * 函数参数相关异常
 */
public class FunctionCallArgsException extends MqlRuntimeException {

    //函数名
    @Getter
    private String name;

    //错误消息
    @Getter
    private String description;

    public FunctionCallArgsException(String name) {
        this.name = name;
    }

    public FunctionCallArgsException(String name, String description) {
        this.name = name;
        this.description = description;
    }
}
