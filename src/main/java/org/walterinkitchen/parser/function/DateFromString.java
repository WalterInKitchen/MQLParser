package org.walterinkitchen.parser.function;

import org.bson.Document;
import org.walterinkitchen.parser.exception.FunctionCallArgsException;
import org.walterinkitchen.parser.expression.*;

import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * 字符串转日期
 */
public class DateFromString implements Function, ExpressionVisitor<DateFromString.Context, Void> {
    public static final String FUNC_NAME = "dateFromString";

    /**
     * 函数功能同dateFromString
     * links:https://docs.mongodb.com/manual/reference/operator/aggregation/dateFromString/
     *
     * @param args args
     * @return
     */
    @Override
    public Expression call(List<Expression> args) {
        if (args.size() < 2 || args.size() > 5) {
            throw new FunctionCallArgsException(FUNC_NAME);
        }

        Context context = new Context();

        //dateString
        args.get(0).accept(this, context);
        String dateString = String.valueOf(context.stack.pop());
        //format
        args.get(1).accept(this, context);
        String format = String.valueOf(context.stack.pop());

        //timeZone
        String zone = null;
        if (args.size() >= 3) {
            args.get(2).accept(this, context);
            zone = String.valueOf(context.stack.pop());
        }

        //onError
        Object onError = null;
        if (args.size() >= 4) {
            args.get(3).accept(this, context);
            onError = context.stack.pop();
        }

        //onNull
        Object onNull = null;
        if (args.size() >= 5) {
            args.get(4).accept(this, context);
            onNull = context.stack.pop();
        }

        String finalZone = zone;
        Object finalOnNull = onNull;
        Object finalOnError = onError;
        Document doc = new Document("$dateFromString", new HashMap<String, Object>() {{
            put("dateString", dateString);
            put("format", format);
            if (finalZone != null) {
                put("timezone", finalZone);
            }
            if (finalOnError != null) {
                put("onError", finalOnError);
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
