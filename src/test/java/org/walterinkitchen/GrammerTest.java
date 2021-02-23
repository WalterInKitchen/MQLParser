package org.walterinkitchen;

import com.mongodb.ConnectionString;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.junit.Test;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.walterinkitchen.parser.BaseMongoProvider;

import java.util.List;

public class GrammerTest {

    public MongoTemplate mongoTemplate() {
        String connection = "mongodb://root:123456@127.0.0.1:27001/?authSource=admin&readPreference=primary";
        ConnectionString connectionString = new ConnectionString(connection);
        MongoClient client = MongoClients.create(connectionString);

        MongoTemplate template = new MongoTemplate(client, "test");
        return template;
    }

    @Test
    public void sqlTest() {
//        String sql = "SELECT * FROM goods WHERE price > 100 && cost < price ORDER BY price DESC, cost ASC LIMIT 10, 100";
//        String sql = "SELECT name, price FROM goods WHERE price > 100 && cost < price ORDER BY price DESC, cost ASC LIMIT 10, 100";
//        String sql = "SELECT name, price FROM goods ORDER BY price DESC, cost ASC LIMIT 10, 10";
//        String sql = "SELECT field_1 AS f1 FROM test ORDER BY field_1 ASC LIMIT 10";
//        String sql = "SELECT field_1 AS f1 FROM test WHERE price > 1 OR field_1 > 1 ORDER BY field_1 ASC LIMIT 10";
//        String sql = "SELECT field_1 AS f1 FROM test WHERE fd=NULL";
//        String sql = "SELECT field_1 ,field_2  FROM test WHERE field_1>2 XOR field_2>2";
//        String sql = "SELECT field_1 ,field_2  FROM test WHERE field_1 + 2 = field_2";
//        String sql = "SELECT *,field_1 AS f1,field_2, type  FROM test";
//        String sql = "SELECT *,field_1 AS f1,field_2, type  FROM test WHERE id = '601d32e58bff912880c35a91'";
//        String sql = "SELECT *,field_1 AS f1,field_2, type  FROM test WHERE name LIKE '\\^'";
//        String sql = "SELECT *,field_1 AS f1,field_2, type  FROM test WHERE date > DATE('2020', 'yyyy')";
//        String sql = "SELECT * FROM test";
//        String sql = "SELECT * FROM test WHERE dateToString(date, '%Y') = '2020'";
//        String sql = "SELECT *, dateToString(date, '%Y') AS year FROM test";
//        String sql = "SELECT *,dateToString(date, '%Y') AS year FROM test";
        String sql = "SELECT *,dateToString(date, '%Y', '+08', '1999') AS year FROM test";
        BaseMongoProvider provider = new BaseMongoProvider(mongoTemplate());
        List<?> list = provider.query(sql, Object.class);

        System.out.println(list);
    }
}
