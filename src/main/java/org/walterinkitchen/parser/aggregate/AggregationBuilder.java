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
        EmptyAggregateChain tail = new EmptyAggregateChain(null);
        ProjectAggregateChain select = new ProjectAggregateChain(tail);
        LimitAggregateChain limit = new LimitAggregateChain(select);
        SortAggregateChain sort = new SortAggregateChain(limit);
        GroupByAggregateChain group = new GroupByAggregateChain(sort);
        MatchAggregateChain match = new MatchAggregateChain(group);
        FromAggregateChain from = new FromAggregateChain(match);
        AddIdAggregateChain id = new AddIdAggregateChain(from);
        return new AggregationBuilder(id);
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
