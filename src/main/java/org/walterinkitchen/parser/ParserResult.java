package org.walterinkitchen.parser;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.mongodb.core.aggregation.Aggregation;

@Data
@AllArgsConstructor
public class ParserResult {
    /**
     * aggregation
     */
    private Aggregation aggregation;
    /**
     * target table
     */
    private String table;
}
