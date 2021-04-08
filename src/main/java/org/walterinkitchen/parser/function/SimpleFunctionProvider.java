package org.walterinkitchen.parser.function;

import lombok.NonNull;

import java.util.HashMap;
import java.util.Map;

public class SimpleFunctionProvider implements FunctionProvider {
    private static final Map<String, Function> functionMap = new HashMap<>();
    private static final Map<String, Function> runtimeFunctionMap = new HashMap<>();

    static {
        initFunc();
        initRuntimeFunc();
    }

    private static void initRuntimeFunc() {
        runtimeFunctionMap.put(NowRuntimeFunc.FUNC_NAME.toLowerCase(), new NowRuntimeFunc());
        runtimeFunctionMap.put(Mod.FUNC_NAME.toLowerCase(), new Mod());
    }

    private static void initFunc() {
        functionMap.put(DateToString.FUNC_NAME.toLowerCase(), new DateToString());
        functionMap.put(DateFromString.FUNC_NAME.toLowerCase(), new DateFromString());
        functionMap.put(Average.FUNC_NAME.toLowerCase(), new Average());
        functionMap.put(First.FUNC_NAME.toLowerCase(), new First());
        functionMap.put(Last.FUNC_NAME.toLowerCase(), new Last());
        functionMap.put(StdDevPop.FUNC_NAME.toLowerCase(), new StdDevPop());
        functionMap.put(StdDevSamp.FUNC_NAME.toLowerCase(), new StdDevSamp());
        functionMap.put(IfNull.FUNC_NAME.toLowerCase(), new IfNull());
        functionMap.put(ToUpper.FUNC_NAME.toLowerCase(), new ToUpper());
        functionMap.put(ToLower.FUNC_NAME.toLowerCase(), new ToLower());
        functionMap.put(Round.FUNC_NAME.toLowerCase(), new Round());
        functionMap.put(Abs.FUNC_NAME.toLowerCase(), new Abs());
        functionMap.put(Power.FUNC_NAME.toLowerCase(), new Power());
        functionMap.put(Sqrt.FUNC_NAME.toLowerCase(), new Sqrt());
        functionMap.put(Log.FUNC_NAME.toLowerCase(), new Log());
        functionMap.put(Ln.FUNC_NAME.toLowerCase(), new Ln());
        functionMap.put(Trunc.FUNC_NAME.toLowerCase(), new Trunc());
        functionMap.put(Floor.FUNC_NAME.toLowerCase(), new Floor());
        functionMap.put(Ceil.FUNC_NAME.toLowerCase(), new Ceil());
        functionMap.put(Sin.FUNC_NAME.toLowerCase(), new Sin());
        functionMap.put(Sinh.FUNC_NAME.toLowerCase(), new Sinh());
        functionMap.put(Cos.FUNC_NAME.toLowerCase(), new Cos());
        functionMap.put(Cosh.FUNC_NAME.toLowerCase(), new Cosh());
        functionMap.put(ToBool.FUNC_NAME.toLowerCase(), new ToBool());
        functionMap.put(ArrayElemAt.FUNC_NAME.toLowerCase(), new ArrayElemAt());
        functionMap.put(ToDecimal.FUNC_NAME.toLowerCase(), new ToDecimal());
        functionMap.put(ToString.FUNC_NAME.toLowerCase(), new ToString());
        functionMap.put(ToInt.FUNC_NAME.toLowerCase(), new ToInt());
    }

    @Override
    public Function getFunctionByName(@NonNull String name) {
        return functionMap.get(name.toLowerCase());
    }

    @Override
    public Function getRuntimeFunctionByName(String name) {
        return runtimeFunctionMap.get(name.toLowerCase());
    }
}
