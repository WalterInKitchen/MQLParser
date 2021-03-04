package org.walterinkitchen.parser.aggregate;

import org.bson.Document;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.util.StringUtils;
import org.walterinkitchen.parser.expression.AccumulatorExpression;
import org.walterinkitchen.parser.expression.ExprContext;
import org.walterinkitchen.parser.expression.Expression;
import org.walterinkitchen.parser.expression.GroupByExpression;
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

        List<AbsStage> remainStages = new ArrayList<>();
        //filter and process all groupBy stages
        List<GroupStage> groupStages = new ArrayList<>();
        List<AbsStage> stages1 = new ArrayList<>();
        for (AbsStage stage : stages) {
            if (!(stage instanceof GroupStage)) {
                stages1.add(stage);
                continue;
            }
            groupStages.add((GroupStage) stage);
        }

        //filter all project stage
        ProjectStage projectStage = null;
        for (AbsStage stage : stages1) {
            if (!(stage instanceof ProjectStage)
                    || groupStages.isEmpty()) {
                remainStages.add(stage);
                continue;
            }
            projectStage = (ProjectStage) stage;
        }
        for (GroupStage stage : groupStages) {
            operations.addAll(convertGroupStage(stage, projectStage, context));
        }

        return Result.build(operations, remainStages);
    }

    private Collection<? extends AggregationOperation> convertGroupStage(GroupStage groupStage,
                                                                         ProjectStage projectStage,
                                                                         Context context) {
        //build group
        ExprContext ctx = new ExprContext();
        ctx.enterScope(ExprContext.Scope.GROUP_BY);
        Map<String, Object> groups = new HashMap<>();
        for (GroupByExpression.Expr expr : groupStage.getExpression().getExpressions()) {
            Expression expression = expr.getExpression();
            expression.accept(expressionVisitor, ctx);
            Object obj = ctx.getOptQ().pop();
            String colName = expr.getAlias();
            if (StringUtils.isEmpty(colName)) {
                colName = ColumnNameProvider.obtainColumnName(expression);
            }
            groups.put(colName, obj);
        }

        //build accumulators
        Map<String, Object> accumulators = new HashMap<>();
        if (projectStage != null) {
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
        }

        //construct project pipeline
        Document group = new Document("$group", new HashMap<String, Object>() {{
            put("_id", groups);
            putAll(accumulators);
        }});

        List<AggregationOperation> operations = new ArrayList<>();
        operations.add(x -> group);
        operations.addAll(postGroup(groups));

        //map
        ctx.exitScope();
        return operations;
    }

    private Collection<? extends AggregationOperation> postGroup(Map<String, Object> groups) {
        //pull field named _id.xxxx to top level
        List<AggregationOperation> operations = new ArrayList<>();
        HashMap<String, Object> pullUp = new HashMap<>();
        for (String key : groups.keySet()) {
            pullUp.put(key, "$_id." + key);
        }
        operations.add(x -> new Document("$addFields", pullUp));

        //hide _id field
        operations.add(x -> new Document("$project", new HashMap<String, Object>() {{
            put("_id", 0);
        }}));
        return operations;
    }
}
