package org.walterinkitchen.parser.aggregate;

import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.walterinkitchen.parser.stage.AbsStage;
import org.walterinkitchen.parser.stage.FromStage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class FromAggregateChain extends AbsAggregateChain {

    protected FromAggregateChain(AbsAggregateChain next) {
        super(next);
    }

    @Override
    Result handle(List<AbsStage> stages, Context context) {
        List<AggregationOperation> operations = new ArrayList<>();
        List<AbsStage> stageList = new ArrayList<>();
        String table = null;
        for (AbsStage stage : stages) {
            if (!(stage instanceof FromStage)) {
                stageList.add(stage);
                continue;
            }
            FromStage fromStage = (FromStage) stage;
            table = fromStage.getTable();
            operations.addAll(convertFromStage(fromStage, context));
        }

        Result result = Result.build(operations, stageList);
        result.setCollection(table);
        return result;
    }

    private Collection<? extends AggregationOperation> convertFromStage(FromStage stage, Context context) {
        String table = stage.getTable();
        return Collections.emptyList();
    }
}
