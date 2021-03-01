package org.walterinkitchen.parser.expression;

import lombok.Getter;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

public class ExprContext {
    @Getter
    private final List<AggregationOperation> operations = new ArrayList<>();

    @Getter
    private final Deque<Object> optQ = new LinkedList<>();

    private final Deque<Scope> scopes = new LinkedList<>();

    public Scope currentScope() {
        return scopes.peekLast();
    }

    public void enterScope(Scope scope) {
        this.scopes.push(scope);
    }

    public Scope exitScope() {
        return this.scopes.pop();
    }

    public enum Scope {
        GROUP_BY,
        SELECT;
    }
}
