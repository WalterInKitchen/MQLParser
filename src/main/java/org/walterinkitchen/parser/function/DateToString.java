package org.walterinkitchen.parser.function;


import org.bson.Document;
import org.walterinkitchen.parser.exception.FunctionCallArgsException;
import org.walterinkitchen.parser.expression.*;

import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * 日期转字符串
 */
public class DateToString implements Function, ExpressionVisitor<DateToString.Context, Void> {
    public static final String FUNC_NAME = "dateToString";

    /**
     * 函数功能同MongoDB DateToString
     * links:https://docs.mongodb.com/manual/reference/operator/aggregation/dateToString/
     *
     * @param args args
     * @return 表达式
     */
    @Override
    public Expression call(List<Expression> args) {
        if (args.size() < 2 || args.size() > 4) {
            throw new FunctionCallArgsException(FUNC_NAME);
        }

        Context context = new Context();
        args.get(0).accept(this, context);
        String date = "$" + context.stack.pop();
        args.get(1).accept(this, context);
        String format = String.valueOf(context.stack.pop());

        String zone = null;
        if (args.size() >= 3) {
            args.get(2).accept(this, context);
            zone = String.valueOf(context.stack.pop());
        }

        String onNull = null;
        if (args.size() >= 4) {
            args.get(3).accept(this, context);
            onNull = String.valueOf(context.stack.pop());
        }

        String finalZone = zone;
        String finalOnNull = onNull;
        Document doc = new Document("$dateToString", new HashMap<String, String>() {{
            put("date", date);
            put("format", format);
            if (finalZone != null) {
                put("timezone", finalZone);
            }
            if (finalOnNull != null) {
                put("onNull", finalOnNull);
            }
        }});
        return FunctionCallExpression.build(doc);
    }

    @Override
    public Void visit(FieldExpression expression, Context context) {
        String field = expression.getField();
        context.stack.push(field);
        return null;
    }

    @Override
    public Void visit(StringLiteralExpression expression, Context context) {
        String text = expression.getText();
        context.stack.push(text);
        return null;
    }

    @Override
    public Void visit(NullLiteralExpression expression, Context context) {
        context.stack.push(null);
        return null;
    }

    @Override
    public Void visitDefault(Expression expression, Context context) {
        throw new FunctionCallArgsException(FUNC_NAME, "arg is not supported:" + expression);
    }

    protected static class Context {
        private final Deque<Object> stack = new LinkedList<>();
    }
}
