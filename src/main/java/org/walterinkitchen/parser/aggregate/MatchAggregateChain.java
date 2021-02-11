package org.walterinkitchen.parser.aggregate;

import lombok.Getter;
import org.bson.Document;
import org.bson.types.Decimal128;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.walterinkitchen.parser.expression.*;
import org.walterinkitchen.parser.stage.AbsStage;
import org.walterinkitchen.parser.stage.FilterStage;

import java.util.*;

public class MatchAggregateChain extends AbsAggregateChain {

    protected MatchAggregateChain(AbsAggregateChain next) {
        super(next);
    }

    @Override
    Result handle(List<AbsStage> stages, Context context) {
        List<AggregationOperation> operations = new ArrayList<>();
        List<AbsStage> stageList = new ArrayList<>();
        for (AbsStage stage : stages) {
            if (!(stage instanceof FilterStage)) {
                stageList.add(stage);
                continue;
            }
            operations.addAll(convertMatchStage((FilterStage) stage, context));
        }

        return Result.build(operations, stageList);
    }

    private Collection<? extends AggregationOperation> convertMatchStage(FilterStage stage, Context context) {
        Expression expression = stage.getExpression();
        ExprContext exprContext = new ExprContext();
        expression.accept(Visitor.getInstance(), exprContext);
        List<AggregationOperation> operations = new ArrayList<>(exprContext.getOperations());
        if (!exprContext.getOptQ().isEmpty()) {
            Object expr = exprContext.optQ.pop();
            Document doc = new Document("$match", new Document("$expr", expr));
            operations.add(x -> doc);
        }

        return operations;
    }

    static class Visitor implements ExpressionVisitor<ExprContext, Void> {
        private static volatile Visitor instance;

        @Override
        public Void visit(BaseCompareExpression expression, ExprContext context) {
            expression.getExpr1().accept(this, context);
            Object expr1 = context.optQ.pop();
            expression.getExpr2().accept(this, context);
            Object expr2 = context.optQ.pop();

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
            context.optQ.push(expr);
            return null;
        }

        @Override
        public Void visit(AndExpression expression, ExprContext context) {
            List<Object> subExprs = new ArrayList<>();
            for (Expression expr : expression.getExpressions()) {
                expr.accept(this, context);
                subExprs.add(context.optQ.pop());
            }

            Document doc = new Document("$and", subExprs);
            context.optQ.push(doc);
            return null;
        }

        @Override
        public Void visit(OrExpression expression, ExprContext context) {
            List<Object> subExprs = new ArrayList<>();
            for (Expression expr : expression.getExpressions()) {
                expr.accept(this, context);
                subExprs.add(context.optQ.pop());
            }

            Document doc = new Document("$or", subExprs);
            context.optQ.push(doc);
            return null;
        }

        @Override
        public Void visit(NotExpression expression, ExprContext context) {
            expression.getExpression().accept(this, context);
            Object expr = context.optQ.pop();
            Document doc = new Document("$not", expr);
            context.optQ.push(doc);
            return null;
        }

        @Override
        public Void visit(FieldExpression expression, ExprContext context) {
            String field = "$" + expression.getField();
            context.optQ.push(field);
            return null;
        }

        @Override
        public Void visit(NumberLiteralExpression expression, ExprContext context) {
            Decimal128 decimal = new Decimal128(expression.getNum());
            context.optQ.push(decimal);
            return null;
        }

        @Override
        public Void visit(BooleanLiteralExpression expression, ExprContext context) {
            context.optQ.push(expression.isBool());
            return null;
        }

        @Override
        public Void visit(StringLiteralExpression expression, ExprContext context) {
            context.optQ.push(expression.getText());
            return null;
        }

        static Visitor getInstance() {
            if (instance == null) {
                synchronized (Visitor.class) {
                    if (instance == null) {
                        instance = new Visitor();
                    }
                }
            }
            return instance;
        }
    }

    static class ExprContext {
        @Getter
        private final List<AggregationOperation> operations = new ArrayList<>();

        @Getter
        private final Deque<Object> optQ = new LinkedList<>();
    }
}
