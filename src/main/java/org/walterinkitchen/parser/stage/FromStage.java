package org.walterinkitchen.parser.stage;

import lombok.Data;

@Data
public class FromStage extends AbsStage {
    private String table;

    public FromStage(String table) {
        this.table = table;
    }

    @Override
    <T, C> T accept(StageVisitor<T, C> visitor, C context) {
        return visitor.visit(this, context);
    }
}
