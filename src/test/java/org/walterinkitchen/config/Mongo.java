package org.walterinkitchen.config;

import com.mongodb.ConnectionString;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.data.mongodb.core.MongoTemplate;

public class Mongo {
    private static final MongoTemplate mongo;

    static {
        String connection = "mongodb://root:123456@127.0.0.1:27001/?authSource=admin&readPreference=primary";
        ConnectionString connectionString = new ConnectionString(connection);
        MongoClient client = MongoClients.create(connectionString);
        mongo = new MongoTemplate(client, "test");
    }

    public static MongoTemplate mongoTemplate() {
        return mongo;
    }
}
