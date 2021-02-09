package org.walterinkitchen.parser.stage;

public interface StageVisitor<T, C> {
    default T visitDefault(AbsStage stage, C ctx) {
        return null;
    }

    default T visit(CountStage stage, C ctx) {
        return visitDefault(stage, ctx);
    }

    default T visit(FilterStage stage, C ctx) {
        return visitDefault(stage, ctx);
    }

    default T visit(FromStage stage, C ctx) {
        return visitDefault(stage, ctx);
    }

    default T visit(GroupStage stage, C ctx) {
        return visitDefault(stage, ctx);
    }

    default T visit(JoinStage stage, C ctx) {
        return visitDefault(stage, ctx);
    }

    default T visit(LimitStage stage, C ctx) {
        return visitDefault(stage, ctx);
    }

    default T visit(ProjectStage stage, C ctx) {
        return visitDefault(stage, ctx);
    }

    default T visit(SortStage stage, C ctx) {
        return visitDefault(stage, ctx);
    }
}
