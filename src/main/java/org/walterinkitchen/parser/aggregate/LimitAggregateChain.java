package org.walterinkitchen.parser.aggregate;

import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.LimitOperation;
import org.springframework.data.mongodb.core.aggregation.SkipOperation;
import org.walterinkitchen.parser.stage.AbsStage;
import org.walterinkitchen.parser.stage.LimitStage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class LimitAggregateChain extends AbsAggregateChain {

    protected LimitAggregateChain(AbsAggregateChain next) {
        super(next);
    }

    @Override
    Result handle(List<AbsStage> stages, Context context) {
        List<AggregationOperation> operations = new ArrayList<>();
        List<AbsStage> stageList = new ArrayList<>();
        for (AbsStage stage : stages) {
            if (!(stage instanceof LimitStage)) {
                stageList.add(stage);
                continue;
            }
            operations.addAll(convertLimitStage((LimitStage) stage, context));
        }

        return Result.build(operations, stageList);
    }

    private Collection<? extends AggregationOperation> convertLimitStage(LimitStage stage, Context context) {
        SkipOperation skip = Aggregation.skip(stage.getOffset());
        LimitOperation limit = Aggregation.limit(stage.getSize());
        return Arrays.asList(skip, limit);
    }
}
