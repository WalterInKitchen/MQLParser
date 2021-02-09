package org.walterinkitchen.parser.aggregate;

import org.bson.Document;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.walterinkitchen.parser.expression.Expression;
import org.walterinkitchen.parser.expression.FieldExpression;
import org.walterinkitchen.parser.misc.Direction;
import org.walterinkitchen.parser.stage.AbsStage;
import org.walterinkitchen.parser.stage.SortStage;

import java.util.*;

public class SortAggregateChain extends AbsAggregateChain {

    protected SortAggregateChain(AbsAggregateChain next) {
        super(next);
    }

    @Override
    Result handle(List<AbsStage> stages, Context context) {
        List<AggregationOperation> operations = new ArrayList<>();
        List<AbsStage> stageList = new ArrayList<>();
        for (AbsStage stage : stages) {
            if (!(stage instanceof SortStage)) {
                stageList.add(stage);
                continue;
            }
            operations.addAll(convertSortStage((SortStage) stage, context));
        }

        return Result.build(operations, stageList);
    }

    private Collection<? extends AggregationOperation> convertSortStage(SortStage stage, Context context) {
        List<AggregationOperation> operations = new ArrayList<>();
        Map<String, Integer> sortMap = new HashMap<>();
        for (SortStage.Option option : stage.getOptions()) {
            String fd = convertToFd(option.getField(), context);
            sortMap.put(fd, option.getDirection() == Direction.ASC ? 1 : -1);
        }
        operations.add(x -> new Document("$sort", sortMap));
        return operations;
    }

    private String convertToFd(Expression expression, Context context) {
        return ((FieldExpression) expression).getField();
    }
}
