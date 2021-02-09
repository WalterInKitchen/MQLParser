package org.walterinkitchen.parser.stage;

/**
 * The procedure of parse
 */
public abstract class AbsStage {

    abstract <T, C> T accept(StageVisitor<T, C> visitor, C context);

}
