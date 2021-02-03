package org.walterinkitchen.parser;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.ATNConfigSet;
import org.antlr.v4.runtime.dfa.DFA;
import org.walterinkitchen.parser.expression.Expression;
import org.walterinkitchen.parser.sqlParser.MySQLLexer;
import org.walterinkitchen.parser.sqlParser.MySQLParser;

import java.util.BitSet;
import java.util.List;

public class BaseMongoProvider implements MongoProvider, ANTLRErrorListener {

    @Override
    public <T> List<T> query(String ql, Class<T> outputType) {
        MySQLLexer lexer = new MySQLLexer(CharStreams.fromString(ql));
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        MySQLParser parser = new MySQLParser(tokenStream);
        parser.removeErrorListeners();
        parser.addErrorListener(this);
        MySQLParser.QueryContext query = parser.query();

        GrammarVisitor visitor = new GrammarVisitor();
        GrammarVisitor.Result result = query.accept(visitor);

        Expression rootExpression = result.getRootExpression();

        return null;
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
