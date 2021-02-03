package org.walterinkitchen.parser;

import lombok.Data;
import org.walterinkitchen.parser.expression.Expression;
import org.walterinkitchen.parser.sqlParser.MySQLParser;
import org.walterinkitchen.parser.sqlParser.MySQLParserBaseVisitor;

public class GrammarVisitor extends MySQLParserBaseVisitor<GrammarVisitor.Result> {

    @Override
    public Result visitQueryExpression(MySQLParser.QueryExpressionContext ctx) {

        return super.visitQueryExpression(ctx);
    }

    @Override
    public Result visitQuerySpecification(MySQLParser.QuerySpecificationContext ctx) {
        return super.visitQuerySpecification(ctx);
    }

    @Override
    public Result visitQueryExpressionBody(MySQLParser.QueryExpressionBodyContext ctx) {
        return super.visitQueryExpressionBody(ctx);
    }

    @Data
    public static class Result {
        private Expression rootExpression;
    }
}
