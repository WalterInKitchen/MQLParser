package org.walterinkitchen.parser.aggregate;

import org.bson.Document;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.walterinkitchen.parser.expression.AccumulatorExpression;
import org.walterinkitchen.parser.expression.Expression;
import org.walterinkitchen.parser.stage.AbsStage;
import org.walterinkitchen.parser.stage.GroupStage;
import org.walterinkitchen.parser.stage.ProjectStage;

import java.util.*;

/**
 * 构造GROUP BY聚合管道
 */
public class GroupByAggregateChain extends AbsAggregateChain {
    private final BaseExpressionVisitor expressionVisitor = BaseExpressionVisitor.getInstance();

    protected GroupByAggregateChain(AbsAggregateChain next) {
        super(next);
    }

    @Override
    Result handle(List<AbsStage> stages, Context context) {
        List<AggregationOperation> operations = new ArrayList<>();

        //filter all project stage
        List<AbsStage> stageList1 = new ArrayList<>();
        ProjectStage projectStage = null;
        for (AbsStage stage : stages) {
            if (!(stage instanceof ProjectStage)) {
                stageList1.add(stage);
                continue;
            }
            projectStage = (ProjectStage) stage;
        }

        //filter and process all groupBy stages
        List<AbsStage> stageList2 = new ArrayList<>();
        for (AbsStage stage : stageList1) {
            if (!(stage instanceof GroupStage)) {
                stageList2.add(stage);
                continue;
            }
            operations.addAll(convertGroupStage((GroupStage) stage, projectStage, context));
        }

        return Result.build(operations, stageList2);
    }

    private Collection<? extends AggregationOperation> convertGroupStage(GroupStage groupStage,
                                                                         ProjectStage projectStage,
                                                                         Context context) {
        //build group
        ExprContext ctx = new ExprContext();
        ctx.enterScope(ExprContext.Scope.GROUP_BY);
        Map<String, Object> groups = new HashMap<>();
        for (Expression expr : groupStage.getExpression().getExpressions()) {
            expr.accept(expressionVisitor, ctx);
            Object obj = ctx.getOptQ().pop();
            groups.put(ColumnNameProvider.obtainColumnName(expr), obj);
        }

        //build accumulators
        Map<String, Object> accumulators = new HashMap<>();
        List<ProjectStage.Field> fields = projectStage.getFields();
        for (ProjectStage.Field field : fields) {
            if (!(field.getExpression() instanceof AccumulatorExpression)) {
                continue;
            }
            field.getExpression().accept(expressionVisitor, ctx);
            Object accu = ctx.getOptQ().pop();
            if (field.getAlias() != null) {
                accumulators.put(field.getAlias(), accu);
                continue;
            }
            accumulators.put(ColumnNameProvider.obtainColumnName(field.getExpression()), accu);
        }

        //construct project pipeline
        Document group = new Document("$group", new HashMap<String, Object>() {{
            put("_id", groups);
            putAll(accumulators);
        }});

        List<AggregationOperation> operations = new ArrayList<>();
        operations.add(x -> group);
        HashMap<String, Object> pullUp = postGroup(groups);
        operations.add(x -> new Document("$addFields", pullUp));
        operations.add(x -> new Document("$project", new HashMap<String, Object>() {{
            put("_id", 0);
        }}));

        //map
        ctx.exitScope();
        return operations;
    }

    private HashMap<String, Object> postGroup(Map<String, Object> groups) {
        HashMap<String, Object> pullUp = new HashMap<>();
        for (String key : groups.keySet()) {
            pullUp.put(key, "$_id." + key);
        }
        return pullUp;
    }
}
