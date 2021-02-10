package org.walterinkitchen.parser.aggregate;

import org.bson.Document;
import org.walterinkitchen.parser.stage.AbsStage;

import java.util.Collections;
import java.util.List;

public class AddIdAggregateChain extends AbsAggregateChain {
    protected AddIdAggregateChain(AbsAggregateChain next) {
        super(next);
    }

    @Override
    Result handle(List<AbsStage> stages, Context context) {
        Document document = new Document("$addFields", new Document("id", new Document("$toString", "$_id")));
        return Result.build(Collections.singletonList(x -> document), stages);
    }
}
