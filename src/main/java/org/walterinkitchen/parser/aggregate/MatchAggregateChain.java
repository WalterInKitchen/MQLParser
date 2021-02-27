package org.walterinkitchen.parser.aggregate;

import org.bson.Document;
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
        expression.accept(BaseExpressionVisitor.getInstance(), exprContext);
        List<AggregationOperation> operations = new ArrayList<>(exprContext.getOperations());
        if (!exprContext.getOptQ().isEmpty()) {
            Object expr = exprContext.getOptQ().pop();
            Document doc = new Document("$match", new Document("$expr", expr));
            operations.add(x -> doc);
        }

        return operations;
    }

}
