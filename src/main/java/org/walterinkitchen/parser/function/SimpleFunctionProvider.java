package org.walterinkitchen.parser.function;

import lombok.NonNull;

import java.util.HashMap;
import java.util.Map;

public class SimpleFunctionProvider implements FunctionProvider {
    private static final Map<String, Function> functionMap = new HashMap<>();

    static {
        functionMap.put(DateToString.FUNC_NAME.toLowerCase(), new DateToString());
        functionMap.put(DateFromString.FUNC_NAME.toLowerCase(), new DateFromString());
    }

    @Override
    public Function getFunctionByName(@NonNull String name) {
        return functionMap.get(name.toLowerCase());
    }
}
