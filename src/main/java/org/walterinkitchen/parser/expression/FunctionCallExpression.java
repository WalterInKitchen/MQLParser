package org.walterinkitchen.parser.expression;

import lombok.AccessLevel;
import lombok.Setter;
import org.bson.Document;

public class FunctionCallExpression implements Expression {

    @Setter(AccessLevel.PROTECTED)
    private Document document;

    @Override
    public <C, T> T accept(ExpressionVisitor<C, T> visitor, C context) {
        return visitor.visit(this, context);
    }

    public Document explain(){
        return document;
    }

    public static FunctionCallExpression build(Document document){
        FunctionCallExpression expression = new FunctionCallExpression();
        expression.setDocument(document);
        return expression;
    }
}
