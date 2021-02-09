package org.walterinkitchen.parser.stage;

import lombok.Getter;
import org.walterinkitchen.parser.expression.Expression;

public class FilterStage extends AbsStage {

    @Getter
    private Expression expression;

    @Override
    <T, C> T accept(StageVisitor<T, C> visitor, C context) {
        return visitor.visit(this, context);
    }

    public static FilterStage build(Expression expression) {
        FilterStage stage = new FilterStage();
        stage.expression = expression;
        return stage;
    }
}
