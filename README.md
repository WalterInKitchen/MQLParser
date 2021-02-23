# SqlToMongoParser

## 说明

借助该库，你可以用类似SQL语法来查询MongoDB的数据：

```java
String sql = "SELECT *,field_1 AS f1,field_2, type FROM test WHERE name LIKE '\\^'";
BaseMongoProvider provider = new BaseMongoProvider(mongoTemplate());
List<Object> list = provider.query(sql, Object.class);
```

