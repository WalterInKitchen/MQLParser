package org.walterinkitchen.parser.exception;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 该函数不被支持
 */
@NoArgsConstructor
public class FunctionNotSupportedException extends MqlRuntimeException {
    @Getter
    private String function;
    @Getter
    private List<String> args;

    public FunctionNotSupportedException(String function, List<String> args) {
        this.function = function;
        this.args = args;
    }

    public FunctionNotSupportedException(String function) {
        this.function = function;
    }
}
