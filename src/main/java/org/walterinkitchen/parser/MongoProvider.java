package org.walterinkitchen.parser;

import java.util.List;

public interface MongoProvider {

    <T> List<T> query(String ql, Class<T> outputType);

}
