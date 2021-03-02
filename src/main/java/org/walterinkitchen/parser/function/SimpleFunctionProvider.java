package org.walterinkitchen.parser.function;

import lombok.NonNull;

import java.util.HashMap;
import java.util.Map;

public class SimpleFunctionProvider implements FunctionProvider {
    private static final Map<String, Function> functionMap = new HashMap<>();

    static {
        functionMap.put(DateToString.FUNC_NAME.toLowerCase(), new DateToString());
        functionMap.put(DateFromString.FUNC_NAME.toLowerCase(), new DateFromString());
        functionMap.put(Average.FUNC_NAME.toLowerCase(), new Average());
        functionMap.put(First.FUNC_NAME.toLowerCase(), new First());
        functionMap.put(Last.FUNC_NAME.toLowerCase(), new Last());
    }

    @Override
    public Function getFunctionByName(@NonNull String name) {
        return functionMap.get(name.toLowerCase());
    }
}
