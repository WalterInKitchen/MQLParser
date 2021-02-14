package org.walterinkitchen.parser;

import lombok.Data;
import org.antlr.v4.runtime.tree.ParseTree;
import org.walterinkitchen.parser.exception.LimitClauseInvalidException;
import org.walterinkitchen.parser.expression.*;
import org.walterinkitchen.parser.misc.Direction;
import org.walterinkitchen.parser.sqlParser.MySQLLexer;
import org.walterinkitchen.parser.sqlParser.MySQLParser;
import org.walterinkitchen.parser.sqlParser.MySQLParserBaseVisitor;
import org.walterinkitchen.parser.stage.*;

import java.math.BigDecimal;
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
    public Result visitPureIdentifier(MySQLParser.PureIdentifierContext ctx) {
        String text = ctx.getText();
        this.context.optQ.push(text);
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

    @Override
    public Result visitExprAnd(MySQLParser.ExprAndContext ctx) {
        ctx.expr().forEach(x -> x.accept(this));
        Expression expr2 = (Expression) this.context.optQ.pop();
        Expression expr1 = (Expression) this.context.optQ.pop();
        AndExpression and = AndExpression.build(Arrays.asList(expr1, expr2));
        this.context.optQ.push(and);
        return null;
    }

    @Override
    public Result visitExprOr(MySQLParser.ExprOrContext ctx) {
        ctx.expr().forEach(x -> x.accept(this));
        Expression expr2 = (Expression) this.context.optQ.pop();
        Expression expr1 = (Expression) this.context.optQ.pop();
        OrExpression or = OrExpression.build(Arrays.asList(expr1, expr2));
        this.context.optQ.push(or);
        return null;
    }

    @Override
    public Result visitExprXor(MySQLParser.ExprXorContext ctx) {
        ctx.expr().forEach(x -> x.accept(this));
        Expression expr2 = (Expression) this.context.optQ.pop();
        Expression expr1 = (Expression) this.context.optQ.pop();
        XorExpression xor = XorExpression.build(Arrays.asList(expr1, expr2));
        this.context.optQ.push(xor);
        return null;
    }

    @Override
    public Result visitExprNot(MySQLParser.ExprNotContext ctx) {
        ctx.expr().accept(this);
        Expression expr = (Expression) this.context.optQ.pop();
        expr = NotExpression.build(expr);
        this.context.optQ.push(expr);
        return null;
    }

    @Override
    public Result visitExprIs(MySQLParser.ExprIsContext ctx) {
        ctx.boolPri().accept(this);
        Expression expression = (Expression) this.context.optQ.pop();
        CompareIsExpression.Type type = null;
        if (ctx.FALSE_SYMBOL() != null) {
            type = CompareIsExpression.Type.FALSE;
        } else if (ctx.TRUE_SYMBOL() != null) {
            type = CompareIsExpression.Type.TRUE;
        } else if (ctx.UNKNOWN_SYMBOL() != null) {
            type = CompareIsExpression.Type.UNKNOWN;
        }
        if (type != null) {
            expression = CompareIsExpression.build(expression, type);
            if (ctx.notRule() != null) {
                expression = NotExpression.build(expression);
            }
        }
        this.context.optQ.push(expression);
        return null;
    }

    @Override
    public Result visitPrimaryExprIsNull(MySQLParser.PrimaryExprIsNullContext ctx) {
        ctx.boolPri().accept(this);
        Expression expression = (Expression) this.context.optQ.pop();
        expression = CompareIsExpression.build(expression, CompareIsExpression.Type.NULL);
        if (ctx.notRule() != null) {
            expression = NotExpression.build(expression);
        }
        this.context.optQ.push(expression);
        return null;
    }

    @Override
    public Result visitPrimaryExprCompare(MySQLParser.PrimaryExprCompareContext ctx) {
        ctx.compOp().accept(this);
        BaseCompareExpression.Comparator comparator = (BaseCompareExpression.Comparator) this.context.optQ.pop();
        ctx.boolPri().accept(this);
        Expression expr1 = (Expression) this.context.optQ.pop();
        ctx.predicate().accept(this);
        Expression expr2 = (Expression) this.context.optQ.pop();

        BaseCompareExpression expression = BaseCompareExpression.build(expr1, comparator, expr2);
        this.context.optQ.push(expression);
        return null;
    }

    @Override
    public Result visitPrimaryExprAllAny(MySQLParser.PrimaryExprAllAnyContext ctx) {
        ctx.compOp().accept(this);
        BaseCompareExpression.Comparator comparator = (BaseCompareExpression.Comparator) this.context.optQ.pop();
        ctx.boolPri().accept(this);
        Expression expr1 = (Expression) this.context.optQ.pop();
        ctx.subquery().accept(this);
        Expression expr2 = (Expression) this.context.optQ.pop();

        AllAnyCompareExpression.Type type = null;
        if (ctx.ALL_SYMBOL() != null) {
            type = AllAnyCompareExpression.Type.ALL;
        } else if (ctx.ANY_SYMBOL() != null) {
            type = AllAnyCompareExpression.Type.ANY;
        }
        AllAnyCompareExpression expression = AllAnyCompareExpression.build(type, expr1, comparator, expr2);
        this.context.optQ.push(expression);
        return null;
    }

    @Override
    public Result visitPredicate(MySQLParser.PredicateContext ctx) {
        Expression expression = null;
        ctx.bitExpr().forEach(x -> x.accept(this));
        if (ctx.predicateOperations() != null) {
            Expression expr1 = (Expression) this.context.optQ.pop();
            this.context.optQ.push(expr1);
            ctx.predicateOperations().accept(this);
            expression = (Expression) this.context.optQ.pop();

            if (ctx.notRule() != null) {
                expression = NotExpression.build(expression);
            }

            this.context.optQ.push(expression);
            return null;
        }
        if (ctx.SOUNDS_SYMBOL() != null && ctx.LIKE_SYMBOL() != null) {
            Expression expr2 = (Expression) this.context.optQ.pop();
            Expression expr1 = (Expression) this.context.optQ.pop();
            expression = CompareSoundsLikeExpression.build(expr1, expr2);
            this.context.optQ.push(expression);
            return null;
        }

        return null;
    }

    @Override
    public Result visitPredicateExprIn(MySQLParser.PredicateExprInContext ctx) {
        Expression expr1 = (Expression) this.context.optQ.pop();
        Expression expr2 = null;
        if (ctx.subquery() != null) {
            ctx.subquery().accept(this);
            expr2 = (Expression) this.context.optQ.pop();
        }

        if (ctx.exprList() != null) {
            ctx.exprList().accept(this);
            expr2 = (Expression) this.context.optQ.pop();
        }

        InArrayCompareExpression expression = InArrayCompareExpression.build(expr1, expr2);
        this.context.optQ.push(expression);
        return null;
    }

    @Override
    public Result visitExprList(MySQLParser.ExprListContext ctx) {
        List<Expression> expressions = new ArrayList<>(ctx.expr().size());
        for (MySQLParser.ExprContext expr : ctx.expr()) {
            expr.accept(this);
            Expression expression = (Expression) this.context.optQ.pop();
            expressions.add(expression);
        }
        ArrayExpression arrayExpression = ArrayExpression.build(expressions);
        this.context.optQ.push(arrayExpression);
        return null;
    }

    @Override
    public Result visitPredicateExprBetween(MySQLParser.PredicateExprBetweenContext ctx) {
        Expression expression = (Expression) this.context.optQ.pop();
        ctx.bitExpr().accept(this);
        Expression expr1 = (Expression) this.context.optQ.pop();
        ctx.predicate().accept(this);
        Expression expr2 = (Expression) this.context.optQ.pop();

        BaseCompareExpression greaterThanSmaller = BaseCompareExpression.build(expression, BaseCompareExpression.Comparator.GTE, expr1);
        BaseCompareExpression smallerThanBigger = BaseCompareExpression.build(expression, BaseCompareExpression.Comparator.LTE, expr2);
        expression = AndExpression.build(Arrays.asList(greaterThanSmaller, smallerThanBigger));
        this.context.optQ.push(expression);
        return null;
    }

    @Override
    public Result visitBitExpr(MySQLParser.BitExprContext ctx) {
        if (ctx.simpleExpr() != null) {
            ctx.simpleExpr().accept(this);
            return null;
        }

        ctx.bitExpr().forEach(e -> e.accept(this));
        Expression expr2 = (Expression) this.context.optQ.pop();
        Expression expr1 = (Expression) this.context.optQ.pop();

        //todo:impl
        switch (ctx.op.getType()) {
            case MySQLLexer.BITWISE_XOR_OPERATOR:
                break;
            case MySQLLexer.MULT_OPERATOR:
                this.context.optQ.push(ArithmeticExpression.build(expr1, ArithmeticExpression.Operator.MULTI, expr2));
                return null;
            case MySQLLexer.DIV_OPERATOR:
            case MySQLLexer.DIV_SYMBOL:
                this.context.optQ.push(ArithmeticExpression.build(expr1, ArithmeticExpression.Operator.DIV, expr2));
                return null;
            case MySQLLexer.MOD_SYMBOL:
            case MySQLLexer.MOD_OPERATOR:
                this.context.optQ.push(ArithmeticExpression.build(expr1, ArithmeticExpression.Operator.MOD, expr2));
                return null;

            case MySQLLexer.PLUS_OPERATOR:
                this.context.optQ.push(ArithmeticExpression.build(expr1, ArithmeticExpression.Operator.PLUS, expr2));
                return null;
            case MySQLLexer.MINUS_OPERATOR:
                this.context.optQ.push(ArithmeticExpression.build(expr1, ArithmeticExpression.Operator.MINUS, expr2));
                return null;
        }

        return null;
    }

    @Override
    public Result visitCompOp(MySQLParser.CompOpContext ctx) {
        BaseCompareExpression.Comparator comparator = null;

        if (ctx.EQUAL_OPERATOR() != null) {
            comparator = BaseCompareExpression.Comparator.EQ;
        } else if (ctx.NULL_SAFE_EQUAL_OPERATOR() != null) {
            comparator = BaseCompareExpression.Comparator.NULL_SAFE_EQ;
        } else if (ctx.GREATER_OR_EQUAL_OPERATOR() != null) {
            comparator = BaseCompareExpression.Comparator.GTE;
        } else if (ctx.GREATER_THAN_OPERATOR() != null) {
            comparator = BaseCompareExpression.Comparator.GT;
        } else if (ctx.LESS_OR_EQUAL_OPERATOR() != null) {
            comparator = BaseCompareExpression.Comparator.LTE;
        } else if (ctx.LESS_THAN_OPERATOR() != null) {
            comparator = BaseCompareExpression.Comparator.LT;
        } else if (ctx.NOT_EQUAL_OPERATOR() != null) {
            comparator = BaseCompareExpression.Comparator.NEQ;
        }

        this.context.optQ.push(comparator);
        return null;
    }

    @Override
    public Result visitTextLiteral(MySQLParser.TextLiteralContext ctx) {
        StringLiteralExpression expression = StringLiteralExpression.build(ctx.getText());
        this.context.optQ.push(expression);
        return null;
    }

    @Override
    public Result visitNumLiteral(MySQLParser.NumLiteralContext ctx) {
        BigDecimal decimal = new BigDecimal(ctx.getText());
        NumberLiteralExpression expression = NumberLiteralExpression.build(decimal);
        this.context.optQ.push(expression);
        return null;
    }

    @Override
    public Result visitNullLiteral(MySQLParser.NullLiteralContext ctx) {
        this.context.optQ.push(NullLiteralExpression.build());
        return null;
    }

    @Override
    public Result visitBoolLiteral(MySQLParser.BoolLiteralContext ctx) {
        BooleanLiteralExpression expression = null;
        if (ctx.TRUE_SYMBOL() != null) {
            expression = BooleanLiteralExpression.build(true);
        } else if (ctx.FALSE_SYMBOL() != null) {
            expression = BooleanLiteralExpression.build(false);
        }
        this.context.optQ.push(expression);
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
