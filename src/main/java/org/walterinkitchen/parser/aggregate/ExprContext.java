package org.walterinkitchen.parser.aggregate;

import lombok.Getter;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.walterinkitchen.parser.expression.Expression;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

class ExprContext {
    @Getter
    private final List<AggregationOperation> operations = new ArrayList<>();

    @Getter
    private final Deque<Object> optQ = new LinkedList<>();
    private final Deque<Expression> fatherExpr = new LinkedList<>();
}
