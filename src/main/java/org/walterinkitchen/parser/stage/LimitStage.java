package org.walterinkitchen.parser.stage;

import lombok.Data;

@Data
public class LimitStage extends AbsStage {
    private long offset;
    private long size;

    @Override
    <T, C> T accept(StageVisitor<T, C> visitor, C context) {
        return visitor.visit(this, context);
    }
}
