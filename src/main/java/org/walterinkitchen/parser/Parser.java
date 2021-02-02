package org.walterinkitchen.parser;

import org.springframework.data.mongodb.core.aggregation.Aggregation;

/**
 * 解析SQL为Aggregation
 *
 * @author walter
 */
public interface Parser {

    /**
     * 解析SQL为Aggregation
     *
     * @param ql SQL
     * @return Aggregation
     */
    ParserResult parse(String ql);
}
