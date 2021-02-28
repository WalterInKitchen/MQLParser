package org.walterinkitchen.parser.stage;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.walterinkitchen.parser.expression.GroupByExpression;

public class GroupStage extends AbsStage {

    @Getter
    @Setter(AccessLevel.PROTECTED)
    private GroupByExpression expression;

    private GroupStage() {
    }

    @Override
    <T, C> T accept(StageVisitor<T, C> visitor, C context) {
        return visitor.visit(this, context);
    }

    public static GroupStage build(GroupByExpression expression) {
        GroupStage stage = new GroupStage();
        stage.setExpression(expression);
        return stage;
    }
}
