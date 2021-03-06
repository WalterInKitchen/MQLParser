package org.walterinkitchen.parser.expression;

/**
 * Visitor for expression
 */
public interface ExpressionVisitor<C, T> {

    default T visitDefault(Expression expression, C context) {
        return null;
    }

    default T visit(StringLiteralExpression expression, C context) {
        return visitDefault(expression, context);
    }

    default T visit(FieldExpression expression, C context) {
        return visitDefault(expression, context);
    }

    default T visit(NumberLiteralExpression expression, C context) {
        return visitDefault(expression, context);
    }

    default T visit(NullLiteralExpression expression, C context) {
        return visitDefault(expression, context);
    }

    default T visit(BooleanLiteralExpression expression, C context) {
        return visitDefault(expression, context);
    }

    default T visit(ArithmeticExpression expression, C context) {
        return visitDefault(expression, context);
    }

    default T visit(LogicExpression expression, C context) {
        return visitDefault(expression, context);
    }

    default T visit(AndExpression expression, C context) {
        return visitDefault(expression, context);
    }

    default T visit(OrExpression expression, C context) {
        return visitDefault(expression, context);
    }

    default T visit(XorExpression expression, C context) {
        return visitDefault(expression, context);
    }

    default T visit(NotExpression expression, C context) {
        return visitDefault(expression, context);
    }

    default T visit(CompareExpression expression, C context) {
        return visitDefault(expression, context);
    }

    default T visit(CompareIsExpression expression, C context) {
        return visitDefault(expression, context);
    }

    default T visit(CompareLikeExpression expression, C context) {
        return visitDefault(expression, context);
    }

    default T visit(CompareSoundsLikeExpression expression, C context) {
        return visitDefault(expression, context);
    }

    default T visit(InArrayCompareExpression expression, C context) {
        return visitDefault(expression, context);
    }

    default T visit(BaseCompareExpression expression, C context) {
        return visitDefault(expression, context);
    }

    default T visit(SelectExpression expression, C context) {
        return visitDefault(expression, context);
    }

    default T visit(LimitExpression expression, C context) {
        return visitDefault(expression, context);
    }

    default T visit(ArrayExpression expression, C context) {
        return visitDefault(expression, context);
    }

    default T visit(AllElementExpression expression, C context) {
        return visitDefault(expression, context);
    }

    default T visit(FunctionCallExpression expression, C context) {
        return visitDefault(expression, context);
    }

    default T visit(GroupByExpression expression, C context) {
        return visitDefault(expression, context);
    }

    default T visit(AccumulatorExpression expression, C context) {
        return visitDefault(expression, context);
    }

    default T visit(BaseAccumulatorExpression expression, C context) {
        return visitDefault(expression, context);
    }

    default T visit(FirstLastExpression expression, C context) {
        return visitDefault(expression, context);
    }

    default T visit(CountExpression expression, C context) {
        return visitDefault(expression, context);
    }

    default T visit(IfNullExpression expression, C context) {
        return visitDefault(expression, context);
    }

    default T visit(StringConvertExpression expression, C context) {
        return visitDefault(expression, context);
    }

    default T visit(RoundExpression expression, C context) {
        return visitDefault(expression, context);
    }

    default T visit(RuntimeFunctionCallExpression expression, C context) {
        return visitDefault(expression, context);
    }

    default T visit(AbsFunctionExpression expression, C context) {
        return visitDefault(expression, context);
    }
}
