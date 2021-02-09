package org.walterinkitchen.parser.stage;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import org.walterinkitchen.parser.expression.Expression;

import java.util.List;

public class ProjectStage extends AbsStage {

    @Getter
    private List<Field> fields;

    public static ProjectStage build(List<Field> fields) {
        ProjectStage stage = new ProjectStage();
        stage.fields = fields;
        return stage;
    }

    @Override
    <T, C> T accept(StageVisitor<T, C> visitor, C context) {
        return visitor.visit(this, context);
    }

    @Data
    @AllArgsConstructor
    public static class Field {
        private Expression expression;
        private String as;
    }
}
