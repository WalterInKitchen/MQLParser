package org.walterinkitchen;

import org.junit.Test;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.walterinkitchen.config.Mongo;
import org.walterinkitchen.entity.Person;
import org.walterinkitchen.parser.BaseMongoProvider;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

public class GrammarTest {
    private MongoTemplate mongoTemplate() {
        return Mongo.mongoTemplate();
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

    @Test
    public void enumCompareInTest() {
        MongoTemplate template = mongoTemplate();
        BaseMongoProvider provider = new BaseMongoProvider(template);

        //Insert test data
        Person petter = new Person();
        petter.setFirstName("petter");
        petter.setSecondName("walt");
        petter.setTitle(Person.Title.BOSS);
        template.insert(petter);

        Person jhon = new Person();
        jhon.setFirstName("Jhon");
        jhon.setSecondName("xxxx");
        jhon.setTitle(Person.Title.ENGINEER);
        template.insert(jhon);

        Person bob = new Person();
        bob.setFirstName("Bob");
        bob.setSecondName("xxxx");
        bob.setTitle(Person.Title.SUPERVISOR);
        template.insert(bob);

        List<Person> people = provider.query("SELECT * FROM person WHERE title IN ('BOSS', 'ENGINEER')", Person.class);
        if (people.size() != 2) {
            throw new RuntimeException("test failed");
        }
        if (people.get(0).getTitle() != Person.Title.ENGINEER && people.get(0).getTitle() != Person.Title.BOSS) {
            throw new RuntimeException("test failed");
        }
        if (people.get(1).getTitle() != Person.Title.ENGINEER && people.get(1).getTitle() != Person.Title.BOSS) {
            throw new RuntimeException("test failed");
        }

        System.out.println(people);
        template.dropCollection(Person.class);
    }

    @Test
    public void exprTest() {
        MongoTemplate template = mongoTemplate();
        BaseMongoProvider provider = new BaseMongoProvider(template);

        //Insert test data
        Person petter = new Person();
        petter.setFirstName("petter");
        petter.setSalary(15000.0);
        petter.setBonusRate(10);
        petter.setTitle(Person.Title.BOSS);
        template.insert(petter);

        Person jhon = new Person();
        jhon.setFirstName("Jhon");
        jhon.setSalary(30000.0);
        jhon.setBonusRate(25);
        jhon.setTitle(Person.Title.ENGINEER);
        template.insert(jhon);

        String ql = "SELECT *, salary * (1 + bonusRate/100.0) AS income FROM person";
        List<Person> people = provider.query(ql, Person.class);
        System.out.println(people);

        if (people.get(0).getIncome() != petter.getSalary() * (1 + petter.getBonusRate() / 100.0)) {
            throw new RuntimeException("test failed");
        }
        if (people.get(1).getIncome() != jhon.getSalary() * (1 + jhon.getBonusRate() / 100.0)) {
            throw new RuntimeException("test failed");
        }

        template.dropCollection(Person.class);
    }

    @Test
    public void exprCompare() {
        MongoTemplate template = mongoTemplate();
        BaseMongoProvider provider = new BaseMongoProvider(template);

        //Insert test data
        Person petter = new Person();
        petter.setFirstName("petter");
        petter.setSalary(15000.0);
        petter.setBonusRate(10);
        petter.setTitle(Person.Title.BOSS);
        template.insert(petter);

        Person jhon = new Person();
        jhon.setFirstName("Jhon");
        jhon.setSalary(30000.0);
        jhon.setBonusRate(25);
        jhon.setTitle(Person.Title.ENGINEER);
        template.insert(jhon);

        String ql = "SELECT * FROM person WHERE salary * (1 + bonusRate/100.0) = 16500";
        List<Person> people = provider.query(ql, Person.class);
        for (Person p : people) {
            if (p.getSalary() * (1 + p.getBonusRate() / 100.0) - 16500 != 0)
                throw new RuntimeException("test failed");
        }
        template.dropCollection(Person.class);
    }
}
