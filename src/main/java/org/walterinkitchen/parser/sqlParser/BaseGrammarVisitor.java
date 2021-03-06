package org.walterinkitchen.parser.sqlParser;

import lombok.Data;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNodeImpl;
import org.walterinkitchen.parser.exception.FunctionNotSupportedException;
import org.walterinkitchen.parser.exception.LimitClauseInvalidException;
import org.walterinkitchen.parser.expression.*;
import org.walterinkitchen.parser.function.Function;
import org.walterinkitchen.parser.function.FunctionProvider;
import org.walterinkitchen.parser.function.SimpleFunctionProvider;
import org.walterinkitchen.parser.misc.Direction;
import org.walterinkitchen.parser.stage.*;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

public class BaseGrammarVisitor extends MySQLParserBaseVisitor<BaseGrammarVisitor.Result> {
    private final Context context = new Context();

    private enum SelectOption {
        ALL,
        DISTINCT;
    }

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
            while (!this.context.optQ.isEmpty()) {
                AbsStage fromStage = (AbsStage) this.context.optQ.pop();
                stages.add(fromStage);
            }
        }

        //filter
        if (ctx.whereClause() != null) {
            ctx.whereClause().accept(this);
            while (!this.context.optQ.isEmpty()) {
                AbsStage filterStage = (AbsStage) this.context.optQ.pop();
                stages.add(filterStage);
            }
        }

        //groupBy
        if (ctx.groupByClause() != null) {
            ctx.groupByClause().accept(this);
            while (!this.context.optQ.isEmpty()) {
                AbsStage groupByStage = (AbsStage) this.context.optQ.pop();
                stages.add(groupByStage);
            }
        }

        //option
        if (!ctx.selectOption().isEmpty()) {
            Set<SelectOption> options = new HashSet<>();
            for (MySQLParser.SelectOptionContext option : ctx.selectOption()) {
                option.accept(this);
                options.add((SelectOption) this.context.optQ.pop());
            }
            this.context.optQ.push(options);
        }
        //project
        ctx.selectItemList().accept(this);
        while (!this.context.optQ.isEmpty()) {
            AbsStage projectStage = (AbsStage) this.context.optQ.pop();
            stages.add(projectStage);
        }

        this.context.optQ.push(stages);
        return null;
    }

    @Override
    public Result visitQuerySpecOption(MySQLParser.QuerySpecOptionContext ctx) {
        int type = ((TerminalNodeImpl) ctx.children.get(0)).getSymbol().getType();
        switch (type) {
            case MySQLLexer.DISTINCT_SYMBOL:
                this.context.optQ.push(SelectOption.DISTINCT);
                break;
            case MySQLLexer.ALL_SYMBOL:
                this.context.optQ.push(SelectOption.ALL);
                break;
        }
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
        Context.Stage stage = new SelectOrCountStageDecider().decide(ctx);

        //COUNT stage
        if (stage == Context.Stage.COUNT) {
            this.context.enterScope(Context.Stage.COUNT);
            List<CountExpression> countExpressions = new ArrayList<>();
            for (MySQLParser.SelectItemContext itemContext : ctx.selectItem()) {
                itemContext.accept(this);
                if (this.context.optQ.isEmpty()) {
                    continue;
                }
                countExpressions.add((CountExpression) this.context.optQ.pop());
            }
            CountStage countStage = CountStage.build(countExpressions);
            this.context.optQ.push(countStage);
            this.context.exitScope();
            return null;
        }

        this.context.enterScope(Context.Stage.SELECT);
        @SuppressWarnings("unchecked")
        Set<SelectOption> options = this.context.optQ.isEmpty() ?
                Collections.emptySet() : (Set<SelectOption>) this.context.optQ.pop();

        List<ProjectStage.Field> fields = new ArrayList<>();
        if (ctx.MULT_OPERATOR() != null) {
            fields.add(new ProjectStage.Field(new AllElementExpression(), null));
        }
        List<MySQLParser.SelectItemContext> itemsCtx = ctx.selectItem();
        for (MySQLParser.SelectItemContext itemContext : itemsCtx) {
            itemContext.accept(this);
            ProjectStage.Field fd = (ProjectStage.Field) this.context.optQ.pop();
            fields.add(fd);
        }

        //select -> group
        if (options.contains(SelectOption.DISTINCT)) {
            GroupByExpression groupByExpression = GroupByExpression
                    .buildByExprs(fields.stream()
                            .map(x -> new GroupByExpression.Expr(x.getExpression(), x.getAlias()))
                            .collect(Collectors.toList()));
            GroupStage groupStage = GroupStage.build(groupByExpression);
            this.context.optQ.push(groupStage);
        } else {
            ProjectStage projectStage = ProjectStage.build(fields);
            this.context.optQ.push(projectStage);
        }
        this.context.exitScope();
        return null;
    }

    @Override
    public Result visitSelectItem(MySQLParser.SelectItemContext ctx) {
        ctx.expr().accept(this);
        Expression expression = (Expression) this.context.optQ.pop();
        String alias = null;
        if (ctx.selectAlias() != null) {
            ctx.selectAlias().accept(this);
            Object alObj = this.context.optQ.pop();
            alias = ((StringLiteralExpression) alObj).getText();
        }
        //count
        if (this.context.currentScope() == Context.Stage.COUNT) {
            CountExpression count = CountExpression.build(expression, alias);
            this.context.optQ.push(count);
            return null;
        }

        ProjectStage.Field field = new ProjectStage.Field(expression, alias);
        this.context.optQ.push(field);
        return null;
    }

    @Override
    public Result visitPureIdentifier(MySQLParser.PureIdentifierContext ctx) {
        String text = ctx.getText();
        this.context.optQ.push(StringLiteralExpression.build(text));
        return null;
    }

    @Override
    public Result visitIdentifierKeyword(MySQLParser.IdentifierKeywordContext ctx) {
        String text = ctx.getText();
        this.context.optQ.push(StringLiteralExpression.build(text));
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
    public Result visitOrderClause(MySQLParser.OrderClauseContext ctx) {
        this.context.enterScope(Context.Stage.ORDER_BY);
        SortStage sortStage = new SortStage();
        ctx.orderList().accept(this);
        @SuppressWarnings("unchecked")
        List<SortStage.Option> options = (List<SortStage.Option>) this.context.optQ.pop();
        sortStage.setOptions(options);
        this.context.optQ.push(sortStage);
        this.context.exitScope();
        return null;
    }

    @Override
    public Result visitOrderList(MySQLParser.OrderListContext ctx) {
        List<MySQLParser.OrderExpressionContext> expressionContexts = ctx.orderExpression();
        switch (this.context.currentScope()) {
            case GROUP_BY:
                List<Expression> expressions = new ArrayList<>();
                for (MySQLParser.OrderExpressionContext expr : expressionContexts) {
                    expr.accept(this);
                    Expression expression = (Expression) this.context.optQ.pop();
                    expressions.add(expression);
                }
                this.context.optQ.push(expressions);
                break;

            case ORDER_BY:
                List<SortStage.Option> options = new ArrayList<>();
                for (MySQLParser.OrderExpressionContext expr : expressionContexts) {
                    expr.accept(this);
                    SortStage.Option option = (SortStage.Option) this.context.optQ.pop();
                    options.add(option);
                }
                this.context.optQ.push(options);
                break;
        }

        return null;
    }

    @Override
    public Result visitOrderExpression(MySQLParser.OrderExpressionContext ctx) {
        if (this.context.currentScope() == Context.Stage.GROUP_BY) {
            ctx.expr().accept(this);
            Expression expression = (Expression) this.context.optQ.pop();
            this.context.optQ.push(expression);
        } else if (this.context.currentScope() == Context.Stage.ORDER_BY) {
            Direction direction = Direction.ASC;
            if (ctx.direction() != null) {
                ctx.direction().accept(this);
                direction = (Direction) this.context.optQ.pop();
            }
            ctx.expr().accept(this);
            Expression expression = (Expression) this.context.optQ.pop();
            SortStage.Option option = SortStage.Option.build(expression, direction);
            this.context.optQ.push(option);
        }
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
    public Result visitPredicateExprLike(MySQLParser.PredicateExprLikeContext ctx) {
        Expression expr1 = (Expression) this.context.optQ.pop();
        ctx.simpleExpr().get(0).accept(this);
        Expression expr2 = (Expression) this.context.optQ.pop();
        Expression escape = null;
        if (ctx.simpleExpr().size() == 2) {
            ctx.simpleExpr().get(1).accept(this);
            escape = (Expression) this.context.optQ.pop();
        }
        CompareLikeExpression expression = CompareLikeExpression.build(expr1, expr2, escape);
        this.context.optQ.push(expression);
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

        //对文本应用between
        if (expr1 instanceof StringLiteralExpression
                || expr2 instanceof StringLiteralExpression) {
            InArrayCompareExpression inExpr = InArrayCompareExpression
                    .build(expression, ArrayExpression.build(Arrays.asList(expr1, expr2)));
            this.context.optQ.push(inExpr);
            return null;
        }

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
    public Result visitTextStringLiteral(MySQLParser.TextStringLiteralContext ctx) {
        String text = ctx.value.getText();
        text = text.substring(1, text.length() - 1);
        this.context.optQ.push(StringLiteralExpression.build(text));
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

    @Override
    public Result visitFunctionCall(MySQLParser.FunctionCallContext ctx) {
        if (ctx.pureIdentifier() != null) {
            String funcName = ctx.pureIdentifier().getText();
            ctx.udfExprList().accept(this);
            @SuppressWarnings("unchecked")
            List<Expression> expressions = (List<Expression>) this.context.optQ.pop();
            Function function = this.context.functionProvider.getFunctionByName(funcName);
            if (function == null) {
                throw new FunctionNotSupportedException(funcName);
            }
            Expression expression = function.call(expressions);
            this.context.optQ.push(expression);
        } else if (ctx.qualifiedIdentifier() != null) {
            String funcName = ctx.qualifiedIdentifier().getText();
            ctx.exprList().accept(this);
            @SuppressWarnings("unchecked")
            List<Expression> expressions = ((ArrayExpression) this.context.optQ.pop()).getExpressions();
            Function function = this.context.functionProvider.getFunctionByName(funcName);
            if (function == null) {
                throw new FunctionNotSupportedException(funcName);
            }
            Expression expression = function.call(expressions);
            this.context.optQ.push(expression);
        }
        return null;
    }

    @Override
    public Result visitUdfExprList(MySQLParser.UdfExprListContext ctx) {
        List<Expression> expressions = new ArrayList<>(ctx.udfExpr().size());
        for (MySQLParser.UdfExprContext expr : ctx.udfExpr()) {
            expr.accept(this);
            expressions.add((Expression) this.context.optQ.pop());
        }
        this.context.optQ.push(expressions);
        return null;
    }

    @Override
    public Result visitGroupByClause(MySQLParser.GroupByClauseContext ctx) {
        this.context.enterScope(Context.Stage.GROUP_BY);
        ctx.orderList().accept(this);
        @SuppressWarnings("unchecked")
        List<Expression> expressions = (List<Expression>) this.context.optQ.pop();
        GroupByExpression groupByExpression = GroupByExpression.build(expressions);
        GroupStage stage = GroupStage.build(groupByExpression);
        this.context.optQ.push(stage);
        this.context.exitScope();
        return null;
    }

    @Override
    public Result visitSumExpr(MySQLParser.SumExprContext ctx) {
        BaseAccumulatorExpression.Type type = null;
        switch (ctx.name.getType()) {
            case MySQLLexer.SUM_SYMBOL:
                type = BaseAccumulatorExpression.Type.SUM;
                break;
            case MySQLLexer.MAX_SYMBOL:
                type = BaseAccumulatorExpression.Type.MAX;
                break;
            case MySQLLexer.MIN_SYMBOL:
                type = BaseAccumulatorExpression.Type.MIN;
                break;
            case MySQLLexer.AVG_SYMBOL:
                type = BaseAccumulatorExpression.Type.AVG;
                break;
        }
        Expression expression = null;
        if (ctx.inSumExpr() != null) {
            ctx.inSumExpr().accept(this);
            expression = (Expression) this.context.optQ.pop();
        } else if (ctx.MULT_OPERATOR() != null) {
            expression = new AllElementExpression();
        }

        if (this.context.currentScope().equals(Context.Stage.COUNT)) {
            this.context.optQ.push(expression);
            return null;
        }

        BaseAccumulatorExpression baseAccumulatorExpression = BaseAccumulatorExpression.build(type, expression);
        this.context.optQ.push(baseAccumulatorExpression);
        return null;
    }

    @Override
    public Result visitInSumExpr(MySQLParser.InSumExprContext ctx) {
        ctx.expr().accept(this);
        return null;
    }

    @Override
    public Result visitRuntimeFunctionCall(MySQLParser.RuntimeFunctionCallContext ctx) {
        String funcName = ctx.name.getText();
        List<Expression> args = new ArrayList<>();
        if (ctx.timeFunctionParameters() != null) {
            ctx.timeFunctionParameters().accept(this);
            if (!this.context.optQ.isEmpty()) {
                @SuppressWarnings("unchecked")
                List<Expression> list = (List<Expression>) this.context.optQ.pop();
                args.addAll(list);
            }
        }
        if (ctx.expr() != null) {
            for (MySQLParser.ExprContext expr : ctx.expr()) {
                expr.accept(this);
                if (!this.context.optQ.isEmpty()) {
                    @SuppressWarnings("unchecked")
                    Expression expression = (Expression) this.context.optQ.pop();
                    args.add(expression);
                }
            }
        }

        Function function = this.context.getFunctionProvider().getRuntimeFunctionByName(funcName);
        Expression expression = function.call(args);
        this.context.optQ.push(expression);
        return null;
    }

    @Data
    private static class Context {
        private Deque<Object> optQ = new LinkedList<>();
        private FunctionProvider functionProvider = new SimpleFunctionProvider();
        private Deque<Stage> stage = new LinkedList<>();

        public Stage currentScope() {
            return this.stage.peekLast();
        }

        public void enterScope(Stage stage) {
            this.stage.push(stage);
        }

        public Stage exitScope() {
            return this.stage.pop();
        }

        enum Stage {
            COUNT,
            SELECT,
            GROUP_BY,
            ORDER_BY
        }
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

    /**
     * 判断SELECT语句构造那种管道
     */
    protected static class SelectOrCountStageDecider extends MySQLParserBaseVisitor<Void> {
        private final Context context = new Context();

        public Context.Stage decide(MySQLParser.SelectItemListContext ctx) {
            ctx.accept(this);
            if (this.context.optQ.isEmpty()) {
                return Context.Stage.SELECT;
            }

            return (Context.Stage) context.optQ.pop();
        }

        @Override
        public Void visitSumExpr(MySQLParser.SumExprContext ctx) {
            if (ctx.name.getType() == MySQLLexer.COUNT_SYMBOL) {
                this.context.optQ.push(Context.Stage.COUNT);
            }
            return null;
        }
    }
}
