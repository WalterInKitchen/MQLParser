package org.walterinkitchen;

import org.junit.Test;
import org.walterinkitchen.parser.BaseMongoProvider;

import java.util.List;

public class GrammerTest {

    @Test
    public void sqlTest() {
        String sql = "SELECT * FROM goods WHERE price > 100 && cost < price ORDER BY price LIMIT 10";
//        String sql = "SELECT * FROM goods ORDER BY price ";
        BaseMongoProvider provider = new BaseMongoProvider();
        List<Object> list = provider.query(sql, Object.class);

        System.out.println(list);
    }
}
