package org.walterinkitchen.parser.stage;

public class GroupStage extends AbsStage {
    @Override
    <T, C> T accept(StageVisitor<T, C> visitor, C context) {
        return visitor.visit(this, context);
    }
}
