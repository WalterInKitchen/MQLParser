package org.walterinkitchen.parser.stage;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.walterinkitchen.parser.expression.Expression;
import org.walterinkitchen.parser.misc.Direction;

import java.util.ArrayList;
import java.util.List;

@Data
public class SortStage extends AbsStage {
    private List<Option> options = new ArrayList<>();


    public SortStage addOption(Option option) {
        this.options.add(option);
        return this;
    }

    @Override
    <T, C> T accept(StageVisitor<T, C> visitor, C context) {
        return visitor.visit(this, context);
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Option {

        private Expression field;
        private Direction direction;

        public static Option build(Expression field, Direction direction) {
            return new Option(field, direction);
        }

        public static Option buildAsc(Expression field) {
            return new Option(field, Direction.ASC);
        }

        public static Option buildDesc(Expression field) {
            return new Option(field, Direction.DESC);
        }
    }
}
