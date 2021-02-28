package org.walterinkitchen.parser.aggregate;

import org.walterinkitchen.parser.expression.Expression;
import org.walterinkitchen.parser.expression.ExpressionVisitor;
import org.walterinkitchen.parser.expression.FieldExpression;
import org.walterinkitchen.parser.expression.SumExpression;

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
        public String visit(SumExpression expression, Void context) {
            return "SUM";
        }

        @Override
        public String visit(FieldExpression expression, Void context) {
            return expression.getField();
        }
    }
}
