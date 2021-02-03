package org.walterinkitchen.parser.sqlParser;

import org.antlr.v4.runtime.*;

public abstract class MySQLBaseRecognizer extends Parser {
    protected final int serverVersion = 80000;
    protected final int AnsiQuotes = 100;

    public MySQLBaseRecognizer(TokenStream input) {
        super(input);
    }


    protected boolean isSqlModeActive(int val) {
        return true;
    }
}
