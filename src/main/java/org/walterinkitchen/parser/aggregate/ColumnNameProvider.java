package org.walterinkitchen.parser.aggregate;

import org.walterinkitchen.parser.expression.*;

/**
 * 得到表达式的列字符串
 */
public class ColumnNameProvider {
    private static final Visitor visitor = new Visitor();

    private ColumnNameProvider() {
    }

    static String obtainColumnName(Expression expression) {
        return expression.accept(visitor, null);
    }

    private static class Visitor implements ExpressionVisitor<Void, String> {
        @Override
        public String visitDefault(Expression expression, Void context) {
            return "";
        }

        @Override
        public String visit(FirstLastExpression expression, Void context) {
            return expression.getType().keyWord();
        }

        @Override
        public String visit(ArithmeticExpression expression, Void context) {
            return expression.getOperator().name();
        }

        @Override
        public String visit(BaseAccumulatorExpression expression, Void context) {
            return expression.getType().keyWord();
        }

        @Override
        public String visit(FieldExpression expression, Void context) {
            return expression.getField();
        }

        @Override
        public String visit(StringLiteralExpression expression, Void context) {
            return expression.getText();
        }
    }
}
