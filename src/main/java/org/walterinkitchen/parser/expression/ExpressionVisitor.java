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
}
