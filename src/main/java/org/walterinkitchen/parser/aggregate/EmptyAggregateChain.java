package org.walterinkitchen.parser.aggregate;

import org.bson.Document;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.walterinkitchen.parser.stage.AbsStage;

import java.util.*;

public class EmptyAggregateChain extends AbsAggregateChain {
    protected EmptyAggregateChain(AbsAggregateChain next) {
        super(next);
    }

    @Override
    Result handle(List<AbsStage> stages, Context context) {
        Map<String, Object> project = new HashMap<>();
        project.put("_id", 0);

        List<AggregationOperation> operations =
                Collections.singletonList(x -> new Document("$project", project));
        return Result.build(operations, stages);
    }
}
