package org.walterinkitchen.parser.aggregate;

import lombok.Data;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.walterinkitchen.parser.stage.AbsStage;

import java.util.List;

public class AggregationBuilder {

    private final AbsAggregateChain header;

    private AggregationBuilder(AbsAggregateChain header) {
        this.header = header;
    }

    public static AggregationBuilder buildDefaultBuilder() {
        LimitAggregateChain limit = new LimitAggregateChain(null);
        SortAggregateChain sort = new SortAggregateChain(limit);
        FromAggregateChain from = new FromAggregateChain(sort);
        return new AggregationBuilder(from);
    }

    public Result buildAggregation(List<AbsStage> stages) {
        AbsAggregateChain.Result result = header.doHandleStages(stages, new AbsAggregateChain.Context());
        return Result.build(result.getCollection(), result.getOperations());
    }

    @Data
    public static class Result {
        private String collection;
        private List<AggregationOperation> operations;

        public static Result build(String table, List<AggregationOperation> operations) {
            Result result = new Result();
            result.setCollection(table);
            result.setOperations(operations);
            return result;
        }
    }
}
