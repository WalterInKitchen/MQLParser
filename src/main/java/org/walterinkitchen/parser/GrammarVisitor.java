package org.walterinkitchen.parser;

import lombok.Data;
import org.antlr.v4.runtime.tree.ParseTree;
import org.walterinkitchen.parser.exception.LimitClauseInvalidException;
import org.walterinkitchen.parser.expression.Expression;
import org.walterinkitchen.parser.expression.FieldExpression;
import org.walterinkitchen.parser.misc.Direction;
import org.walterinkitchen.parser.sqlParser.MySQLParser;
import org.walterinkitchen.parser.sqlParser.MySQLParserBaseVisitor;
import org.walterinkitchen.parser.stage.*;

import java.util.*;

public class GrammarVisitor extends MySQLParserBaseVisitor<GrammarVisitor.Result> {
    private final Context context = new Context();

    @Override
    public Result visitQueryExpression(MySQLParser.QueryExpressionContext ctx) {
        List<AbsStage> stages = new ArrayList<>();
        if (ctx.queryExpressionBody() != null) {
            ctx.queryExpressionBody().accept(this);
            @SuppressWarnings("unchecked")
            List<AbsStage> query = (List<AbsStage>) this.context.optQ.pop();
            stages.addAll(query);
        }

        if (ctx.orderClause() != null) {
            ctx.orderClause().accept(this);
            AbsStage sort = (AbsStage) this.context.optQ.pop();
            stages.add(sort);
        }

        if (ctx.limitClause() != null) {
            ctx.limitClause().accept(this);
            AbsStage limit = (AbsStage) this.context.optQ.pop();
            stages.add(limit);
        }

        return Result.build(stages);
    }

    @Override
    public Result visitQuerySpecification(MySQLParser.QuerySpecificationContext ctx) {
        List<AbsStage> stages = new ArrayList<>();

        //from
        if (ctx.fromClause() != null) {
            ctx.fromClause().accept(this);
            AbsStage fromStage = (AbsStage) this.context.optQ.pop();
            stages.add(fromStage);
        }

        //filter
        if (ctx.whereClause() != null) {
            ctx.whereClause().accept(this);
            AbsStage filterStage = (AbsStage) this.context.optQ.pop();
            stages.add(filterStage);
        }

        //project
        ctx.selectItemList().accept(this);
        ProjectStage projectStage = (ProjectStage) this.context.optQ.pop();
        stages.add(projectStage);

        this.context.optQ.push(stages);
        return null;
    }

    @Override
    public Result visitWhereClause(MySQLParser.WhereClauseContext ctx) {
        if (ctx.expr() == null) {
            FilterStage stage = new FilterStage();
            this.context.optQ.push(stage);
        }

        ctx.expr().accept(this);
        Expression expr = (Expression) this.context.optQ.pop();
        FilterStage filterStage = FilterStage.build(expr);
        this.context.optQ.push(filterStage);
        return null;
    }

    @Override
    public Result visitSelectItemList(MySQLParser.SelectItemListContext ctx) {
        if (ctx.MULT_OPERATOR() != null) {
            ProjectStage stage = ProjectStage.build(Collections.emptyList());
            this.context.optQ.push(stage);
            return null;
        }

        List<ProjectStage.Field> fields = new ArrayList<>();
        List<MySQLParser.SelectItemContext> itemsCtx = ctx.selectItem();
        for (MySQLParser.SelectItemContext itemContext : itemsCtx) {
            itemContext.accept(this);
            ProjectStage.Field fd = (ProjectStage.Field) this.context.optQ.pop();
            fields.add(fd);
        }

        ProjectStage stage = ProjectStage.build(fields);
        this.context.optQ.push(stage);
        return null;
    }

    @Override
    public Result visitSelectItem(MySQLParser.SelectItemContext ctx) {
        ctx.expr().accept(this);
        Expression expression = (Expression) this.context.optQ.pop();
        String alias = null;
        if (ctx.selectAlias() != null) {
            ctx.selectAlias().accept(this);
            alias = (String) this.context.optQ.pop();
        }
        ProjectStage.Field field = new ProjectStage.Field(expression, alias);
        this.context.optQ.push(field);
        return null;
    }

    @Override
    public Result visitTableRef(MySQLParser.TableRefContext ctx) {
        String table = ctx.qualifiedIdentifier().getText();
        FromStage stage = new FromStage(table);
        this.context.optQ.push(stage);
        return null;
    }

    @Override
    public Result visitOrderList(MySQLParser.OrderListContext ctx) {
        SortStage sortStage = new SortStage();
        List<MySQLParser.OrderExpressionContext> expressionContexts = ctx.orderExpression();
        for (MySQLParser.OrderExpressionContext expr : expressionContexts) {
            expr.accept(this);
            SortStage.Option option = (SortStage.Option) this.context.optQ.pop();
            sortStage.addOption(option);
        }

        this.context.optQ.push(sortStage);
        return null;
    }

    @Override
    public Result visitOrderExpression(MySQLParser.OrderExpressionContext ctx) {
        Direction direction = Direction.ASC;

        if (ctx.direction() != null) {
            ctx.direction().accept(this);
            direction = (Direction) this.context.optQ.pop();
        }

        ctx.expr().accept(this);
        Expression expression = (Expression) this.context.optQ.pop();
        SortStage.Option option = SortStage.Option.build(expression, direction);
        this.context.optQ.push(option);
        return null;
    }

    @Override
    public Result visitFieldIdentifier(MySQLParser.FieldIdentifierContext ctx) {
        String fieldText = ctx.getText();
        FieldExpression expression = FieldExpression.build(fieldText);
        this.context.optQ.push(expression);
        return null;
    }

    @Override
    public Result visitDirection(MySQLParser.DirectionContext ctx) {
        if (ctx.DESC_SYMBOL() != null) {
            this.context.optQ.push(Direction.DESC);
        } else {
            this.context.optQ.push(Direction.ASC);
        }
        return null;
    }

    @Override
    public Result visitLimitOptions(MySQLParser.LimitOptionsContext ctx) {
        LimitStage limitStage = new LimitStage();

        List<MySQLParser.LimitOptionContext> limitOptionContexts = ctx.limitOption();
        if (limitOptionContexts.size() == 0) {
            throw new LimitClauseInvalidException();
        } else if (limitOptionContexts.size() == 1) {
            limitOptionContexts.get(0).accept(this);
            long limit = (long) this.context.optQ.pop();
            limitStage.setOffset(0);
            limitStage.setSize(limit);
        } else {
            limitOptionContexts.get(0).accept(this);
            long offset = (long) this.context.optQ.pop();
            limitOptionContexts.get(1).accept(this);
            long limit = (long) this.context.optQ.pop();
            limitStage.setOffset(offset);
            limitStage.setSize(limit);
        }

        this.context.optQ.push(limitStage);
        return null;
    }

    @Override
    public Result visitLimitOption(MySQLParser.LimitOptionContext ctx) {
        ParseTree option = ctx.children.get(0);
        String text = option.getText();
        long lv = Long.parseLong(text);
        this.context.optQ.push(lv);
        return null;
    }

    @Data
    private static class Context {
        private Deque<Object> optQ = new LinkedList<>();
    }

    @Data
    public static class Result {
        private List<AbsStage> stages;

        public static Result build(List<AbsStage> stages) {
            Result result = new Result();
            result.setStages(stages);
            return result;
        }
    }
}
