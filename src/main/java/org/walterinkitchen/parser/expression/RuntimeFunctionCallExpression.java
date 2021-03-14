package org.walterinkitchen.parser.expression;

public class RuntimeFunctionCallExpression implements Expression {

    private Object obj;

    @Override
    public <C, T> T accept(ExpressionVisitor<C, T> visitor, C context) {
        return visitor.visit(this, context);
    }

    public Object explain() {
        return this.obj;
    }

    public static RuntimeFunctionCallExpression build(Object obj) {
        RuntimeFunctionCallExpression expression = new RuntimeFunctionCallExpression();
        expression.obj = obj;
        return expression;
    }
}
