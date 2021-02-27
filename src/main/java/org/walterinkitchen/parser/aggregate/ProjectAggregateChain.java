package org.walterinkitchen.parser.aggregate;

import org.bson.Document;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.walterinkitchen.parser.expression.*;
import org.walterinkitchen.parser.stage.AbsStage;
import org.walterinkitchen.parser.stage.ProjectStage;

import java.util.*;

public class ProjectAggregateChain extends AbsAggregateChain {
    private final BaseExpressionVisitor expressionVisitor = BaseExpressionVisitor.getInstance();

    protected ProjectAggregateChain(AbsAggregateChain next) {
        super(next);
    }

    @Override
    Result handle(List<AbsStage> stages, AbsAggregateChain.Context context) {
        List<AggregationOperation> operations = new ArrayList<>();
        List<AbsStage> stageList = new ArrayList<>();
        for (AbsStage stage : stages) {
            if (!(stage instanceof ProjectStage)) {
                stageList.add(stage);
                continue;
            }
            operations.addAll(convertProjectStage((ProjectStage) stage, context));
        }

        return Result.build(operations, stageList);
    }

    private Collection<? extends AggregationOperation> convertProjectStage(ProjectStage stage, AbsAggregateChain.Context context) {
        int mode = 1;
        Map<String, Object> project = new HashMap<>();
        for (ProjectStage.Field field : stage.getFields()) {
            if (field.getExpression() instanceof AllElementExpression) {
                mode = 2;
                continue;
            }
            Expression expression = field.getExpression();
            ExprContext ctx = new ExprContext();
            expression.accept(expressionVisitor, ctx);
            Object expr = ctx.getOptQ().pop();
            if (field.getAlias() != null) {
                project.put(field.getAlias(), expr);
                continue;
            }
            if (mode == 1) {
                project.put(String.valueOf(expr), 1);
            }
        }

        String finalKeyWord = mode == 2 ? "$addFields" : "$project";
        return project.isEmpty() ? Collections.emptyList() : Collections.singletonList(x -> new Document(finalKeyWord, project));
    }

    protected class Context {
        private final Deque<Object> optQ = new LinkedList<>();
        private String alias;
    }
}
