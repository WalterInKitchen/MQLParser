package org.walterinkitchen.parser.aggregate;

import org.bson.Document;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.walterinkitchen.parser.expression.*;
import org.walterinkitchen.parser.stage.AbsStage;
import org.walterinkitchen.parser.stage.CountStage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class CountAggregateChain extends EmptyAggregateChain {

    protected CountAggregateChain(AbsAggregateChain next) {
        super(next);
    }

    @Override
    Result handle(List<AbsStage> stages, Context context) {
        List<AggregationOperation> operations = new ArrayList<>();
        List<AbsStage> stageList = new ArrayList<>();
        for (AbsStage stage : stages) {
            if (!(stage instanceof CountStage)) {
                stageList.add(stage);
                continue;
            }
            operations.addAll(convertCountStage((CountStage) stage, context));
        }

        return Result.build(operations, stageList);
    }

    private Collection<? extends AggregationOperation> convertCountStage(CountStage stage, Context context) {
        List<CountExpression> expressions = stage.getExpressions();
        if (expressions.isEmpty()) {
            return Collections.emptyList();
        }
        List<AggregationOperation> operations = new ArrayList<>();
        CountExpression expr = expressions.get(0);
        if (expr.getExpr() instanceof FieldExpression) {
            //只统计非null字段
            Expression notNull = NotExpression.build(CompareIsExpression.build(expr.getExpr(), CompareIsExpression.Type.NULL));
            ExprContext exprContext = new ExprContext();
            notNull.accept(BaseExpressionVisitor.getInstance(), exprContext);
            if (!exprContext.getOptQ().isEmpty()) {
                Object exp = exprContext.getOptQ().pop();
                Document doc = new Document("$match", new Document("$expr", exp));
                operations.add(x -> doc);
            }
        }

        Document doc = new Document("$count", expr.getAlis() == null ? "count" : expr.getAlis());
        operations.add(x -> doc);
        return operations;
    }
}
