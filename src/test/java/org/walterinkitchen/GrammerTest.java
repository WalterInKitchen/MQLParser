package org.walterinkitchen;

import com.mongodb.ConnectionString;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.junit.Test;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.walterinkitchen.entity.Person;
import org.walterinkitchen.parser.BaseMongoProvider;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

public class GrammerTest {

    public MongoTemplate mongoTemplate() {
        String connection = "mongodb://root:123456@127.0.0.1:27001/?authSource=admin&readPreference=primary";
        ConnectionString connectionString = new ConnectionString(connection);
        MongoClient client = MongoClients.create(connectionString);

        MongoTemplate template = new MongoTemplate(client, "test");
        return template;
    }

    @Test
    public void dateFromString() throws ParseException {
        MongoTemplate template = mongoTemplate();
        BaseMongoProvider provider = new BaseMongoProvider(template);

        //Insert test data
        Person petter = new Person();
        petter.setFirstName("petter");
        petter.setSecondName("walt");
        petter.setBornDate(new SimpleDateFormat("yyyy-MM-dd").parse("1990-03-28"));
        template.insert(petter);

        List<Person> list = provider.query("SELECT * ,dateFromString('2020-12-13', '%Y-%m-%d', '+08') AS registerAt FROM person ", Person.class);
        Person person = list.get(0);
        if (person.getRegisterAt() == null) {
            throw new RuntimeException("test failed");
        }
        if (!new SimpleDateFormat("yyyy-MM-dd").format(person.getRegisterAt()).equals("2020-12-13")) {
            throw new RuntimeException("test failed");
        }

        Query query = Query.query(Criteria.where("firstName").is("petter"));
        template.remove(query, Person.COLLECTION);
    }

    @Test
    public void dateToString() throws ParseException {
        MongoTemplate template = mongoTemplate();
        BaseMongoProvider provider = new BaseMongoProvider(template);

        //Insert test data
        Person petter = new Person();
        petter.setFirstName("petter");
        petter.setSecondName("walt");
        petter.setBornDate(new SimpleDateFormat("yyyy-MM-dd").parse("1990-03-28"));
        petter.setRegisterAt(new SimpleDateFormat("yyyy-MM-dd").parse("2021-02-21"));
        template.insert(petter);

        List<Map> list = provider.query("SELECT * ,dateToString(bornDate, '%Y-%m-%d', '+08') AS bd FROM person ", Map.class);
        Map map = list.get(0);
        if (!map.get("bd").equals("1990-03-28")) {
            throw new RuntimeException("test failed");
        }

        Query query = Query.query(Criteria.where("firstName").is("petter"));
        template.remove(query, Person.COLLECTION);
    }
}
