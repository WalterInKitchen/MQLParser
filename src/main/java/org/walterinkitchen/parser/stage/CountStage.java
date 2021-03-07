package org.walterinkitchen.parser.stage;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.walterinkitchen.parser.expression.CountExpression;

import java.util.List;

@Getter
public class CountStage extends AbsStage {

    @Setter(AccessLevel.PROTECTED)
    private List<CountExpression> expressions;

    @Override
    <T, C> T accept(StageVisitor<T, C> visitor, C context) {
        return visitor.visit(this, context);
    }

    public static CountStage build(List<CountExpression> expressions) {
        CountStage stage = new CountStage();
        stage.setExpressions(expressions);
        return stage;
    }
}
