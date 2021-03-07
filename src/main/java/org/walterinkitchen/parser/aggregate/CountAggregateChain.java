package org.walterinkitchen.parser.aggregate;

import org.bson.Document;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.walterinkitchen.parser.expression.CountExpression;
import org.walterinkitchen.parser.stage.AbsStage;
import org.walterinkitchen.parser.stage.CountStage;

import java.util.*;

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
        CountExpression expr = expressions.get(0);
        Document doc = new Document("$count", expr.getAlis() == null ? "count" : expr.getAlis());
        return Collections.singletonList(x -> doc);
    }
}
