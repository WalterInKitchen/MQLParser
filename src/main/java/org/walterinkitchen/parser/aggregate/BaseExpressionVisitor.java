package org.walterinkitchen.parser.aggregate;

import org.bson.Document;
import org.bson.types.Decimal128;
import org.walterinkitchen.parser.expression.*;

import java.util.*;

public class BaseExpressionVisitor implements org.walterinkitchen.parser.expression.ExpressionVisitor<ExprContext, Void> {
    private static volatile BaseExpressionVisitor instance;

    private BaseExpressionVisitor() {
    }

    @Override
    public Void visit(BaseCompareExpression expression, ExprContext context) {
        expression.getExpr1().accept(this, context);
        Object expr1 = context.getOptQ().pop();
        expression.getExpr2().accept(this, context);
        Object expr2 = context.getOptQ().pop();

        String opt = null;
        switch (expression.getComparator()) {
            case EQ:
            case NULL_SAFE_EQ:
                opt = "$eq";
                break;
            case NEQ:
                opt = "$ne";
                break;
            case GT:
                opt = "$gt";
                break;
            case GTE:
                opt = "$gte";
                break;
            case LT:
                opt = "$lt";
                break;
            case LTE:
                opt = "$lte";
                break;
        }
        Document expr = new Document(opt, Arrays.asList(expr1, expr2));
        context.getOptQ().push(expr);
        return null;
    }

    @Override
    public Void visit(AndExpression expression, ExprContext context) {
        List<Object> subExprs = new ArrayList<>();
        for (Expression expr : expression.getExpressions()) {
            expr.accept(this, context);
            subExprs.add(context.getOptQ().pop());
        }

        Document doc = new Document("$and", subExprs);
        context.getOptQ().push(doc);
        return null;
    }

    @Override
    public Void visit(OrExpression expression, ExprContext context) {
        List<Object> subExprs = new ArrayList<>();
        for (Expression expr : expression.getExpressions()) {
            expr.accept(this, context);
            subExprs.add(context.getOptQ().pop());
        }

        Document doc = new Document("$or", subExprs);
        context.getOptQ().push(doc);
        return null;
    }

    @Override
    public Void visit(NotExpression expression, ExprContext context) {
        expression.getExpression().accept(this, context);
        Object expr = context.getOptQ().pop();
        Document doc = new Document("$not", expr);
        context.getOptQ().push(doc);
        return null;
    }

    @Override
    public Void visit(CompareIsExpression expression, ExprContext context) {
        expression.getExpression().accept(this, context);
        Object expr = context.getOptQ().pop();
        Document doc = null;
        switch (expression.getType()) {
            case NULL:
                doc = new Document("$lt", Arrays.asList(expr, null));
                break;
            case TRUE:
                doc = new Document("$eq", Arrays.asList(expr, true));
                break;
            case FALSE:
                doc = new Document("$eq", Arrays.asList(expr, false));
                break;
            case UNKNOWN:
                break;
        }
        context.getOptQ().push(doc);
        return null;
    }

    @Override
    public Void visit(XorExpression expression, ExprContext context) {
        List<Object> subExprs = new ArrayList<>();
        for (Expression expr : expression.getExpressions()) {
            expr.accept(this, context);
            subExprs.add(context.getOptQ().pop());
        }

        Document doc = new Document("$not", new Document("$eq", subExprs));
        context.getOptQ().push(doc);
        return null;
    }

    @Override
    public Void visit(ArithmeticExpression expression, ExprContext context) {
        expression.getExpr1().accept(this, context);
        Object expr1 = context.getOptQ().pop();
        expression.getExpr2().accept(this, context);
        Object expr2 = context.getOptQ().pop();

        if (expr1 instanceof List) {
            expr1 = ((List<?>) expr1).get(0);
        }
        if (expr2 instanceof List) {
            expr2 = ((List<?>) expr2).get(0);
        }

        switch (expression.getOperator()) {
            case MOD:
                context.getOptQ().push(new Document("$mod", Arrays.asList(expr1, expr2)));
                break;
            case MULTI:
                context.getOptQ().push(new Document("$multiply", Arrays.asList(expr1, expr2)));
                break;
            case DIV:
                context.getOptQ().push(new Document("$divide", Arrays.asList(expr1, expr2)));
                break;
            case PLUS:
                context.getOptQ().push(new Document("$add", Arrays.asList(expr1, expr2)));
                break;
            case MINUS:
                context.getOptQ().push(new Document("$subtract", Arrays.asList(expr1, expr2)));
                break;
        }

        return null;
    }

    @Override
    public Void visit(InArrayCompareExpression expression, ExprContext context) {
        expression.getExpr1().accept(this, context);
        Object expr1 = context.getOptQ().pop();
        expression.getExpr2().accept(this, context);
        Object expr2 = context.getOptQ().pop();
        context.getOptQ().push(new Document("$in", Arrays.asList(expr1, expr2)));
        return null;
    }

    @Override
    public Void visit(ArrayExpression expression, ExprContext context) {
        List<Object> valList = new ArrayList<>();
        for (Expression expr : expression.getExpressions()) {
            expr.accept(this, context);
            valList.add(context.getOptQ().pop());
        }
        context.getOptQ().push(valList);
        return null;
    }

    @Override
    public Void visit(FunctionCallExpression expression, ExprContext context) {
        context.getOptQ().push(expression.explain());
        return null;
    }

    @Override
    public Void visit(FieldExpression expression, ExprContext context) {
        String field = "$" + expression.getField();
        context.getOptQ().push(field);
        return null;
    }

    @Override
    public Void visit(NumberLiteralExpression expression, ExprContext context) {
        Decimal128 decimal = new Decimal128(expression.getNum());
        context.getOptQ().push(decimal);
        return null;
    }

    @Override
    public Void visit(BooleanLiteralExpression expression, ExprContext context) {
        context.getOptQ().push(expression.isBool());
        return null;
    }

    @Override
    public Void visit(StringLiteralExpression expression, ExprContext context) {
        context.getOptQ().push(expression.getText());
        return null;
    }

    @Override
    public Void visit(NullLiteralExpression expression, ExprContext context) {
        context.getOptQ().push(null);
        return null;
    }

    @Override
    public Void visit(CompareLikeExpression expression, ExprContext context) {
        expression.getExpr1().accept(this, context);
        Object expr1 = context.getOptQ().pop();
        expression.getExpr2().accept(this, context);
        Object expr2 = context.getOptQ().pop();

        Map<String, Object> match = new HashMap<>();
        match.put("input", expr1);
        match.put("regex", expr2);

        Document doc = new Document("$regexMatch", match);
        context.getOptQ().push(doc);
        return null;
    }

    @Override
    public Void visit(BaseAccumulatorExpression expression, ExprContext context) {
        String operation = "$" + expression.getType().keyWord();
        if (context.currentScope() == ExprContext.Scope.GROUP_BY) {
            expression.getExpressions().get(0).accept(this, context);
            Object sumObj = context.getOptQ().pop();
            Document sum = new Document(operation, sumObj);
            context.getOptQ().push(sum);
        } else {
            List<Object> sums = new ArrayList<>();
            for (Expression expr : expression.getExpressions()) {
                expr.accept(this, context);
                Object sumObj = context.getOptQ().pop();
                sums.add(sumObj);
            }
            Document sum = new Document(operation, sums);
            context.getOptQ().push(sum);
        }
        return null;
    }

    @Override
    public Void visit(FirstLastExpression expression, ExprContext context) {
        expression.getExpression().accept(this, context);
        Object obj = context.getOptQ().pop();
        Document doc = new Document("$" + expression.getType().keyWord(), obj);
        context.getOptQ().push(doc);
        return null;
    }

    @Override
    public Void visit(AllElementExpression expression, ExprContext context) {
        context.getOptQ().push("*");
        return null;
    }

    public static BaseExpressionVisitor getInstance() {
        if (instance == null) {
            synchronized (BaseExpressionVisitor.class) {
                if (instance == null) {
                    instance = new BaseExpressionVisitor();
                }
            }
        }
        return instance;
    }
}
