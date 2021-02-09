package org.walterinkitchen.parser.aggregate;

import lombok.Data;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.walterinkitchen.parser.stage.AbsStage;

import java.util.List;

public abstract class AbsAggregateChain {
    private final AbsAggregateChain next;

    protected AbsAggregateChain(AbsAggregateChain next) {
        this.next = next;
    }

    abstract Result handle(List<AbsStage> stages, Context context);

    protected Result doHandleStages(List<AbsStage> stages, Context context) {
        Result result = this.handle(stages, context);
        if (this.next != null) {
            Result rs1 = this.next.doHandleStages(result.stages, context);
            result.merge(rs1);
        }

        return result;
    }

    @Data
    static class Context {

    }

    @Data
    static class Result {
        private String collection;
        List<AggregationOperation> operations;
        List<AbsStage> stages;

        public void merge(Result result) {
            this.operations.addAll(result.getOperations());
            if (this.getCollection() == null && result.getCollection() != null) {
                this.setCollection(result.getCollection());
            }
        }

        public static Result build(List<AggregationOperation> operations, List<AbsStage> stages) {
            Result result = new Result();
            result.setOperations(operations);
            result.setStages(stages);
            return result;
        }
    }
}
