package org.walterinkitchen;

import com.mongodb.MongoClientURI;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;
import org.walterinkitchen.parser.BaseMongoProvider;

import java.util.List;

public class GrammerTest {

    public MongoTemplate mongoTemplate() {
        MongoClientURI uri = new MongoClientURI("mongodb://root:123456@127.0.0.1:17018,127.0.0.1:17017/?authSource=admin&replicaSet=rs0&readPreference=primary");
        MongoDbFactory factory = new SimpleMongoDbFactory(uri);
        MongoTemplate template = new MongoTemplate(factory);
        return template;
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
