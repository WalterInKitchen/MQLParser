package org.walterinkitchen.parser;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.ATNConfigSet;
import org.antlr.v4.runtime.dfa.DFA;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.walterinkitchen.parser.aggregate.AggregationBuilder;
import org.walterinkitchen.parser.sqlParser.BaseGrammarVisitor;
import org.walterinkitchen.parser.sqlParser.MySQLLexer;
import org.walterinkitchen.parser.sqlParser.MySQLParser;

import java.util.BitSet;
import java.util.List;

/**
 * MongoProvider的基础实现
 */
public class BaseMongoProvider implements MongoProvider, ANTLRErrorListener {

    private final MongoTemplate mongoTemplate;
    private final AggregationBuilder aggregationBuilder;

    public BaseMongoProvider(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
        this.aggregationBuilder = AggregationBuilder.buildDefaultBuilder();
    }

    @Override
    public <T> List<T> query(String ql, Class<T> outputType) {
        MySQLLexer lexer = new MySQLLexer(CharStreams.fromString(ql));
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        MySQLParser parser = new MySQLParser(tokenStream);
        parser.removeErrorListeners();
        parser.addErrorListener(this);
        MySQLParser.QueryExpressionContext query = parser.queryExpression();

        BaseGrammarVisitor visitor = new BaseGrammarVisitor();
        BaseGrammarVisitor.Result result = query.accept(visitor);

        AggregationBuilder.Result ares = aggregationBuilder.buildAggregation(result.getStages());
        AggregationResults<T> aggregationResults = mongoTemplate.aggregate(Aggregation.newAggregation(ares.getOperations()), ares.getCollection(), outputType);
        return aggregationResults.getMappedResults();
    }

    @Override
    public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
        throw e;
    }

    @Override
    public void reportAmbiguity(Parser recognizer, DFA dfa, int startIndex, int stopIndex, boolean exact, BitSet ambigAlts, ATNConfigSet configs) {

    }

    @Override
    public void reportAttemptingFullContext(Parser recognizer, DFA dfa, int startIndex, int stopIndex, BitSet conflictingAlts, ATNConfigSet configs) {

    }

    @Override
    public void reportContextSensitivity(Parser recognizer, DFA dfa, int startIndex, int stopIndex, int prediction, ATNConfigSet configs) {

    }
}
