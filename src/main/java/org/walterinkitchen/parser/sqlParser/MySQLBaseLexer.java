package org.walterinkitchen.parser.sqlParser;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Lexer;

import java.util.HashSet;
import java.util.Set;

public abstract class MySQLBaseLexer extends Lexer {
    protected int serverVersion = 80000;

    protected final int NoBackslashEscapes = 100;
    protected final int PipesAsConcat = 101;
    protected final int HighNotPrecedence = 102;

    private final Set<String> charsets = new HashSet<>();

    SqlMode sqlMode = SqlMode.NoMode;
    boolean inVersionComment = false;

    public MySQLBaseLexer(CharStream input) {
        super(input);
    }

    @Override
    public void reset() {
        inVersionComment = false;
        super.reset();
    }

    protected boolean isSqlModeActive(int val) {
        return true;
    }

    protected int determineNumericType(String val) {
        Integer iv = null;
        try {
            iv = Integer.parseInt(val);
        } catch (NumberFormatException ignored) {
        }
        if (iv != null) {
            return MySQLLexer.INT_NUMBER;
        }

        Long lv = null;
        try {
            lv = Long.parseLong(val);
        } catch (NumberFormatException ignored) {
        }
        if (lv != null) {
            return MySQLLexer.LONG_NUMBER;
        }

        return MySQLLexer.DECIMAL_NUMBER;
    }

    protected int determineFunction(int val) {
        return 1;
    }

    protected int checkCharset(String val) {
        return charsets.contains(val) ? MySQLLexer.UNDERSCORE_CHARSET : MySQLLexer.IDENTIFIER;
    }

    protected boolean checkVersion(String val) {
        return true;
    }

    protected void emitDot() {

    }

    public enum SqlMode {
        NoMode,
        AnsiQuotes,
        HighNotPrecedence,
        PipesAsConcat,
        IgnoreSpace,
        NoBackslashEscapes;
    }
}
