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
}
