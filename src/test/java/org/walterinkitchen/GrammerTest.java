package org.walterinkitchen;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.walterinkitchen.parser.BaseMongoProvider;

import java.util.List;

public class GrammerTest {

    public MongoTemplate mongoTemplate() {
        return null;
    }

//    @Test
    public void sqlTest() {
//        String sql = "SELECT * FROM goods WHERE price > 100 && cost < price ORDER BY price DESC, cost ASC LIMIT 10, 100";
//        String sql = "SELECT name, price FROM goods WHERE price > 100 && cost < price ORDER BY price DESC, cost ASC LIMIT 10, 100";
        String sql = "SELECT name, price FROM goods ORDER BY price DESC, cost ASC LIMIT 10, 10";
//        String sql = "SELECT * FROM goods ORDER BY price ";
        BaseMongoProvider provider = new BaseMongoProvider(mongoTemplate());
        List<Object> list = provider.query(sql, Object.class);

        System.out.println(list);
    }
}
