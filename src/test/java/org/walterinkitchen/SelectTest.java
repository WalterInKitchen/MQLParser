package org.walterinkitchen;

import org.junit.Test;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.walterinkitchen.config.Mongo;
import org.walterinkitchen.entity.Person;
import org.walterinkitchen.parser.BaseMongoProvider;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

public class SelectTest {
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

    @Test
    public void groupBySumTest() {
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

        Person bob = new Person();
        bob.setFirstName("Bob");
        bob.setSalary(20000.0);
        bob.setBonusRate(25);
        bob.setTitle(Person.Title.ENGINEER);
        template.insert(bob);


        String ql = "SELECT title, SUM(salary) AS total from person WHERE salary > 0 GROUP BY title ORDER BY total";
        List<Map> result = provider.query(ql, Map.class);
        for (Map map : result) {
            Double total = (Double) map.get("total");
            if ("BOSS".equals(map.get("title"))) {
                if (total - 15000.0 != 0) {
                    throw new RuntimeException("test failed");
                }
            }
            if ("ENGINEER".equals(map.get("title"))) {
                if (total - 50000.0 != 0) {
                    throw new RuntimeException("test failed");
                }
            }
        }

        template.dropCollection(Person.class);
    }

    @Test
    public void groupByMaxMinTest() {
        MongoTemplate template = mongoTemplate();
        BaseMongoProvider provider = new BaseMongoProvider(template);

        //Insert test data
        Person petter = new Person();
        petter.setFirstName("petter");
        petter.setSalary(15000.0);
        petter.setBonusRate(10);
        petter.setTitle(Person.Title.BOSS);
        template.insert(petter);

        Person walter = new Person();
        walter.setFirstName("walter");
        walter.setSalary(25000.0);
        walter.setBonusRate(10);
        walter.setTitle(Person.Title.BOSS);
        template.insert(walter);

        Person jhon = new Person();
        jhon.setFirstName("Jhon");
        jhon.setSalary(30000.0);
        jhon.setBonusRate(25);
        jhon.setTitle(Person.Title.ENGINEER);
        template.insert(jhon);

        Person bob = new Person();
        bob.setFirstName("Bob");
        bob.setSalary(20000.0);
        bob.setBonusRate(25);
        bob.setTitle(Person.Title.ENGINEER);
        template.insert(bob);


        String ql = "SELECT title, MAX(salary) AS 'max', " +
                "MIN(salary) as 'min' ," +
                "AVG(salary) as 'avg' " +
                "from person WHERE salary > 0 GROUP BY title ORDER BY 'max'";
        List<Map> result = provider.query(ql, Map.class);
        for (Map map : result) {
            Double max = (Double) map.get("max");
            Double min = (Double) map.get("min");
            Double avg = (Double) map.get("avg");
            if (map.get("title").equals("BOSS")) {
                if (max - 25000 != 0) {
                    throw new RuntimeException("test failed");
                }
                if (min - 15000.0 != 0) {
                    throw new RuntimeException("test failed");
                }
                if (avg - 20000.0 != 0) {
                    throw new RuntimeException("test failed");
                }
            }
            if (map.get("title").equals("ENGINEER")) {
                if (max - 30000.0 != 0) {
                    throw new RuntimeException("test failed");
                }
                if (min - 20000.0 != 0) {
                    throw new RuntimeException("test failed");
                }
                if (avg - 25000.0 != 0) {
                    throw new RuntimeException("test failed");
                }
            }
        }

        template.dropCollection(Person.class);
    }

    @Test
    public void firstLastTest() throws ParseException {
        MongoTemplate template = mongoTemplate();
        template.dropCollection(Person.class);

        BaseMongoProvider provider = new BaseMongoProvider(template);

        //Insert test data
        Person petter = new Person();
        petter.setFirstName("petter");
        petter.setSalary(15000.0);
        petter.setBonusRate(10);
        petter.setBornDate(new SimpleDateFormat("yyyy-MM-dd").parse("1990-03-28"));
        petter.setTitle(Person.Title.BOSS);
        template.insert(petter);

        Person walter = new Person();
        walter.setFirstName("walter");
        walter.setSalary(25000.0);
        walter.setBonusRate(10);
        walter.setBornDate(new SimpleDateFormat("yyyy-MM-dd").parse("1993-02-20"));
        walter.setTitle(Person.Title.BOSS);
        template.insert(walter);

        Person jhon = new Person();
        jhon.setFirstName("Jhon");
        jhon.setSalary(30000.0);
        jhon.setBonusRate(25);
        jhon.setBornDate(new SimpleDateFormat("yyyy-MM-dd").parse("1988-01-18"));
        jhon.setTitle(Person.Title.ENGINEER);
        template.insert(jhon);

        Person bob = new Person();
        bob.setFirstName("Bob");
        bob.setSalary(20000.0);
        bob.setBonusRate(25);
        bob.setBornDate(new SimpleDateFormat("yyyy-MM-dd").parse("1999-11-12"));
        bob.setTitle(Person.Title.ENGINEER);
        template.insert(bob);

        String ql = "SELECT first(firstName) AS name, first(bornDate) as born FROM person GROUP BY title ORDER BY bornDate ASC";
        List<Map> result = provider.query(ql, Map.class);
        for (Map map : result) {
            Object title = map.get("title");
            String nameInDb = (String) map.get("name");
            String name = null;
            if (title.equals("BOSS")) {
                name = "petter";
            } else if (title.equals("ENGINEER")) {
                name = "Jhon";
            }
            if (!nameInDb.equals(name)) {
                throw new RuntimeException("test failed");
            }
        }

        ql = "SELECT last(firstName) AS name, last(bornDate) as born FROM person GROUP BY title ORDER BY bornDate ASC";
        result = provider.query(ql, Map.class);
        for (Map map : result) {
            Object title = map.get("title");
            String nameInDb = (String) map.get("name");
            String name = null;
            if (title.equals("BOSS")) {
                name = "walter";
            } else if (title.equals("ENGINEER")) {
                name = "Bob";
            }
            if (!nameInDb.equals(name)) {
                throw new RuntimeException("test failed");
            }
        }

        template.dropCollection(Person.class);
    }

    @Test
    public void stdDevPopSampTest() throws ParseException {
        MongoTemplate template = mongoTemplate();
        template.dropCollection(Person.class);

        BaseMongoProvider provider = new BaseMongoProvider(template);

        //Insert test data
        Person petter = new Person();
        petter.setFirstName("petter");
        petter.setSalary(55000.0);
        petter.setBonusRate(10);
        petter.setBornDate(new SimpleDateFormat("yyyy-MM-dd").parse("1990-03-28"));
        petter.setTitle(Person.Title.BOSS);
        template.insert(petter);

        Person walter = new Person();
        walter.setFirstName("walter");
        walter.setSalary(35000.0);
        walter.setBonusRate(10);
        walter.setBornDate(new SimpleDateFormat("yyyy-MM-dd").parse("1993-02-20"));
        walter.setTitle(Person.Title.BOSS);
        template.insert(walter);

        Person jhon = new Person();
        jhon.setFirstName("Jhon");
        jhon.setSalary(30000.0);
        jhon.setBonusRate(25);
        jhon.setBornDate(new SimpleDateFormat("yyyy-MM-dd").parse("1988-01-18"));
        jhon.setTitle(Person.Title.ENGINEER);
        template.insert(jhon);

        Person bob = new Person();
        bob.setFirstName("Bob");
        bob.setSalary(20000.0);
        bob.setBonusRate(25);
        bob.setBornDate(new SimpleDateFormat("yyyy-MM-dd").parse("1999-11-12"));
        bob.setTitle(Person.Title.ENGINEER);
        template.insert(bob);

        String ql = "SELECT sum(1) AS 'count', stdDevPop(salary) AS 'stdSalary' , stdDevSamp(salary) AS 'sapSalary' FROM person GROUP BY title ORDER BY bornDate ASC";
        List<Map> result = provider.query(ql, Map.class);


        template.dropCollection(Person.class);
    }

    @Test
    public void selectDistinctTest() throws ParseException {
        MongoTemplate template = mongoTemplate();
        template.dropCollection(Person.class);

        BaseMongoProvider provider = new BaseMongoProvider(template);

        //Insert test data
        Person petter = new Person();
        petter.setFirstName("petter");
        petter.setSalary(55000.0);
        petter.setBonusRate(10);
        petter.setCity("Shanghai");
        petter.setBornDate(new SimpleDateFormat("yyyy-MM-dd").parse("1990-03-28"));
        petter.setTitle(Person.Title.BOSS);
        template.insert(petter);

        Person walter = new Person();
        walter.setFirstName("walter");
        walter.setSalary(35000.0);
        walter.setBonusRate(10);
        walter.setCity("Beijing");
        walter.setBornDate(new SimpleDateFormat("yyyy-MM-dd").parse("1993-02-20"));
        walter.setTitle(Person.Title.BOSS);
        template.insert(walter);

        Person jhon = new Person();
        jhon.setFirstName("Jhon");
        jhon.setSalary(30000.0);
        jhon.setBonusRate(25);
        jhon.setCity("Shanghai");
        jhon.setBornDate(new SimpleDateFormat("yyyy-MM-dd").parse("1988-01-18"));
        jhon.setTitle(Person.Title.ENGINEER);
        template.insert(jhon);

        Person bob = new Person();
        bob.setFirstName("Bob");
        bob.setCity("Guangzhou");
        bob.setSalary(20000.0);
        bob.setBonusRate(25);
        bob.setBornDate(new SimpleDateFormat("yyyy-MM-dd").parse("1999-11-12"));
        bob.setTitle(Person.Title.ENGINEER);
        template.insert(bob);

        String ql = "SELECT DISTINCT title FROM person";
        List<Map> result = provider.query(ql, Map.class);
        for (Map map : result) {
            Object title = map.get("title");
            if ("BOSS".equals(title) || "ENGINEER".equals(title)) {
                continue;
            }
            throw new RuntimeException("test failed");
        }

        ql = "SELECT DISTINCT title AS 'tt',city FROM person;";
        result = provider.query(ql, Map.class);
        for (Map map : result) {
            Object title = map.get("tt");
            if ("BOSS".equals(title) || "ENGINEER".equals(title)) {
                continue;
            }
            throw new RuntimeException("test failed");
        }

        template.dropCollection(Person.class);
    }

    @Test
    public void selectDistinctExprTest() throws ParseException {
        MongoTemplate template = mongoTemplate();
        template.dropCollection(Person.class);

        BaseMongoProvider provider = new BaseMongoProvider(template);

        //Insert test data
        Person petter = new Person();
        petter.setFirstName("petter");
        petter.setSalary(55000.0);
        petter.setBonusRate(10);
        petter.setCity("Shanghai");
        petter.setBornDate(new SimpleDateFormat("yyyy-MM-dd").parse("1990-03-28"));
        petter.setTitle(Person.Title.BOSS);
        template.insert(petter);

        Person walter = new Person();
        walter.setFirstName("walter");
        walter.setSalary(35000.0);
        walter.setBonusRate(10);
        walter.setCity("Beijing");
        walter.setBornDate(new SimpleDateFormat("yyyy-MM-dd").parse("1993-02-20"));
        walter.setTitle(Person.Title.BOSS);
        template.insert(walter);

        Person jhon = new Person();
        jhon.setFirstName("Jhon");
        jhon.setSalary(30000.0);
        jhon.setBonusRate(25);
        jhon.setCity("Shanghai");
        jhon.setBornDate(new SimpleDateFormat("yyyy-MM-dd").parse("1988-01-18"));
        jhon.setTitle(Person.Title.ENGINEER);
        template.insert(jhon);

        Person bob = new Person();
        bob.setFirstName("Bob");
        bob.setCity("Guangzhou");
        bob.setSalary(20000.0);
        bob.setBonusRate(25);
        bob.setBornDate(new SimpleDateFormat("yyyy-MM-dd").parse("1999-11-12"));
        bob.setTitle(Person.Title.ENGINEER);
        template.insert(bob);

        String ql = "SELECT DISTINCT salary * 10 AS 'sa' FROM person";
        List<Map> result = provider.query(ql, Map.class);


        template.dropCollection(Person.class);
    }

    @Test
    public void nullCheckTest() throws ParseException {
        MongoTemplate template = mongoTemplate();
        template.dropCollection(Person.class);

        BaseMongoProvider provider = new BaseMongoProvider(template);

        //Insert test data
        Person petter = new Person();
        petter.setFirstName("petter");
        petter.setSalary(55000.0);
        petter.setBonusRate(10);
        petter.setCity("Shanghai");
        petter.setBornDate(new SimpleDateFormat("yyyy-MM-dd").parse("1990-03-28"));
        petter.setTitle(Person.Title.BOSS);
        template.insert(petter);

        Person walter = new Person();
        walter.setFirstName("walter");
        walter.setSalary(35000.0);
        walter.setBonusRate(10);
        walter.setCity("Beijing");
        walter.setBornDate(new SimpleDateFormat("yyyy-MM-dd").parse("1993-02-20"));
        walter.setTitle(Person.Title.BOSS);
        template.insert(walter);

        Person jhon = new Person();
        jhon.setFirstName("Jhon");
        jhon.setSalary(30000.0);
        jhon.setBonusRate(25);
        jhon.setCity("Shanghai");
        jhon.setBornDate(new SimpleDateFormat("yyyy-MM-dd").parse("1988-01-18"));
        jhon.setTitle(Person.Title.ENGINEER);
        jhon.setAdvance(false);
        template.insert(jhon);

        Person bob = new Person();
        bob.setFirstName("Bob");
        bob.setSecondName("Little");
        bob.setCity("Guangzhou");
        bob.setSalary(20000.0);
        bob.setBonusRate(25);
        bob.setBornDate(new SimpleDateFormat("yyyy-MM-dd").parse("1999-11-12"));
        bob.setTitle(Person.Title.ENGINEER);
        bob.setAdvance(true);
        template.insert(bob);

        String ql = "SELECT * FROM person WHERE secondName IS NULL ";
        List<Person> result = provider.query(ql, Person.class);
        for (Person person : result) {
            if (person.getSecondName() != null) {
                throw new RuntimeException("test failed");
            }
        }

        ql = "SELECT * FROM person WHERE secondName IS NOT NULL ";
        result = provider.query(ql, Person.class);
        for (Person person : result) {
            if (person.getSecondName() == null) {
                throw new RuntimeException("test failed");
            }
        }

        ql = "SELECT * FROM person WHERE advance IS TRUE ";
        result = provider.query(ql, Person.class);
        for (Person person : result) {
            if (!person.getAdvance()) {
                throw new RuntimeException("test failed");
            }
        }

        ql = "SELECT * FROM person WHERE advance IS FALSE";
        result = provider.query(ql, Person.class);
        for (Person person : result) {
            if (person.getAdvance()) {
                throw new RuntimeException("test failed");
            }
        }

        template.dropCollection(Person.class);
    }

    @Test
    public void selectCountTest() throws ParseException {
        MongoTemplate template = mongoTemplate();
        template.dropCollection(Person.class);

        BaseMongoProvider provider = new BaseMongoProvider(template);

        //Insert test data
        Person petter = new Person();
        petter.setFirstName("petter");
        petter.setSalary(55000.0);
        petter.setBonusRate(10);
        petter.setCity("Shanghai");
        petter.setBornDate(new SimpleDateFormat("yyyy-MM-dd").parse("1990-03-28"));
        petter.setTitle(Person.Title.BOSS);
        template.insert(petter);

        Person walter = new Person();
        walter.setFirstName("walter");
        walter.setSalary(35000.0);
        walter.setBonusRate(10);
        walter.setCity("Beijing");
        walter.setBornDate(new SimpleDateFormat("yyyy-MM-dd").parse("1993-02-20"));
        walter.setTitle(Person.Title.BOSS);
        template.insert(walter);

        Person jhon = new Person();
        jhon.setFirstName("Jhon");
        jhon.setSalary(30000.0);
        jhon.setBonusRate(25);
        jhon.setCity("Shanghai");
        jhon.setBornDate(new SimpleDateFormat("yyyy-MM-dd").parse("1988-01-18"));
        jhon.setTitle(Person.Title.ENGINEER);
        jhon.setAdvance(false);
        template.insert(jhon);

        Person bob = new Person();
        bob.setFirstName("Bob");
        bob.setSecondName("Little");
        bob.setCity("Guangzhou");
        bob.setSalary(20000.0);
        bob.setBonusRate(25);
        bob.setBornDate(new SimpleDateFormat("yyyy-MM-dd").parse("1999-11-12"));
        bob.setTitle(Person.Title.ENGINEER);
        bob.setAdvance(true);
        template.insert(bob);

        String ql = "SELECT COUNT(*) as total FROM person";
        List<Map> result = provider.query(ql, Map.class);
        if (!result.get(0).get("total").equals(4)) {
            throw new RuntimeException("test failed");
        }

        ql = "SELECT COUNT(advance) as total FROM person";
        result = provider.query(ql, Map.class);
        if (!result.get(0).get("total").equals(2)) {
            throw new RuntimeException("test failed");
        }


        template.dropCollection(Person.class);
    }

    @Test
    public void betweenTest() throws ParseException {
        MongoTemplate template = mongoTemplate();
        template.dropCollection(Person.class);

        BaseMongoProvider provider = new BaseMongoProvider(template);

        //Insert test data
        Person petter = new Person();
        petter.setFirstName("petter");
        petter.setSecondName("llsa");
        petter.setSalary(55000.0);
        petter.setBonusRate(10);
        petter.setCity("Shanghai");
        petter.setBornDate(new SimpleDateFormat("yyyy-MM-dd").parse("1990-03-28"));
        petter.setTitle(Person.Title.BOSS);
        template.insert(petter);

        Person walter = new Person();
        walter.setFirstName("walter");
        walter.setSecondName("journ");
        walter.setSalary(35000.0);
        walter.setBonusRate(10);
        walter.setCity("Beijing");
        walter.setBornDate(new SimpleDateFormat("yyyy-MM-dd").parse("1993-02-20"));
        walter.setTitle(Person.Title.BOSS);
        template.insert(walter);

        Person jhon = new Person();
        jhon.setFirstName("Jhon");
        jhon.setSecondName("baby");
        jhon.setSalary(18000.0);
        jhon.setBonusRate(25);
        jhon.setCity("Shanghai");
        jhon.setBornDate(new SimpleDateFormat("yyyy-MM-dd").parse("1988-01-18"));
        jhon.setTitle(Person.Title.ENGINEER);
        jhon.setAdvance(false);
        template.insert(jhon);

        Person bob = new Person();
        bob.setFirstName("Bob");
        bob.setSecondName("little");
        bob.setCity("Guangzhou");
        bob.setSalary(20000.0);
        bob.setBonusRate(25);
        bob.setBornDate(new SimpleDateFormat("yyyy-MM-dd").parse("1999-11-12"));
        bob.setTitle(Person.Title.ENGINEER);
        bob.setAdvance(true);
        template.insert(bob);

        String ql = "SELECT * FROM person WHERE secondName BETWEEN 'little' AND 'journ';";
        List<Person> people = provider.query(ql, Person.class);
        for (Person person : people) {
            if (!person.getSecondName().equals("little")
                    && !person.getSecondName().equals("journ")) {
                throw new RuntimeException("test failed");
            }
        }

        template.dropCollection(Person.class);
    }

    @Test
    public void ifNullTest() throws ParseException {
        MongoTemplate template = mongoTemplate();
        template.dropCollection(Person.class);

        BaseMongoProvider provider = new BaseMongoProvider(template);

        //Insert test data
        Person petter = new Person();
        petter.setFirstName("petter");
        petter.setSecondName("llsa");
        petter.setSalary(55000.0);
        petter.setBonusRate(12);
        petter.setCity("Shanghai");
        petter.setBornDate(new SimpleDateFormat("yyyy-MM-dd").parse("1990-03-28"));
        petter.setTitle(Person.Title.BOSS);
        template.insert(petter);

        Person walter = new Person();
        walter.setFirstName("walter");
        walter.setSecondName("journ");
        walter.setSalary(35000.0);
        walter.setCity("Beijing");
        walter.setBornDate(new SimpleDateFormat("yyyy-MM-dd").parse("1993-02-20"));
        walter.setTitle(Person.Title.BOSS);
        template.insert(walter);

        Person jhon = new Person();
        jhon.setFirstName("Jhon");
        jhon.setSecondName("baby");
        jhon.setSalary(18000.0);
        jhon.setBonusRate(35);
        jhon.setCity("Shanghai");
        jhon.setBornDate(new SimpleDateFormat("yyyy-MM-dd").parse("1988-01-18"));
        jhon.setTitle(Person.Title.ENGINEER);
        jhon.setAdvance(false);
        template.insert(jhon);

        Person bob = new Person();
        bob.setFirstName("Bob");
        bob.setSecondName("little");
        bob.setCity("Guangzhou");
        bob.setSalary(20000.0);
        bob.setBornDate(new SimpleDateFormat("yyyy-MM-dd").parse("1999-11-12"));
        bob.setTitle(Person.Title.ENGINEER);
        bob.setAdvance(true);
        template.insert(bob);

        String ql = "SELECT firstName, salary, bonusRate, salary * (1 + IFNULL(bonusRate/100.0,0)) as 'income' FROM person;";
        List<Map> maps = provider.query(ql, Map.class);
        for (Map map : maps) {
            BigDecimal salary = new BigDecimal(String.valueOf(map.get("salary")));
            BigDecimal rate = new BigDecimal(String.valueOf(map.get("bonusRate")));
            BigDecimal income = new BigDecimal(String.valueOf(map.get("income")));
            BigDecimal realIncome = salary.multiply(rate.divide(new BigDecimal("100")).add(new BigDecimal("1")));
            if (realIncome.subtract(income).compareTo(BigDecimal.ZERO) != 0) {
                throw new RuntimeException("test failed");
            }
        }

        template.dropCollection(Person.class);
    }

    @Test
    public void ucaseAndLcaseTest() throws ParseException {
        MongoTemplate template = mongoTemplate();
        template.dropCollection(Person.class);

        BaseMongoProvider provider = new BaseMongoProvider(template);

        //Insert test data
        Person petter = new Person();
        petter.setFirstName("petter");
        petter.setSecondName("llsa");
        template.insert(petter);

        Person walter = new Person();
        walter.setFirstName("walter");
        walter.setSecondName("journ");
        template.insert(walter);

        Person jhon = new Person();
        jhon.setFirstName("Jhon");
        jhon.setSecondName("baby");
        template.insert(jhon);

        Person bob = new Person();
        bob.setFirstName("Bob");
        bob.setSecondName("little");
        template.insert(bob);

        String ql = "SELECT *, UCASE(firstName) as uname, LCASE(firstName) as lname FROM person;";
        List<Map> list = provider.query(ql, Map.class);

        for (Map map : list) {
            String firstName = String.valueOf(map.get("firstName"));
            String uname = String.valueOf(map.get("uname"));
            String lname = String.valueOf(map.get("lname"));
            if (!firstName.toUpperCase().equals(uname)) {
                throw new RuntimeException("test failed");
            }
            if (!firstName.toLowerCase().equals(lname)) {
                throw new RuntimeException("test failed");
            }
        }

        template.dropCollection(Person.class);
    }

    @Test
    public void roundTest() throws ParseException {
        MongoTemplate template = mongoTemplate();
        template.dropCollection(Person.class);

        BaseMongoProvider provider = new BaseMongoProvider(template);

        //Insert test data
        Person petter = new Person();
        petter.setFirstName("petter");
        petter.setSecondName("llsa");
        petter.setSalary(200.12344);
        template.insert(petter);

        Person walter = new Person();
        walter.setFirstName("walter");
        walter.setSecondName("journ");
        walter.setSalary(123.12344);
        template.insert(walter);

        Person jhon = new Person();
        jhon.setFirstName("Jhon");
        jhon.setSecondName("baby");
        jhon.setSalary(123.456);
        template.insert(jhon);

        Person bob = new Person();
        bob.setFirstName("Bob");
        bob.setSecondName("little");
        bob.setSalary(768.080456);
        template.insert(bob);

        String ql = "SELECT firstName, salary, ROUND(salary, 0) as 'income' FROM person;";
        List<Person> res = provider.query(ql, Person.class);

        template.dropCollection(Person.class);
    }

    @Test
    public void selectDateTest() throws ParseException {
        MongoTemplate template = mongoTemplate();
        template.dropCollection(Person.class);

        BaseMongoProvider provider = new BaseMongoProvider(template);

        //Insert test data
        Person petter = new Person();
        petter.setFirstName("petter");
        petter.setSecondName("llsa");
        petter.setSalary(200.12344);
        template.insert(petter);

        Person walter = new Person();
        walter.setFirstName("walter");
        walter.setSecondName("journ");
        walter.setSalary(123.12344);
        template.insert(walter);

        Person jhon = new Person();
        jhon.setFirstName("Jhon");
        jhon.setSecondName("baby");
        jhon.setSalary(123.456);
        template.insert(jhon);

        Person bob = new Person();
        bob.setFirstName("Bob");
        bob.setSecondName("little");
        bob.setSalary(768.080456);
        template.insert(bob);

        String ql = "SELECT NOW() as registerAt from person";
        List<Person> res = provider.query(ql, Person.class);

        template.dropCollection(Person.class);
    }

    @Test
    public void absTest() throws ParseException {
        MongoTemplate template = mongoTemplate();
        template.dropCollection(Person.class);

        BaseMongoProvider provider = new BaseMongoProvider(template);

        //Insert test data
        Person petter = new Person();
        petter.setFirstName("petter");
        petter.setSecondName("llsa");
        petter.setSalary(-200.12344);
        template.insert(petter);

        Person walter = new Person();
        walter.setFirstName("walter");
        walter.setSecondName("journ");
        walter.setSalary(-123.12344);
        template.insert(walter);

        Person jhon = new Person();
        jhon.setFirstName("Jhon");
        jhon.setSecondName("baby");
        jhon.setSalary(-123.456);
        template.insert(jhon);

        Person bob = new Person();
        bob.setFirstName("Bob");
        bob.setSecondName("little");
        bob.setSalary(-768.080456);
        template.insert(bob);

        String ql = "SELECT ABS(salary) as 'salary' from person";
        List<Person> res = provider.query(ql, Person.class);
        for (Person person : res) {
            if (person.getSalary() < 0) {
                throw new RuntimeException("test failed");
            }
        }

        template.dropCollection(Person.class);
    }

    @Test
    public void powerTest() throws ParseException {
        MongoTemplate template = mongoTemplate();
        template.dropCollection(Person.class);

        BaseMongoProvider provider = new BaseMongoProvider(template);

        //Insert test data
        Person petter = new Person();
        petter.setFirstName("petter");
        petter.setSalary(100.0);
        template.insert(petter);

        Person walter = new Person();
        walter.setFirstName("walter");
        walter.setSecondName("journ");
        walter.setSalary(200.0);
        template.insert(walter);

        Person jhon = new Person();
        jhon.setFirstName("Jhon");
        jhon.setSecondName("baby");
        jhon.setSalary(300.0);
        template.insert(jhon);

        Person bob = new Person();
        bob.setFirstName("Bob");
        bob.setSecondName("little");
        bob.setSalary(400.0);
        template.insert(bob);

        String ql = "SELECT salary, POWER(salary, 2) as income FROM person;";
        List<Person> res = provider.query(ql, Person.class);
        for (Person person : res) {
            BigDecimal salary = new BigDecimal(String.valueOf(person.getSalary()));
            BigDecimal income = new BigDecimal(String.valueOf(person.getIncome()));
            if (salary.pow(2).compareTo(income) != 0) {
                throw new RuntimeException("test failed");
            }
        }

        template.dropCollection(Person.class);
    }

    @Test
    public void sqrtTest() throws ParseException {
        MongoTemplate template = mongoTemplate();
        template.dropCollection(Person.class);

        BaseMongoProvider provider = new BaseMongoProvider(template);

        //Insert test data
        Person petter = new Person();
        petter.setFirstName("petter");
        petter.setSalary(100.0);
        template.insert(petter);

        Person walter = new Person();
        walter.setFirstName("walter");
        walter.setSecondName("journ");
        walter.setSalary(400.0);
        template.insert(walter);

        Person jhon = new Person();
        jhon.setFirstName("Jhon");
        jhon.setSecondName("baby");
        jhon.setSalary(900.0);
        template.insert(jhon);

        Person bob = new Person();
        bob.setFirstName("Bob");
        bob.setSecondName("little");
        bob.setSalary(1600.0);
        template.insert(bob);

        String ql = "SELECT salary, SQRT(salary) as income FROM person;";
        List<Person> res = provider.query(ql, Person.class);
        for (Person person : res) {
            BigDecimal salary = new BigDecimal(String.valueOf(person.getSalary()));
            BigDecimal income = new BigDecimal(String.valueOf(person.getIncome()));
            if (income.pow(2).compareTo(salary) != 0) {
                throw new RuntimeException("test failed");
            }
        }

        template.dropCollection(Person.class);
    }

    @Test
    public void modTest() throws ParseException {
        MongoTemplate template = mongoTemplate();
        template.dropCollection(Person.class);

        BaseMongoProvider provider = new BaseMongoProvider(template);

        //Insert test data
        Person petter = new Person();
        petter.setFirstName("petter");
        petter.setSalary(100.0);
        template.insert(petter);

        Person walter = new Person();
        walter.setFirstName("walter");
        walter.setSecondName("journ");
        walter.setSalary(400.0);
        template.insert(walter);

        Person jhon = new Person();
        jhon.setFirstName("Jhon");
        jhon.setSecondName("baby");
        jhon.setSalary(900.0);
        template.insert(jhon);

        Person bob = new Person();
        bob.setFirstName("Bob");
        bob.setSecondName("little");
        bob.setSalary(1600.0);
        template.insert(bob);

        String ql = "SELECT salary, MOD(salary,3) as income FROM person;";
        List<Person> res = provider.query(ql, Person.class);
        for (Person person : res) {
            BigDecimal salary = new BigDecimal(String.valueOf(person.getSalary()));
            BigDecimal income = new BigDecimal(String.valueOf(person.getIncome()));
            if (income == null) {
                throw new RuntimeException("test failed");
            }
        }

        template.dropCollection(Person.class);
    }

    @Test
    public void logTest() throws ParseException {
        MongoTemplate template = mongoTemplate();
        template.dropCollection(Person.class);

        BaseMongoProvider provider = new BaseMongoProvider(template);

        //Insert test data
        Person petter = new Person();
        petter.setFirstName("petter");
        petter.setSalary(100.0);
        template.insert(petter);

        Person walter = new Person();
        walter.setFirstName("walter");
        walter.setSecondName("journ");
        walter.setSalary(400.0);
        template.insert(walter);

        Person jhon = new Person();
        jhon.setFirstName("Jhon");
        jhon.setSecondName("baby");
        jhon.setSalary(900.0);
        template.insert(jhon);

        Person bob = new Person();
        bob.setFirstName("Bob");
        bob.setSecondName("little");
        bob.setSalary(1600.0);
        template.insert(bob);

        String ql = "SELECT salary, LOG(salary,10) as income FROM person;";
        List<Person> res = provider.query(ql, Person.class);
        for (Person person : res) {
            BigDecimal salary = new BigDecimal(String.valueOf(person.getSalary()));
            BigDecimal income = new BigDecimal(String.valueOf(person.getIncome()));
            if (income == null) {
                throw new RuntimeException("test failed");
            }
        }

        template.dropCollection(Person.class);
    }

    @Test
    public void lnTest() throws ParseException {
        MongoTemplate template = mongoTemplate();
        template.dropCollection(Person.class);

        BaseMongoProvider provider = new BaseMongoProvider(template);

        //Insert test data
        Person petter = new Person();
        petter.setFirstName("petter");
        petter.setSalary(100.0);
        template.insert(petter);

        Person walter = new Person();
        walter.setFirstName("walter");
        walter.setSecondName("journ");
        walter.setSalary(400.0);
        template.insert(walter);

        Person jhon = new Person();
        jhon.setFirstName("Jhon");
        jhon.setSecondName("baby");
        jhon.setSalary(900.0);
        template.insert(jhon);

        Person bob = new Person();
        bob.setFirstName("Bob");
        bob.setSecondName("little");
        bob.setSalary(1600.0);
        template.insert(bob);

        String ql = "SELECT salary, LN(salary) as income FROM person;";
        List<Person> res = provider.query(ql, Person.class);
        for (Person person : res) {
            BigDecimal salary = new BigDecimal(String.valueOf(person.getSalary()));
            BigDecimal income = new BigDecimal(String.valueOf(person.getIncome()));
            if (income == null) {
                throw new RuntimeException("test failed");
            }
        }

        template.dropCollection(Person.class);
    }

    @Test
    public void truncTest() throws ParseException {
        MongoTemplate template = mongoTemplate();
        template.dropCollection(Person.class);

        BaseMongoProvider provider = new BaseMongoProvider(template);

        //Insert test data
        Person petter = new Person();
        petter.setFirstName("petter");
        petter.setSalary(100.22);
        template.insert(petter);

        Person walter = new Person();
        walter.setFirstName("walter");
        walter.setSecondName("journ");
        walter.setSalary(400.33);
        template.insert(walter);

        Person jhon = new Person();
        jhon.setFirstName("Jhon");
        jhon.setSecondName("baby");
        jhon.setSalary(900.11);
        template.insert(jhon);

        Person bob = new Person();
        bob.setFirstName("Bob");
        bob.setSecondName("little");
        bob.setSalary(1600.12);
        template.insert(bob);

        String ql = "SELECT salary, TRUNC(salary) as income FROM person;";
        List<Person> res = provider.query(ql, Person.class);
        for (Person person : res) {
            BigDecimal salary = new BigDecimal(String.valueOf(person.getSalary()));
            BigDecimal income = new BigDecimal(String.valueOf(person.getIncome()));
            BigDecimal decimal = salary.setScale(0, RoundingMode.DOWN);
            if (income.compareTo(decimal) != 0) {
                throw new RuntimeException("test failed");
            }
        }

        template.dropCollection(Person.class);
    }


    @Test
    public void floorTest() throws ParseException {
        MongoTemplate template = mongoTemplate();
        template.dropCollection(Person.class);

        BaseMongoProvider provider = new BaseMongoProvider(template);

        //Insert test data
        Person petter = new Person();
        petter.setFirstName("petter");
        petter.setSalary(100.22);
        template.insert(petter);

        Person walter = new Person();
        walter.setFirstName("walter");
        walter.setSecondName("journ");
        walter.setSalary(400.33);
        template.insert(walter);

        Person jhon = new Person();
        jhon.setFirstName("Jhon");
        jhon.setSecondName("baby");
        jhon.setSalary(900.11);
        template.insert(jhon);

        Person bob = new Person();
        bob.setFirstName("Bob");
        bob.setSecondName("little");
        bob.setSalary(1600.12);
        template.insert(bob);

        String ql = "SELECT salary, FLOOR(salary) as income FROM person;";
        List<Person> res = provider.query(ql, Person.class);
        for (Person person : res) {
            BigDecimal salary = new BigDecimal(String.valueOf(person.getSalary()));
            BigDecimal income = new BigDecimal(String.valueOf(person.getIncome()));
            BigDecimal decimal = salary.setScale(0, RoundingMode.DOWN);
            if (income.compareTo(decimal) != 0) {
                throw new RuntimeException("test failed");
            }
        }

        ql = "SELECT salary, CEIL(salary) as income FROM person;";
        res = provider.query(ql, Person.class);
        for (Person person : res) {
            BigDecimal salary = new BigDecimal(String.valueOf(person.getSalary()));
            BigDecimal income = new BigDecimal(String.valueOf(person.getIncome()));
            income = income.subtract(BigDecimal.ONE);
            BigDecimal decimal = salary.setScale(0, RoundingMode.DOWN);
            if (income.compareTo(decimal) != 0) {
                throw new RuntimeException("test failed");
            }
        }

        template.dropCollection(Person.class);
    }
}
